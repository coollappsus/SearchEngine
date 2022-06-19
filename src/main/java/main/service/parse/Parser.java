package main.service.parse;

import main.service.database.DBConnection;
import main.service.exception.CanceledException;
import main.service.exception.StatusCodeException;
import main.model.*;
import main.controllers.properties.ConnectProperties;
import main.service.*;
import org.hibernate.SessionFactory;
import org.jsoup.nodes.Document;
import main.utils.LemmatizatorForParsing;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveAction;

public class Parser extends RecursiveAction implements Node {
    private final FieldService fieldService;
    private final IndexService indexService;
    private final PageService pageService;
    private final LemmaService lemmaService;
    private final SiteService siteService;
    private volatile boolean isCanceled = false;
    private List<Parser> tasks = new ArrayList<>();
    private final String URL;
    private String userAgent = ConnectProperties.getUserAgent();
    private String referrer = ConnectProperties.getRefferer();
    private final CopyOnWriteArrayList WORKED_LINKS;
    private Set<String> linksStr;
    private Connection.Response response;

    public Parser(FieldService fieldService, IndexService indexService, PageService pageService,
                  LemmaService lemmaService, SiteService siteService, String url,
                  CopyOnWriteArrayList workedLinks) {
        this.fieldService = fieldService;
        this.indexService = indexService;
        this.pageService = pageService;
        this.lemmaService = lemmaService;
        this.siteService = siteService;
        this.URL = url;
        this.WORKED_LINKS = workedLinks;
    }

    @Override
    public List<String> parsePage() {
        siteService.setSiteStatus(siteService.findByUrl(URL), Status.INDEXING, "");
        linksStr = new HashSet<>();
        Document doc = null;
        try {
            if (isCancelled()) {
                return new ArrayList<>();
//                throw new CanceledException("Индексация прервана пользователем");
            }
            Thread.sleep(150);
            response = Jsoup.connect(URL)
                    .followRedirects(false)
                    .execute();
            if (response.statusCode() != 200) {
                throw new StatusCodeException(response.body(), response.statusCode());
            }
            doc = Jsoup.connect(URL)
                    .ignoreContentType(true)
                    .userAgent(userAgent)
                    .referrer(referrer)
                    .get();
            parseLinks(doc);
            parseTagField(fieldService.findAll(), doc);
        } catch (IOException | StatusCodeException e) {
            StringBuilder error = new StringBuilder();
            error.append("Ошибка индексирования! Сайт - ").append(siteService.findByUrl(URL).getUrl())
                    .append(" вернул следующую ошибку - ").append(System.lineSeparator()).append(e.getMessage());
            siteService.setSiteStatus(siteService.findByUrl(URL), Status.FAILED, error.toString());
        } catch (InterruptedException e) {
            StringBuilder error = new StringBuilder();
            error.append("Индексация прервана пользователем. Идёт процесс завершения индексации");
            siteService.setSiteStatus(siteService.findByUrl(URL), Status.FAILED, error.toString());
            return new ArrayList<>();
        }
        return new ArrayList<>(linksStr);
    }

    @Override
    protected void compute() {
        ArrayList<String> linksList = new ArrayList<>();
        List<String> links = null;
//        try {
            if (isCancelled()) {
                return;
//                throw new CanceledException("Индексация прервана пользователем");
            } else {
                links = parsePage();
            }
//        } catch (CanceledException e) {
//            StringBuilder error = new StringBuilder();
//            error.append("Индексация прервана пользователем");
//            siteService.setSiteStatus(siteService.findByUrl(URL), Status.FAILED, error.toString());
//            return;
//        }
        for (String link : links) {
            try {
                if (!linksList.contains(link) && !WORKED_LINKS.contains(link)) {
                    if (isCancelled()) return;
                    linksList.add(link);
                    WORKED_LINKS.add(link);
                    Parser task = new Parser(fieldService, indexService, pageService, lemmaService, siteService,
                            link, WORKED_LINKS);
                    task.invoke();
                    tasks.add(task);
                }
            } catch (Exception e) {
                StringBuilder error = new StringBuilder();
                error.append("Ошибка выполнения рекурсивных задач! Страница - ").append(link).append(" Ошибка - ")
                        .append(e.getMessage());
                siteService.setSiteStatus(siteService.findByUrl(URL), Status.FAILED, error.toString());
                return;
            }
        }
    }

    private void parseLinks(Document doc) {
//        try {
            if (isCancelled()) {
                return;
//                throw new CanceledException("Индексация прервана пользователем");
            }
//        } catch (CanceledException e) {
//            StringBuilder error = new StringBuilder();
//            error.append("Индексация прервана пользователем");
//            siteService.setSiteStatus(siteService.findByUrl(URL), Status.FAILED, error.toString());
//            return;
//        }
        Elements links = doc.select("a[href]");
        links.forEach(element -> {
            String link = element.attr("abs:href");
            if (link.matches(URL + ".+") && (!link.equals(URL)) && (!link.contains("#"))) {
                linksStr.add(link);
            }
        });
        pageService.create(URL.replaceAll(siteService.findByUrl(URL).getUrl(), ""),
                response.statusCode(), doc.toString(), siteService.findByUrl(URL));
    }

    private void parseTagField(List<Field> extractFromField, Document doc) throws IOException {
        String textTitle = doc.select(extractFromField.get(0).getSelector()).text();
        String textBody = doc.select(extractFromField.get(1).getSelector()).text();
        LemmatizatorForParsing lemmaTitle = new LemmatizatorForParsing(lemmaService, siteService, textTitle, URL);
        LemmatizatorForParsing lemmaBody = new LemmatizatorForParsing(lemmaService, siteService, textBody, URL);
        TreeMap<String, Float> lemmaListTitle = lemmaTitle.runAndPrepare();
        TreeMap<String, Float> lemmaListBody = lemmaBody.runAndPrepare();
        TreeMap<String, Float> lemmaRank = indexService.calculationRank(lemmaListTitle, lemmaListBody);
        indexService.create(lemmaRank, response, URL);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return isCanceled = mayInterruptIfRunning;
    }

//    public void closeConnectDB() {
//        sessionFactory.getCurrentSession().flush();
//        sessionFactory.close();
//    }
}
