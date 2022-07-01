package main.service.search;

import main.model.FoundPage;
import main.model.Lemma;
import main.model.Page;
import main.service.FoundPageService;
import main.utils.LemmatizatorForSearch;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchInPage extends Thread {

    private final FoundPageService foundPageService;
    private final String searchLine;
    private final ArrayList<Lemma> lemmaList;
    private final String urlSite;
    private final String nameSite;
    private final Page page;
    private final float relevance;
    private final LemmatizatorForSearch lemmatizator;
    private final StringBuffer snippet;

    public SearchInPage(FoundPageService foundPageService, String searchLine, ArrayList<Lemma> lemmaList,
                        String urlSite, String nameSite, Page page, float relevance) throws IOException {
        this.foundPageService = foundPageService;
        this.searchLine = searchLine;
        this.lemmaList = lemmaList;
        this.urlSite = urlSite;
        this.nameSite = nameSite;
        this.page = page;
        this.relevance = relevance;

        lemmatizator = new LemmatizatorForSearch(searchLine);
        snippet = new StringBuffer();
    }

    @Override
    public void run() {
        TreeMap<String, String> lemmatizatorList = lemmatizator.runAndPrepare();
        String snippetContent = Jsoup.parse(page.getContent()).text();
        for (Map.Entry<String, String> l : lemmatizatorList.entrySet()) {
            for (Lemma lemma : lemmaList) {
                if (l.getKey().equals(lemma.getLemma())) {
                    snippetContent = snippetContent.replaceAll(l.getValue(),
                            "<b>" + l.getValue() + "</b>");
                }
            }
        }
        createSnippet(snippetContent);
        createFoundPage();
    }

    private void createFoundPage() {
        AtomicInteger startTitleIndex = new AtomicInteger(page.getContent().indexOf("<title>"));
        AtomicInteger endTitleIndex = new AtomicInteger(page.getContent().indexOf("</title>"));
        FoundPage foundPage = new FoundPage(
                urlSite,
                nameSite,
                page.getPath().substring(0, page.getPath().length() - 1),
                page.getContent().substring(startTitleIndex.addAndGet(7), endTitleIndex.get()),
                snippet.toString(),
                relevance);
        foundPageService.setFoundPages(foundPage);
    }

    private void createSnippet(String content) {
        CopyOnWriteArrayList<String> sentences = new CopyOnWriteArrayList<>(content.split("\\."));
        for (String sentence: sentences) {
            AtomicInteger indexStart = new AtomicInteger(sentence.indexOf("<b>"));
            AtomicInteger indexEnd = new AtomicInteger(sentence.lastIndexOf("</b>"));
            if (indexStart.get() != -1 && indexEnd.get() != -1) {
                snippet.append("...").append(sentence).append("...");
            }
        }
    }
}
