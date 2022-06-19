package main.repositories;

import main.model.Index;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface IndexRepository extends CrudRepository<Index, Integer> {

    @Modifying
    @Transactional
    @Query(value = "DELETE i FROM `index` i " +
            "INNER JOIN page p ON i.page_id " +
            "INNER JOIN site s ON p.site_id " +
            "WHERE s.url = ?1", nativeQuery = true)
    void deleteAllNotIn(String url);
}
