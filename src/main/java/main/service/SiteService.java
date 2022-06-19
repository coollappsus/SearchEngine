package main.service;

import main.controllers.properties.SiteProperties;
import main.model.Site;
import main.model.Status;
import main.repositories.SiteRepository;
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
public class SiteService {

    @Resource
    private final SessionFactory sessionFactory = new DBConnection().getSessionFactory();
    private final SiteRepository siteRepository;
    private final SiteProperties siteProperties;
    private final ArrayList<Site> siteList;

    @Autowired
    public SiteService(SiteRepository siteRepository, SiteProperties siteProperties, ArrayList<Site> siteList) {
        this.siteRepository = siteRepository;
        this.siteProperties = siteProperties;
        this.siteList = siteList;
    }

    public void create() {
        siteList.clear();
        List<SiteProperties.SiteData> sites = siteProperties.getList();
        for (SiteProperties.SiteData site: sites) {
            Site site1 = new Site(Status.FAILED, System.currentTimeMillis(), "",
                    site.getUrl(), site.getName());
            siteList.add(site1);
        }
        if (!siteList.isEmpty()) {
            siteList.forEach(this::save);
        }
    }

    public synchronized Site findByUrl(String url) {
        List<SiteProperties.SiteData> siteDataList = siteProperties.getList();
        for (SiteProperties.SiteData siteData: siteDataList) {
            if (url.contains(siteData.getUrl())) {
                url = siteData.getUrl();
            }
        }
        Session session = sessionFactory.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        String sql = "from " + Site.class.getSimpleName() + " s where s.url LIKE :custName";
        List<Site> sites = session.createQuery(sql, Site.class).setParameter("custName", url).getResultList();
        session.flush();
        tx1.commit();
        session.close();
        if (sites.isEmpty()) return null;
        return sites.get(0);
    }

    public synchronized void save(Site site) {
        Session session = sessionFactory.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        Site foundSite = findByUrl(site.getUrl());
        if (foundSite == null) {
            siteRepository.save(site);
        } else {
//            if (foundSite.getStatus() == Status.FAILED && foundSite.getStatus().toString()
//                    .equals("Индексация прервана пользователем") && !site.getLastError().isEmpty()) return;
            String sql = "update " + Site.class.getSimpleName() +
                    " s set s.lastError = :custError, s.status = :custStatus, s.statusTime = :custTime" +
                    " where s.id LIKE :custId";
            session.createQuery(sql).setParameter("custError", site.getLastError())
                    .setParameter("custStatus", Status.valueOf(site.getStatus().name()))
                    .setParameter("custTime", site.getStatusTime()).setParameter("custId", site.getId())
                    .executeUpdate();
        }
        session.flush();
        tx1.commit();
        session.close();
    }


    public ArrayList<Site> getAll() {
        siteList.clear();
        Iterable<Site> siteIterable = siteRepository.findAll();
        siteIterable.forEach(siteList::add);
        return siteList;
    }

    public void delete(Site site) {
        siteRepository.delete(site);
    }

    public void checkOfExistenceAndDelete() {
        List<SiteProperties.SiteData> sites = siteProperties.getList();
        ArrayList<String> urlsSite = new ArrayList<>();
        sites.forEach(siteData -> urlsSite.add(siteData.getUrl()));
        siteList.forEach(site -> {
            if (!urlsSite.contains(site.getUrl())) {
                delete(site);
            }
        });
    }

    public void setSiteStatus(Site site, Status status, String error) {
        site.setStatus(status);
        site.setStatusTime(System.currentTimeMillis());
        site.setLastError(error);
        save(site);
    }
}
