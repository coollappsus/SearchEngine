package main.repositories;

import main.model.Lemma;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface LemmaRepository extends CrudRepository<Lemma, Integer> {

    Lemma findByLemma (String lemma);

    @Modifying
    @Transactional
    @Query(value = "DELETE l FROM lemma l " +
            "INNER JOIN site s ON l.site_id = s.id " +
            "WHERE s.url = ?1",
            nativeQuery = true)
    void deleteAllNotIn(String url);
}
