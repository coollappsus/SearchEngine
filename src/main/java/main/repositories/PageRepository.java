package main.repositories;

import main.model.Page;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PageRepository extends CrudRepository<Page, Integer> {

    @Modifying
    @Transactional
    @Query(value = "DELETE p FROM page p " +
            "INNER JOIN site s ON p.site_id = s.id " +
            "WHERE s.url = ?1", nativeQuery = true)
    void deleteAllNotIn(String url);

}
