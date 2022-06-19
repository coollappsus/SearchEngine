package main.controllers;

import main.controllers.properties.SiteProperties;
import main.dto.ResultDto;
import main.dto.SearchDto;
import main.model.FoundPage;
import main.service.FoundPageService;
import main.service.IndexService;
import main.service.LemmaService;
import main.service.PageService;
import main.service.search.Search;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class PageController {

    private final IndexService indexService;
    private final SiteProperties properties;
    private final PageService pageService;
    private final LemmaService lemmaService;
    private final FoundPageService foundPageService;

    @Autowired
    public PageController(IndexService indexService, SiteProperties properties, PageService pageService,
                          LemmaService lemmaService, FoundPageService foundPageService) {
        this.indexService = indexService;
        this.properties = properties;
        this.pageService = pageService;
        this.lemmaService = lemmaService;
        this.foundPageService = foundPageService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String query,
                                 @RequestParam(required = false) String site,
                                 @RequestParam(required = false, defaultValue = "0") int offset,
                                 @RequestParam(required = false, defaultValue = "20") int limit) {
        if (query.isEmpty()) {
            return ResponseEntity.badRequest().body(new ResultDto("Задан пустой поисковый запрос"));
        }
        try {
            foundPageService.clearFoundPages();
            Search search = new Search(lemmaService, pageService, indexService, foundPageService, properties, query, site);
            search.start();
            while (true) {
                if (search.getFutures().isDone()) {
                    Thread.sleep(1000);
                    break;
                }
            }
            ArrayList<FoundPage> fullData = foundPageService.getFoundPages();
            List<FoundPage> data = fullData.subList(offset, Math.min(fullData.size(), offset + limit));
            SearchDto searchDto = new SearchDto(data, fullData.size());
            return ResponseEntity.ok().body(searchDto);
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.internalServerError().body(
                    new ResultDto("В процессе поиска произошла неизвестная ошибка" +
                            System.lineSeparator() + e.getMessage()));
        } catch (NullPointerException e) {
            return ResponseEntity.ok().body(new SearchDto(new ArrayList<>(), 0));
        }
    }
}
