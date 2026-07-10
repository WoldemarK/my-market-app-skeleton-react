package ru.yandex.shop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.shop.dto.ItemDto;
import ru.yandex.shop.dto.PageResponse;
import ru.yandex.shop.enums.SortType;
import ru.yandex.shop.exception.ItemNotFoundException;
import ru.yandex.shop.mapper.ItemMapper;
import ru.yandex.shop.repository.ItemRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для управления товарами в интернет-магазине.
 * <p>
 * Предоставляет функциональность для:
 * <ul>
 *   <li>Поиска товаров с пагинацией и сортировкой</li>
 *   <li>Получения товара по идентификатору</li>
 *   <li>Группировки товаров для отображения в виде сетки</li>
 * </ul>
 * </p>
 * <p>
 * Сервис использует кеширование Redis для ускорения работы:
 * <ul>
 *   <li>{@code items-page} - кеширует результаты поиска с пагинацией</li>
 *   <li>{@code items} - кеширует отдельные товары по ID</li>
 * </ul>
 * </p>
 *
 * <p><b>Пример использования:</b></p>
 * <pre>
 * {@code
 * @Autowired
 * private ItemService itemService;
 *
 * // Поиск товаров с пагинацией
 * itemService.findItems("apple", SortType.PRICE_ASC, 1, 10)
 *     .subscribe(page -> {
 *         System.out.println("Items: " + page.items());
 *         System.out.println("Total: " + page.totalElements());
 *     });
 *
 * // Получение товара по ID
 * itemService.findById(1L)
 *     .subscribe(item -> System.out.println("Item: " + item.title()));
 *
 * // Группировка для отображения в 3 колонки
 * itemService.groupItems(itemService.findAll())
 *     .subscribe(rows -> {
 *         for (List<ItemDto> row : rows) {
 *             System.out.println("Row: " + row);
 *         }
 *     });
 * }
 * </pre>
 *
 * @author Kovtunov Vladimir
 * @version 1.0
 * @see ItemRepository
 * @see ItemMapper
 * @see ItemDto
 * @see PageResponse
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final TransactionalOperator transactionalOperator;

    /**
     * Выполняет поиск товаров с поддержкой пагинации, сортировки и фильтрации по названию.
     * <p>
     * Результаты поиска кешируются в Redis с ключом, состоящим из:
     * <ul>
     *   <li>Поискового запроса (search)</li>
     *   <li>Типа сортировки (sortType)</li>
     *   <li>Номера страницы (pageNumber)</li>
     *   <li>Размера страницы (pageSize)</li>
     * </ul>
     * </p>
     * <p>
     * Процесс выполнения:
     * <ol>
     *   <li>Валидация параметров пагинации</li>
     *   <li>Выполнение запроса к базе данных с сортировкой и пагинацией</li>
     *   <li>Подсчет общего количества товаров</li>
     *   <li>Формирование объекта {@link PageResponse} с метаданными</li>
     * </ol>
     * </p>
     *
     * @param search     поисковый запрос (фильтрация по названию, может быть null)
     * @param sortType   тип сортировки (ALPHA, PRICE_ASC, PRICE_DESC, может быть null)
     * @param pageNumber номер страницы (начинается с 1)
     * @param pageSize   размер страницы (количество товаров на странице)
     * @return {@link Mono} с {@link PageResponse} содержащим список товаров и метаданные
     * @throws IllegalArgumentException если pageNumber меньше 1 или pageSize меньше 1
     */
    @Cacheable(
            value = "items-page",
            key = "#search + '-' + (#sortType == null ? 'ALPHA' : #sortType.name()) + '-' + #pageNumber + '-' + #pageSize"
    )
    public Mono<PageResponse<ItemDto>> findItems(String search,
                                                 SortType sortType,
                                                 int pageNumber,
                                                 int pageSize
    ) {
        // Определение типа сортировки (по умолчанию ALPHA)
        SortType actualSortType = sortType != null ? sortType : SortType.ALPHA;

        // Вычисление смещения для пагинации
        long offset = (long) (pageNumber - 1) * pageSize;

        log.debug("findItems called: search={}, sort={}, page={}, size={}",
                search,
                actualSortType,
                pageNumber,
                pageSize
        );

        // Валидация параметров и выполнение запроса
        return validatePage(pageNumber, pageSize)
                .then(itemRepository.findItems(
                                        search,
                                        actualSortType.name(),
                                        pageSize,
                                        offset
                                )
                                .map(itemMapper::toDto)
                                .collectList()
                                .zipWith(itemRepository.countItems(search))
                                .map(tuple -> {
                                    List<ItemDto> items = tuple.getT1();
                                    long total = tuple.getT2();

                                    // Вычисление наличия предыдущей и следующей страницы
                                    boolean hasPrevious = pageNumber > 1;
                                    boolean hasNext = total > (long) pageNumber * pageSize;

                                    return new PageResponse<>(
                                            items,
                                            total,
                                            pageNumber,
                                            pageSize,
                                            hasNext,
                                            hasPrevious
                                    );
                                })
                )
                .as(transactionalOperator::transactional)
                .doOnSuccess(page ->
                        log.debug(
                                "Items loaded: totalElements={}, page={}, size={}",
                                page.totalElements(),
                                page.pageNumber(),
                                page.pageSize()
                        ));
    }

    /**
     * Проверяет валидность параметров пагинации.
     * <p>
     * Условия валидации:
     * <ul>
     *   <li>Номер страницы должен быть больше 0</li>
     *   <li>Размер страницы должен быть больше 0</li>
     * </ul>
     * </p>
     *
     * @param pageNumber номер страницы
     * @param pageSize   размер страницы
     * @return {@link Mono}&lt;Void&gt; - успешное завершение или ошибка
     * @throws IllegalArgumentException если параметры не прошли валидацию
     */
    private Mono<Void> validatePage(int pageNumber, int pageSize) {
        if (pageNumber < 1) {
            return Mono.error(new IllegalArgumentException("Page number must be greater than 0"));
        }

        if (pageSize < 1) {
            return Mono.error(new IllegalArgumentException("Page size must be greater than 0"));
        }

        return Mono.empty();
    }

    /**
     * Находит товар по его идентификатору.
     * <p>
     * Результат кешируется в Redis с ключом {@code items:{id}}.
     * При повторном запросе товар будет получен из кеша, что ускоряет работу.
     * </p>
     * <p>
     * Если товар не найден в базе данных, возвращается ошибка
     * {@link ItemNotFoundException}.
     * </p>
     *
     * @param id идентификатор товара
     * @return {@link Mono} с {@link ItemDto} найденного товара
     * @throws ItemNotFoundException если товар с указанным ID не найден
     */
    @Cacheable(value = "items", key = "#id")
    public Mono<ItemDto> findById(Long id) {
        log.debug("findById called: id={}", id);

        return itemRepository.findById(id)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Item not found: id={}", id);
                    return Mono.error(new ItemNotFoundException("Item not found: %d".formatted(id)));
                }))
                .doOnNext(item -> log.debug("Item found: id={}, title={}", item.getId(), item.getTitle()))
                .map(itemMapper::toDto);
    }

    /**
     * Группирует список товаров для отображения в виде сетки с 3 колонками.
     * <p>
     * Используется для представления товаров на главной странице в виде
     * карточек, расположенных в 3 колонки. Функция группирует товары в ряды
     * по 3 штуки. Если в последнем ряду меньше 3 товаров, он дополняется
     * пустыми элементами (с ID = -1).
     * </p>
     * <p>
     * <b>Пример:</b>
     * <pre>
     * Вход: [A, B, C, D, E]
     * Выход: [[A, B, C], [D, E, EMPTY]]
     * </pre>
     * </p>
     *
     * @param items поток товаров для группировки
     * @return {@link Mono} со списком рядов, каждый ряд содержит до 3 товаров
     */
    public Mono<List<List<ItemDto>>> groupItems(Flux<ItemDto> items) {
        return items
                .collectList()
                .map(list -> {
                    log.debug("groupItems called: itemsSize={}", list.size());

                    List<List<ItemDto>> result = new ArrayList<>();

                    for (int i = 0; i < list.size(); i += 3) {
                        List<ItemDto> row = new ArrayList<>(list.subList(i, Math.min(i + 3, list.size())));

                        while (row.size() < 3) {
                            row.add(createEmptyItem());
                        }

                        result.add(row);
                    }

                    log.debug("Items grouped into rows: rowCount={}", result.size());

                    return result;
                });
    }

    /**
     * Создает пустой элемент товара для заполнения сетки.
     * <p>
     * Используется для того, чтобы в каждом ряду было ровно 3 товара.
     * Пустой элемент имеет ID = -1, что позволяет идентифицировать его
     * на фронтенде и скрыть/не отображать.
     * </p>
     *
     * @return {@link ItemDto} с ID = -1 и остальными полями null
     */
    private ItemDto createEmptyItem() {
        return ItemDto.builder()
                .id(-1L)
                .build();
    }
}