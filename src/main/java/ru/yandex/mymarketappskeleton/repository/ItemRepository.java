package ru.yandex.mymarketappskeleton.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.mymarketappskeleton.model.Item;


@Repository
public interface ItemRepository extends ReactiveCrudRepository<Item, Long> {

    @Query("""
                SELECT *
                FROM items
                WHERE (:search IS NULL OR :search = '' 
                       OR LOWER(title) LIKE LOWER(CONCAT('%', :search, '%')))
                ORDER BY 
                    CASE WHEN :sort = 'ALPHA' THEN title END,
                    CASE WHEN :sort = 'PRICE' THEN price END,
                    id
                LIMIT :limit OFFSET :offset
            """)
    Flux<Item> findItems(String search, String sort, int limit, long offset);

    @Query("""
                SELECT COUNT(*)
                FROM items
                WHERE (:search IS NULL OR :search = '' 
                       OR LOWER(title) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Mono<Long> countItems(String search);
}
