package main.service.parse;

import main.controllers.properties.ConnectProperties;
import main.model.Field;
import main.model.Status;
import main.service.*;
import main.service.exception.StatusCodeException;
import main.utils.LemmatizatorForParsing;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

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
    private final List<Parser> tasks;
    private final String URL;
    private final String userAgent;
    private final String referrer;
    private final CopyOnWriteArrayList<String> WORKED_LINKS;
    private Set<String> linksStr;
    private Connection.Response response;
    private boolean isCancelled;

    public Parser(FieldService fieldService, IndexService indexService, PageService pageService,
                  LemmaService lemmaService, SiteService siteService, String url,
                  CopyOnWriteArrayList<String> workedLinks) {
        this.fieldService = fieldService;
        this.indexService = indexService;
        this.pageService = pageService;
        this.lemmaService = lemmaService;
        this.siteService = siteService;
        this.URL = url;
        this.WORKED_LINKS = workedLinks;

        tasks = new ArrayList<>();
        userAgent = ConnectProperties.getUserAgent();
        referrer = ConnectProperties.getRefferer();
        isCancelled = false;
    }

    @Override
    public List<String> parsePage() {
        siteService.setSiteStatus(siteService.findByUrl(URL), Status.INDEXING, "");
        linksStr = new HashSet<>();
        Document doc = null;
        try {
            if (isCancelled) {
                return new ArrayList<>();
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
            if (isCancelled) {
                return;
            } else {
                links = parsePage();
            }
        for (String link : links) {
            try {
                if (!linksList.contains(link) && !WORKED_LINKS.contains(link) && !isCancelled) {
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
            if (isCancelled()) {
                return;
            }
        Elements links = doc.select("a[href]");
        links.forEach(element -> {
            String link = element.attr("abs:href");
            if (link.matches(URL + ".+") && (!link.equals(URL)) && (!link.contains("#"))) {
                linksStr.add(link);
            }
        });
        if (pageService.findByURL(URL.replaceAll(siteService.findByUrl(URL).getUrl(), ""),
                siteService.findByUrl(URL).getId()) == null) {
            pageService.create(URL.replaceAll(siteService.findByUrl(URL).getUrl(), ""),
                    response.statusCode(), doc.toString(), siteService.findByUrl(URL));
        }
    }

    public void parseTagField(List<Field> extractFromField, Document doc) throws IOException {
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
    public boolean cancel(boolean isCanceled) {
        return this.isCancelled = isCanceled;
    }

//    @Override
//    public boolean cancel(boolean mayInterruptIfRunning) {
//        return mayInterruptIfRunning;
//    }
}
