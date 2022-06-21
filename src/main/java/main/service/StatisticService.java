package main.service;

import main.controllers.properties.SiteProperties;
import main.dto.StatisticsDto;
import main.model.Lemma;
import main.model.Page;
import main.model.Site;
import main.model.Status;
import main.service.database.DBConnection;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class StatisticService {

    @Resource
    private final SessionFactory sessionFactory = new DBConnection().getSessionFactory();
    private final LemmaService lemmaService;
    private final PageService pageService;
    private final SiteProperties properties;
    private final SiteService siteService;

    @Autowired
    public StatisticService(LemmaService lemmaService, PageService pageService, SiteProperties properties,
                            SiteService siteService) {
        this.lemmaService = lemmaService;
        this.pageService = pageService;
        this.properties = properties;
        this.siteService = siteService;
    }

    public StatisticsDto.Total createTotal() {
        boolean isIndexing = false;
        List<SiteProperties.SiteData> siteData = properties.getList();
        String sqlSites = "from " + Site.class.getSimpleName() + " s";
        String sqlPages = "from " + Page.class.getSimpleName() + " p";
        String sqlLemmas = "from " + Lemma.class.getSimpleName() + " l";
        String sqlIsIndexing = "from " + Site.class.getSimpleName() + " s where s.url LIKE :custName";
        Session session = sessionFactory.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        int sites = session.createQuery(sqlSites, Site.class).getResultList().size();
        int pages = session.createQuery(sqlPages, Page.class).getResultList().size();
        int lemmas = session.createQuery(sqlLemmas, Lemma.class).getResultList().size();
        for (SiteProperties.SiteData site: siteData) {
            String url = site.getUrl();
            List<Site> siteList = session.createQuery(sqlIsIndexing, Site.class).setParameter("custName", url)
                    .getResultList();
            if (siteList.isEmpty()) continue;
            Site site1 = siteList.get(0);
            isIndexing = site1.getStatus().name().equals("INDEXING");
            if (isIndexing) break;
        }
        session.flush();
        tx1.commit();
        session.close();
        return new StatisticsDto.Total(sites, pages, lemmas, isIndexing);
    }

    public StatisticsDto.Detailed createDetailed(Site site) {
        int siteId = site.getId();
        int pagesCount = pageService.getCountBySiteId(siteId);
        int lemmasCount = lemmaService.getCountBySiteId(siteId);
        return new StatisticsDto.Detailed(site.getUrl(), site.getName(), (Status) site.getStatus(),
                site.getStatusTime(), site.getLastError(), pagesCount, lemmasCount);
    }

    public StatisticsDto getStatistics() {
        ArrayList<Site> siteList = siteService.getAll();
        List<SiteProperties.SiteData> sites = properties.getList();
        ArrayList<StatisticsDto.Detailed> detailedList = new ArrayList<>();
        siteService.checkOfExistenceAndDelete();
        if (siteList.size() != sites.size()) {
            siteService.create();
        }
        for (Site site : siteList) {
            StatisticsDto.Detailed detailed = createDetailed(site);
            detailedList.add(detailed);
        }
        StatisticsDto.Statistics statistics = new StatisticsDto.Statistics(createTotal(), detailedList);
        return new StatisticsDto(statistics);
    }
}
