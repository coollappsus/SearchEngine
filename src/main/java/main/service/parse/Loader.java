package main.service.parse;

import main.model.Site;
import main.model.Status;
import main.service.*;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;

public class Loader extends Thread {
    private final IndexService indexService;
    private final FieldService fieldService;
    private final PageService pageService;
    private final LemmaService lemmaService;
    private final SiteService siteService;
    private boolean isCanceled = false;
    private ForkJoinPool pool = new ForkJoinPool();
    private Site site;
    private Parser processor;

    public Loader(IndexService indexService, FieldService fieldService, PageService pageService,
                  LemmaService lemmaService, SiteService siteService, Site site) {
        this.indexService = indexService;
        this.fieldService = fieldService;
        this.pageService = pageService;
        this.lemmaService = lemmaService;
        this.siteService = siteService;
        this.site = site;
    }


    @Override
    public void run() {
        try {
            siteService.setSiteStatus(site, Status.INDEXING, "");
            CopyOnWriteArrayList workedLink = new CopyOnWriteArrayList();
            workedLink.add(site.getUrl());
            processor = new Parser(fieldService, indexService, pageService,
                    lemmaService, siteService, site.getUrl(), workedLink);
            pool.execute(processor);
            processor.join();
            if (siteService.findByUrl(site.getUrl()).getStatus() == Status.INDEXING && isCanceled) {
                siteService.setSiteStatus(site, Status.FAILED, "Индексация прервана пользователем");
            } else {
                siteService.setSiteStatus(site, Status.INDEXED, "");
            }
        } catch (Exception e) {
            StringBuilder error = new StringBuilder();
            error.append(e.getMessage());
            siteService.setSiteStatus(site, Status.FAILED, error.toString());
        }
    }

    @Override
    public void interrupt() {
        if (processor == null) return;
        isCanceled = true;
        pool.shutdownNow();
        processor.cancel(true);
        //siteService.setSiteStatus(site, Status.FAILED, "Индексация прервана пользователем");
    }
}
