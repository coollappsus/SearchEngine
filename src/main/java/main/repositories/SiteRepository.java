package main.repositories;

import main.model.Site;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SiteRepository extends CrudRepository<Site, Integer> {

    @Modifying
    @Transactional
    @Query(value = "DELETE s FROM site s WHERE s.url = ?1", nativeQuery = true)
    void deleteAllNotIn(String url);
}
