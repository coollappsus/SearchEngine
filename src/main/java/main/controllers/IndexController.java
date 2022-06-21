package main.controllers;

import main.dto.ResultDto;
import main.model.Site;
import main.service.*;
import main.service.parse.Loader;
import main.service.parse.ParserForOnePage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RestController
public class IndexController {
    private final IndexService indexService;
    private final FieldService fieldService;
    private final PageService pageService;
    private final LemmaService lemmaService;
    private final SiteService siteService;
    private final StatisticService statisticService;
    private Future<Loader> indexingThread;
    private final ArrayList<Thread> threads = new ArrayList<>();

    @Autowired
    public IndexController(IndexService indexService, FieldService fieldService,
                           PageService pageService, LemmaService lemmaService,
                           SiteService siteService, StatisticService statisticService) {
        this.indexService = indexService;
        this.fieldService = fieldService;
        this.pageService = pageService;
        this.lemmaService = lemmaService;
        this.siteService = siteService;
        this.statisticService = statisticService;
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<ResultDto> startIndexing() {
        if (statisticService.createTotal().getIsIndexing()) {
            return ResponseEntity.badRequest().body(new ResultDto("Индексация уже запущена"));
        }
        try {
            fieldService.create();
            pageService.deleteAllInformation();
            indexingThread = null;
            threads.clear();
            getIndexingThread(siteService.getAll()).get();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ResultDto("Ошибка URL!" + System.lineSeparator() + e.getMessage()));
        }
        return ResponseEntity.ok(new ResultDto());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<ResultDto> stopIndexing() {
        ResultDto responseOk = new ResultDto();
        if (threads.isEmpty()) {
            ResultDto error = new ResultDto("Индексация не запущена, либо не может быть завершена!");
            return ResponseEntity.badRequest().body(error);
        }
        threads.forEach(thread -> {
            if (!thread.isInterrupted()) {
                thread.interrupt();
            }
        });
        threads.clear();
        return ResponseEntity.ok(responseOk);
    }

    @PostMapping("/indexPage")
    public ResponseEntity<ResultDto> indexPage(@RequestParam String url) {
        List<String> result = new ArrayList<>();
        if (url.isEmpty()) return ResponseEntity.badRequest().body(new ResultDto(
                "Задана пустая строка адреса"));
        try {
            ParserForOnePage parser = new ParserForOnePage(fieldService, indexService, pageService,
                    lemmaService, siteService, url);
            result = parser.parsePage();
        } catch (Exception e) {
            ResultDto resultDtoError = new ResultDto("Ошибка" + System.lineSeparator() + e.getMessage());
            return ResponseEntity.internalServerError().body(resultDtoError);
        }
        if (result.isEmpty()) return ResponseEntity.badRequest().body(new ResultDto(
                "Данная страница находится за пределами сайтов, указанных в конфигурационном файле"));
        return ResponseEntity.ok(new ResultDto());
    }

    private Future<Loader> getIndexingThread(List<Site> sites) {
        if (indexingThread == null) {
            ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
            indexingThread = pool.submit(() -> {
                Loader loader = null;
                for (Site site : sites) {
                    loader = new Loader(indexService, fieldService, pageService, lemmaService, siteService, site);
                    loader.start();
                    threads.add(loader);
                }
                return loader;
            });
        }
        return indexingThread;
    }
}
