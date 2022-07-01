package main.service.parse;

import main.model.Field;
import main.model.Site;
import main.controllers.properties.ConnectProperties;
import main.service.*;
import main.utils.LemmatizatorForParsing;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ParserForOnePage implements Node{
    private final FieldService fieldService;
    private final IndexService indexService;
    private final PageService pageService;
    private final LemmaService lemmaService;
    private final SiteService siteService;
    private final String URL;
    private final String userAgent;
    private final String referrer;
    private Connection.Response response;

    public ParserForOnePage(FieldService fieldService, IndexService indexService, PageService pageService,
                            LemmaService lemmaService, SiteService siteService, String url) {
        this.fieldService = fieldService;
        this.indexService = indexService;
        this.pageService = pageService;
        this.lemmaService = lemmaService;
        this.siteService = siteService;
        this.URL = url;

        userAgent = ConnectProperties.getUserAgent();
        referrer = ConnectProperties.getRefferer();
    }


    @Override
    public List<String> parsePage() {
        if (!checkUrl()) return new ArrayList<>();
        ArrayList<String> result = new ArrayList<>();
        fieldService.create();
        result.add(URL);
        Document doc = null;
        try {
            doc = Jsoup.connect(URL)
                    .ignoreContentType(true)
                    .userAgent(userAgent)
                    .referrer(referrer)
                    .get();
            response = Jsoup.connect(URL)
                    .followRedirects(false)
                    .execute();
            pageService.create(URL.replaceAll(siteService.findByUrl(URL).getUrl(), ""),
                    response.statusCode(), doc.toString(), siteService.findByUrl(URL));
            parseTagField(fieldService.findAll(), doc);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return result;
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

    public boolean checkUrl() {
        ArrayList<Site> siteList = siteService.getAll();
        for (Site site: siteList) {
            if (!URL.contains(site.getUrl())) return true;
        }
        return false;
    }
}
