package main.service.search;

import main.model.FoundPage;
import main.model.Lemma;
import main.model.Page;
import main.service.FoundPageService;
import main.service.PageService;
import main.utils.LemmatizatorForSearch;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.*;

public class SearchInPage extends Thread {

    private final FoundPageService foundPageService;
    private final String searchLine;
    private final ArrayList<Lemma> lemmaList;
    private final String urlSite;
    private final String nameSite;
    private final Page page;
    private final float relevance;
    private LemmatizatorForSearch lemmatizator;
    private StringBuffer snippet = new StringBuffer();

    public SearchInPage(FoundPageService foundPageService, String searchLine, ArrayList<Lemma> lemmaList,
                        String urlSite, String nameSite, Page page, float relevance) {
        this.foundPageService = foundPageService;
        this.searchLine = searchLine;
        this.lemmaList = lemmaList;
        this.urlSite = urlSite;
        this.nameSite = nameSite;
        this.page = page;
        this.relevance = relevance;
    }

    @Override
    public void run() {
        try {
            lemmatizator = new LemmatizatorForSearch(searchLine);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    private synchronized void createFoundPage() {
        int startTitleIndex = page.getContent().indexOf("<title>");
        int endTitleIndex = page.getContent().indexOf("</title>");
        FoundPage foundPage = new FoundPage(
                urlSite,
                nameSite,
                page.getPath().substring(0, page.getPath().length() - 1),
                page.getContent().substring(startTitleIndex + 7, endTitleIndex),
                snippet.toString(),
                relevance);
        foundPageService.setFoundPages(foundPage);
    }

    private synchronized void createSnippet(String content) {
        String[] sentences = content.split("\\.");
        for (String sentence: sentences) {
            int indexStart = sentence.indexOf("<b>");
            int indexEnd = sentence.lastIndexOf("</b>");
            if (indexStart != -1 && indexEnd != -1) {
                snippet.append("...").append(sentence).append("...");
            }
        }
    }
}
