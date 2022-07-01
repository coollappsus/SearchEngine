package main.service;

import main.controllers.properties.SiteProperties;
import main.dto.SearchDto;
import main.model.FoundPage;
import main.service.search.Search;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FoundPageService {

    private final IndexService indexService;
    private final SiteProperties properties;
    private final PageService pageService;
    private final LemmaService lemmaService;
    private final ArrayList<FoundPage> foundPages;

    @Autowired
    public FoundPageService(IndexService indexService, SiteProperties properties, PageService pageService,
                            LemmaService lemmaService) {
        this.indexService = indexService;
        this.properties = properties;
        this.pageService = pageService;
        this.lemmaService = lemmaService;
        foundPages = new ArrayList<>();
    }

    public ArrayList<FoundPage> getFoundPages() {
        foundPages.sort(FoundPage::compareTo);
        return foundPages;
    }

    public void setFoundPages(FoundPage foundPage) {
        foundPages.add(foundPage);
    }

    public void clearFoundPages() {
        foundPages.clear();
    }

    public SearchDto getSearchResult(String query, String site, int offset, int limit)
            throws IOException, InterruptedException {
        try {
            clearFoundPages();
            Search search = new Search(lemmaService, pageService, indexService,
                    this, properties, query, site, offset, limit);
            search.start();
            while (true) {
                if (search.getFutures().isDone()) {
                    Thread.sleep(1000);
                    break;
                }
            }
            ArrayList<FoundPage> data = getFoundPages();
//            ArrayList<FoundPage> fullData = getFoundPages();
//            List<FoundPage> data = fullData.subList(offset, Math.min(fullData.size(), offset + limit));
            return new SearchDto(data, search.getPageList().size());
        } catch (NullPointerException e) {
            return new SearchDto(new ArrayList<>(), 0);
        }
    }
}
