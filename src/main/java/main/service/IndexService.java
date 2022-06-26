package main.service;

import main.model.Index;
import main.model.Status;
import main.repositories.IndexRepository;
import main.repositories.LemmaRepository;
import main.service.database.DBConnection;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@Transactional
public class IndexService {

    @Resource
    private final SessionFactory sessionFactory = new DBConnection().getSessionFactory();
    private final IndexRepository indexRepository;
    private final LemmaRepository lemmaRepository;
    private final PageService pageService;
    private final SiteService siteService;

    @Autowired
    public IndexService(IndexRepository indexRepository, LemmaRepository lemmaRepository, PageService pageService,
                        SiteService siteService) {
        this.indexRepository = indexRepository;
        this.lemmaRepository = lemmaRepository;
        this.pageService = pageService;
        this.siteService = siteService;
    }

    public synchronized void create(TreeMap<String, Float> lemmaRank, Connection.Response response,
                       String URL) throws HibernateException {
        Session session = sessionFactory.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        for(Map.Entry<String, Float> m: lemmaRank.entrySet()) {
            if (response.statusCode() == 200 && siteService.findByUrl(URL).getStatus() == Status.INDEXING) {
                String shortUrl = URL.replaceAll(siteService.findByUrl(URL).getUrl(), "");
                if (shortUrl.equals("")) {shortUrl = "/";}
                Index index = new Index(
                        pageService.findByURL(shortUrl, siteService.findByUrl(URL).getId()),
                        lemmaRepository.findByLemma(m.getKey()),
                        m.getValue());
                save(index);
            }
        }
        session.flush();
        tx1.commit();
        session.close();
    }

    public TreeMap<String, Float> calculationRank(TreeMap<String, Float> lemmaListTitle,
                                                  TreeMap<String, Float> lemmaListBody) {
        TreeMap<String, Float> result = new TreeMap<>();
        for (Map.Entry<String, Float> b: lemmaListBody.entrySet()) {
            for (Map.Entry<String, Float> t: lemmaListTitle.entrySet()) {
                if (t.getKey().equals(b.getKey())) {
                    float value = t.getValue() + (b.getValue() * 0.8F);
                    result.put(t.getKey(), value);
                } else {
                    result.put(t.getKey(), t.getValue());
                    result.put(b.getKey(), b.getValue() * 0.8F);
                }
            }
        }
        return result;
    }

    public List<Index> findByIdLemma (int id) throws HibernateException {
        String sql = "from " + Index.class.getSimpleName() + " index where index.lemma.id LIKE :custName";
        Session session = sessionFactory.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        List<Index> indexList = session.createQuery(sql, Index.class).setParameter("custName", id).getResultList();
        session.flush();
        tx1.commit();
        session.close();
        return indexList;
    }

    public List<Index> findByIdPage (int id) throws HibernateException {
        String sql = "from " + Index.class.getSimpleName() + " index where index.page.id LIKE :custName";
        Session session = sessionFactory.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        List<Index> indexList = session.createQuery(sql, Index.class).setParameter("custName", id).getResultList();
        session.flush();
        tx1.commit();
        session.close();
        return indexList;
    }

    public Index findByIdLemmaAndIdPage(int idLemma, int idPage) throws HibernateException {
        String sql = "from " + Index.class.getSimpleName() + " index where index.page.id = " + idPage + " and " +
                "index.lemma.id = " + idLemma;
        Session session = sessionFactory.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        Index index = session.createQuery(sql, Index.class).uniqueResult();
        session.flush();
        tx1.commit();
        session.close();
        return index;
    }

    public void save(Index index) {
        Index indexFromDB = findByIdLemmaAndIdPage(index.getIdLemma(), index.getIdPage());
        if (indexFromDB == null) {
            indexRepository.save(index);
        }
    }

}
