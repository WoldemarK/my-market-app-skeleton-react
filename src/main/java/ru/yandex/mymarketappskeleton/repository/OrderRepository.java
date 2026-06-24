package ru.yandex.mymarketappskeleton.repository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.mymarketappskeleton.model.Order;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
}
