package main.service.database;

import main.model.*;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import javax.annotation.Resource;

public class DBConnection {

    @Resource
    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration().configure();
                configuration.addAnnotatedClass(Field.class);
                configuration.addAnnotatedClass(Index.class);
                configuration.addAnnotatedClass(Lemma.class);
                configuration.addAnnotatedClass(Page.class);
                configuration.addAnnotatedClass(Site.class);
                configuration.setProperty("javax.persistence.query.timeout", "60000");
                StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties());
                sessionFactory = configuration.buildSessionFactory(builder.build());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sessionFactory;
    }
}
