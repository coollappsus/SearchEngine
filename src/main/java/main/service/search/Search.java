package main.service.search;

import main.controllers.properties.SiteProperties;
import main.model.Index;
import main.model.Lemma;
import main.model.Page;
import main.service.FoundPageService;
import main.service.IndexService;
import main.service.LemmaService;
import main.service.PageService;
import main.utils.LemmatizatorForSearch;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Search {
    private final LemmaService lemmaService;
    private final PageService pageService;
    private final IndexService indexService;
    private final FoundPageService foundPageService;
    private final SiteProperties siteProperties;
    private final TreeMap<Page, Float> pageRelevance;
    private final String site;
    private final String searchString;
    private final ArrayList<Lemma> lemmaList;
    private final ArrayList<Page> pageList;
    private final ExecutorService executorService;
    private final int offset;
    private final int limit;
    private CompletableFuture<Void> futures;

    public Search(LemmaService lemmaService, PageService pageService, IndexService indexService,
                  FoundPageService foundPageService, SiteProperties siteProperties, String searchString,
                  String site, int offset, int limit) {
        this.lemmaService = lemmaService;
        this.pageService = pageService;
        this.indexService = indexService;
        this.foundPageService = foundPageService;
        this.siteProperties = siteProperties;
        this.searchString = searchString;
        this.site = site;
        this.offset = offset;
        this.limit = limit;

        lemmaList = new ArrayList<>();
        pageList = new ArrayList<>();
        pageRelevance = new TreeMap<>(Comparator.comparing(Page::getId));
        executorService = Executors
                .newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 10);
    }

    public void start() throws IOException {
        LemmatizatorForSearch lemmatizator = new LemmatizatorForSearch(searchString);
        TreeMap<String, String> lemmatizatorList = lemmatizator.runAndPrepare();
        for (Map.Entry<String, String> m: lemmatizatorList.entrySet()) {
            Lemma lemma = lemmaService.findByLemma(m.getKey());
            if (lemma != null) {
                lemmaList.add(lemma);
            }
        }
        lemmaList.sort(Comparator.comparing(Lemma::getFrequency));
        calculatingPercent();
        if (!lemmaList.isEmpty()) searchPage();
    }

    private void calculatingPercent() {
        float acceptPercent = 80;
        float maxFrequency = lemmaService.getMaxFrequency();
        float acceptFrequency = (acceptPercent * maxFrequency) / 100;
        lemmaList.removeIf(lemma -> lemma.getFrequency() > acceptFrequency);
    }

    private void searchPage() {
        ArrayList<Integer> pageIds = new ArrayList<>();
        pageList.addAll(lemmaService.findPagesByLemma(lemmaList.get(0)));
        pageList.forEach(page -> pageIds.add(page.getId()));
        pageList.clear();
        for (int i = 1; i < lemmaList.size(); i++) {
            if (pageIds.isEmpty()) break;
            ArrayList<Integer> ids = new ArrayList<>();
            ArrayList<Page> pages = new ArrayList<>(lemmaService.findPagesByLemma(lemmaList.get(i)));
            pages.forEach(page -> ids.add(page.getId()));
            pageIds.removeIf(integer -> !ids.contains(integer));
        }
        if(!pageIds.isEmpty()) {
            pageIds.forEach(integer -> pageList.add(pageService.findById(integer)));
            if (site != null) pageList.removeIf(page -> !page.getSite().getUrl().contains(site));
            calculatingRelevance();
        }
        if (!pageRelevance.isEmpty()) createFoundPageList();
    }

    private void calculatingRelevance() {
        for (Page page: pageList) {
            for (Lemma lemma: lemmaList) {
                Index index = indexService.findByIdLemmaAndIdPage(lemma.getId(), page.getId());
                if (index != null) {
                    pageRelevance.merge(page, index.getRank(), Float::sum);
                }
            }
        }
        float maxAbsRelevance = pageRelevance.values().stream().max(Float::compare).isPresent() ?
                pageRelevance.values().stream().max(Float::compare).get() : 0;
        for (Map.Entry<Page, Float> p : pageRelevance.entrySet()) {
            float resultRelevance = maxAbsRelevance == 0 ? 0 : p.getValue() / maxAbsRelevance;
            if (resultRelevance == 0) pageRelevance.clear();
            else pageRelevance.replace(p.getKey(), p.getValue(), resultRelevance);
        }
    }

    private void createFoundPageList() {
        List<SiteProperties.SiteData> siteData = siteProperties.getList();
        futures = new CompletableFuture<Void>();
        List<Map.Entry<Page, Float>> pageRelevanceList = pageRelevance.entrySet().stream()
                .sorted((e1, e2) -> -e1.getValue().compareTo(e2.getValue())).toList();
        for (int i = offset; i < Math.min(pageList.size(), offset + limit); i++) {
            String content = pageRelevanceList.get(i).getKey().getContent();
            String nameSite = null;
            String urlSite = null;
            for (SiteProperties.SiteData siteData1 : siteData) {
                if (siteData1.getUrl().contains(pageRelevanceList.get(i).getKey().getSite().getUrl())) {
                    nameSite = siteData1.getName();
                    urlSite = siteData1.getUrl();
                }
            }
            try {
                futures = CompletableFuture.runAsync(new SearchInPage(foundPageService, content, lemmaList, urlSite,
                        nameSite, pageRelevanceList.get(i).getKey(),
                        pageRelevanceList.get(i).getValue()), executorService);
            } catch (IOException e) {
                return;
            }
        }
    }

    public CompletableFuture<Void> getFutures() {
        return futures;
    }

    public ArrayList<Page> getPageList() {
        return pageList;
    }
}
