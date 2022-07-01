package main.service;

import main.controllers.properties.SiteProperties;
import main.model.Page;
import main.model.Site;
import main.model.Status;
import main.repositories.IndexRepository;
import main.repositories.LemmaRepository;
import main.repositories.PageRepository;
import main.repositories.SiteRepository;
import main.service.database.DBConnection;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PageService {

    @Resource
    private final SessionFactory sessionFactory = new DBConnection().getSessionFactory();
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final SiteProperties siteProperties;

    @Autowired
    public PageService(PageRepository pageRepository, IndexRepository indexRepository,
                       SiteRepository siteRepository, LemmaRepository lemmaRepository,
                       SiteProperties siteProperties) {
        this.pageRepository = pageRepository;
        this.indexRepository = indexRepository;
        this.siteRepository = siteRepository;
        this.lemmaRepository = lemmaRepository;
        this.siteProperties = siteProperties;
    }

    public void create(String path, int statusCode, String content, Site site) {
        Page page = new Page(path, statusCode, content, site);
        if (page.getPath().equals("")) {page.setPath("/" + page.getPath());}
        savePage(page);
    }

    public Page findById (int id) {
        Optional<Page> optionalPage = pageRepository.findById(id);
        return optionalPage.orElse(null);
    }

    public Long getCountBySiteId(int siteId) {
        Session session = sessionFactory.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        String sql = "select count(*) from " + Page.class.getSimpleName() + " p where p.site.id LIKE :custNumber";
        Long countPages = (Long) session.createQuery(sql).setParameter("custNumber", siteId).getSingleResult();
        session.flush();
        tx1.commit();
        session.close();
        return countPages;
    }

    public Page findByURL (String url, int siteId) throws HibernateException {
        Session session = sessionFactory.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        String sql = "from " + Page.class.getSimpleName()
                + " p where p.path LIKE :custName AND p.site.id LIKE :custNumber";
        Page page = session.createQuery(sql, Page.class)
                .setParameter("custName", url).setParameter("custNumber", siteId).uniqueResult();
        session.flush();
        tx1.commit();
        session.close();
        return page;
    }

    public void savePage (Page page) {
        Optional<Site> siteOptional = siteRepository.findById(page.getSiteId());
        if (siteOptional.isPresent() && siteOptional.get().getStatus() == Status.INDEXING) {
            siteOptional.get().setStatusTime(System.currentTimeMillis());
            siteRepository.save(siteOptional.get());
            pageRepository.save(page);
        }
    }

    public void deleteAllInformation() {
        Session session = sessionFactory.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        List<String> urls = siteProperties.getList().stream().map(SiteProperties.SiteData::getUrl).toList();
        for (String url: urls) {
            indexRepository.deleteAllNotIn(url);
            lemmaRepository.deleteAllNotIn(url);
            pageRepository.deleteAllNotIn(url);
        }
        session.flush();
        tx1.commit();
        session.close();
    }
}
