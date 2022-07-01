package main.service;

import main.model.*;
import main.repositories.LemmaRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LemmaService {

    @Resource
    private final SessionFactory sessionFactory = new DBConnection().getSessionFactory();
    private final LemmaRepository lemmaRepository;
    private final SiteRepository siteRepository;
    private final IndexService indexService;

    @Autowired
    public LemmaService(LemmaRepository lemmaRepository, SiteRepository siteRepository, IndexService indexService) {
        this.lemmaRepository = lemmaRepository;
        this.siteRepository = siteRepository;
        this.indexService = indexService;
    }

    public Long getCountBySiteId(int siteId) {
        Session session = sessionFactory.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        String sql = "select count(*) from " + Lemma.class.getSimpleName() + " l where l.site.id LIKE :custNumber";
        Long countLemmas = (Long) session.createQuery(sql).setParameter("custNumber", siteId).getSingleResult();
        session.flush();
        tx1.commit();
        session.close();
        return countLemmas;
    }

    public Lemma findByLemma (String lemma) throws HibernateException {
        Session session = sessionFactory.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        String sql = "from " + Lemma.class.getSimpleName() + " l where l.lemma LIKE :custName";
        Lemma lemmaFromDB = session.createQuery(sql, Lemma.class).setParameter("custName", lemma).uniqueResult();
        session.flush();
        tx1.commit();
        session.close();
        return lemmaFromDB;
    }

    public List<Lemma> findByPage (Page page) throws HibernateException {
        List<Index> indexList = indexService.findByIdPage(page.getId());
        ArrayList<Lemma> lemmaList = new ArrayList<>();
        indexList.forEach(index -> lemmaList.add(index.getLemma()));
        return lemmaList;
    }

    public List<Page> findPagesByLemma (Lemma lemma) throws HibernateException {
        List<Index> indexList = indexService.findByIdLemma(lemma.getId());
        ArrayList<Page> pageList = new ArrayList<>();
        indexList.forEach(index -> pageList.add(index.getPage()));
        return pageList;
    }

    public Float getMaxFrequency() throws HibernateException {
        Session session = sessionFactory.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        String sql = "select max(frequency) from " + Lemma.class.getSimpleName();
        List<?> maxValue = session.createQuery(sql).getResultList();
        String max = maxValue.get(0).toString();
        session.flush();
        tx1.commit();
        session.close();
        return Float.parseFloat(max);
    }

    public void saveLemma (Lemma lemma) {
        Session session = sessionFactory.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        Optional<Site> siteOptional = siteRepository.findById(lemma.getSiteId());
        if (siteOptional.isPresent() && siteOptional.get().getStatus() == Status.INDEXING) {
            lemmaRepository.save(lemma);
        }
        session.flush();
        tx1.commit();
        session.close();
    }

    public void updateLemma(Lemma lemma) throws HibernateException {
        Session session = sessionFactory.getSessionFactory().openSession();
        Transaction tx1 = session.beginTransaction();
        Optional<Site> siteOptional = siteRepository.findById(lemma.getSiteId());
        if (siteOptional.isPresent() && siteOptional.get().getStatus() == Status.INDEXING) {
            long countPage = indexService.countByLemma(lemma.getId());
            if (countPage > lemma.getFrequency()) {
                lemma.setFrequency(lemma.getFrequency() + 1);
                session.update(lemma);
            }
        }
        session.flush();
        tx1.commit();
        session.close();
    }
}
