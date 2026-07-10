package ru.yandex.shop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.shop.payment.dto.PaymentRequest;
import ru.yandex.paymentservice.exception.InsufficientBalanceException;
import ru.yandex.shop.client.PaymentClient;
import ru.yandex.shop.dto.OrderDto;
import ru.yandex.shop.exception.EmptyCartException;
import ru.yandex.shop.exception.ItemNotFoundException;
import ru.yandex.shop.exception.OrderNotFoundException;
import ru.yandex.shop.exception.PaymentFailedException;
import ru.yandex.shop.mapper.OrderMapper;
import ru.yandex.shop.model.Item;
import ru.yandex.shop.model.Order;
import ru.yandex.shop.model.OrderItem;
import ru.yandex.shop.repository.ItemRepository;
import ru.yandex.shop.repository.OrderItemRepository;
import ru.yandex.shop.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Сервис для управления заказами.
 * <p>
 * Отвечает за создание заказов, получение списка всех заказов и получение заказа по ID.
 * Взаимодействует с сервисом платежей для проверки баланса и списания средств.
 * </p>
 *
 * @author Kovtunov Vladimir
 * @version 1.0
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartService cartService;
    private final OrderMapper orderMapper;
    private final PaymentClient paymentClient;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final TransactionalOperator transactionalOperator;

    private final OrderItemRepository orderItemRepository;

    /**
     * Создает новый заказ на основе содержимого корзины пользователя.
     * <p>
     * Процесс создания заказа включает следующие шаги:
     * <ol>
     *   <li>Получение содержимого корзины по sessionId</li>
     *   <li>Проверка, что корзина не пуста</li>
     *   <li>Проверка, что в корзине нет отрицательных количеств</li>
     *   <li>Поиск всех товаров из корзины в базе данных</li>
     *   <li>Проверка, что все товары существуют в базе данных</li>
     *   <li>Расчет общей суммы заказа</li>
     *   <li>Проверка, что сумма заказа положительная</li>
     *   <li>Заполнение элементов заказа (OrderItem)</li>
     *   <li>Обработка платежа и сохранение заказа</li>
     * </ol>
     * </p>
     *
     * @param sessionId идентификатор сессии пользователя
     * @return {@link Mono} с ID созданного заказа
     * @throws EmptyCartException           если корзина пуста
     * @throws IllegalArgumentException     если в корзине отрицательные количества или сумма заказа равна нулю
     * @throws ItemNotFoundException        если товар из корзины не найден в базе данных
     * @throws InsufficientBalanceException если на балансе недостаточно средств
     * @throws PaymentFailedException       если платеж не прошел
     */
    public Mono<Long> createOrder(String sessionId) {
        return Mono.defer(() -> {
            log.info("Create order started: sessionId={}", sessionId);
            // Получение содержимого корзины
            return cartService.getRawCart(sessionId)
                    .flatMap(cart -> {
                        if (cart.isEmpty()) {
                            return Mono.error(new EmptyCartException("Cart is empty"));
                        }

                        // Проверка на отрицательные количества в корзине
                        boolean hasNegativeQuantity = cart.values()
                                .stream()
                                .anyMatch(count -> count < 0);
                        if (hasNegativeQuantity) {
                            return Mono.error(new IllegalArgumentException("Cart contains negative quantities"));
                        }
                        // Поиск всех товаров из корзины в базе данных
                        return itemRepository.findAllById(cart.keySet())
                                .collectList()

                                .flatMap(items -> {
                                    // Создание карты товаров для быстрого доступа по ID
                                    Map<Long, Item> itemMap = items.stream()
                                            .collect(Collectors.toMap(
                                                    Item::getId,
                                                    i -> i,
                                                    (a, b) -> a
                                            ));

                                    // Проверка, что все товары из корзины найдены в БД
                                    for (Long itemId : cart.keySet()) {
                                        if (!itemMap.containsKey(itemId)) {
                                            log.warn("Item not found: id={}", itemId);
                                            return Mono.error(new ItemNotFoundException("Item not found: " + itemId));
                                        }
                                    }
                                    // Создание нового заказа
                                    Order order = new Order();

                                    // Расчет общей суммы заказа
                                    BigDecimal totalSum = calculateTotalSum(cart, itemMap);

                                    // Проверка, что сумма заказа положительная
                                    if (totalSum.compareTo(BigDecimal.ZERO) <= 0) {
                                        return Mono.error(new IllegalArgumentException("Order total must be positive"));
                                    }

                                    List<OrderItem> orderItems = buildOrderItems(cart, itemMap);
                                    order.setTotalSum(totalSum);
                                    order.setOrderDate(LocalDateTime.now());

                                    return processPaymentAndSaveOrder
                                            (
                                                    sessionId,
                                                    order,
                                                    orderItems,
                                                    totalSum
                                            );
                                });
                    });
        });
    }

    /**
     * Рассчитывает общую сумму заказа на основе корзины и карты товаров.
     * <p>
     * Для каждого товара в корзине умножает его цену на количество
     * и суммирует результаты.
     * </p>
     *
     * @param cart    карта корзины (ID товара -> количество)
     * @param itemMap карта товаров (ID товара -> объект Item)
     * @return общая сумма заказа в виде {@link BigDecimal}
     */
    private BigDecimal calculateTotalSum(Map<Long, Integer> cart, Map<Long, Item> itemMap) {
        return cart.entrySet()
                .stream()
                .map(entry -> {
                    Item item = itemMap.get(entry.getKey());
                    if (item == null) return BigDecimal.ZERO;
                    return item.getPrice().multiply(BigDecimal.valueOf(entry.getValue()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<OrderItem> buildOrderItems(Map<Long, Integer> cart,
                                            Map<Long, Item> itemMap) {

        List<OrderItem> result = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {

            Item item = itemMap.get(entry.getKey());

            if (item == null || entry.getValue() <= 0) {
                continue;
            }

            result.add(
                    OrderItem.builder()
                            .itemId(item.getId())
                            .title(item.getTitle())
                            .price(item.getPrice())
                            .count(entry.getValue())
                            .build()
            );
        }

        return result;
    }

    private Mono<Long> processPaymentAndSaveOrder(
            String sessionId,
            Order order,
            List<OrderItem> orderItems,
            BigDecimal totalSum) {

        return paymentClient.getBalance()

                .flatMap(balanceResponse -> {

                    if (balanceResponse.getBalance().compareTo(totalSum) < 0) {
                        return Mono.error(
                                new InsufficientBalanceException(
                                        String.format(
                                                "Not enough balance. Current: %.2f, Required: %.2f",
                                                balanceResponse.getBalance(),
                                                totalSum
                                        )
                                )
                        );
                    }

                    PaymentRequest request = new PaymentRequest();
                    request.setAmount(totalSum);

                    return paymentClient.pay(request);
                })

                .flatMap(payment -> {

                    if (payment == null || !Boolean.TRUE.equals(payment.getSuccess())) {
                        return Mono.error(new PaymentFailedException("Payment failed"));
                    }

                    log.info("Payment successful, saving order...");

                    return transactionalOperator.transactional(

                            orderRepository.save(order)

                                    .flatMap(savedOrder -> {

                                        log.info("Order saved id={}", savedOrder.getId());

                                        orderItems.forEach(item ->
                                                item.setOrderId(savedOrder.getId()));

                                        return orderItemRepository
                                                .saveAll(orderItems)
                                                .then(cartService.clear(sessionId))
                                                .thenReturn(savedOrder.getId());
                                    })
                    );
                });
    }

    /**
     * Возвращает список всех заказов.
     * <p>
     * Получает все заказы из базы данных и преобразует их в DTO.
     * </p>
     *
     * @return {@link Flux} с потоком {@link OrderDto} всех заказов
     */
    public Flux<OrderDto> findAll() {
        return orderRepository.findAll()
                .flatMap(order ->
                        orderItemRepository.findByOrderId(order.getId())
                                .collectList()
                                .map(items ->
                                        orderMapper.toDto(order, items))
                );
    }

    /**
     * Находит заказ по его идентификатору.
     * <p>
     * Ищет заказ в базе данных по ID. Если заказ не найден,
     * возвращает ошибку {@link OrderNotFoundException}.
     * </p>
     *
     * @param id идентификатор заказа
     * @return {@link Mono} с {@link OrderDto} найденного заказа
     * @throws OrderNotFoundException если заказ с указанным ID не найден
     */
    public Mono<OrderDto> findById(Long id) {
        log.debug("Find order by id={}", id);

        return orderRepository.findById(id)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Order not found: id={}", id);
                    return Mono.error(new OrderNotFoundException("Order not found: " + id));
                }))
                .flatMap(order ->
                        orderItemRepository.findByOrderId(order.getId())
                                .collectList()
                                .map(items -> orderMapper.toDto(order, items)))
                .doOnNext(dto ->
                        log.debug("Order found: id={}", dto.id()));
    }
}

