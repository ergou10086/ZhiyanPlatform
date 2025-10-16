package hbnu.project.zhiyancommonbasic.container;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Set集合工具类 - 支持多线程操作
 * 提供Set集合的常用操作、转换方法和线程安全操作
 *
 * @author yui
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SetUtils {

    /**
     * 创建线程安全的CopyOnWriteArraySet
     */
    @SafeVarargs
    public static <T> Set<T> newConcurrentSet(T... elements) {
        Set<T> set = new CopyOnWriteArraySet<>();
        if (elements != null && elements.length > 0) {
            Collections.addAll(set, elements);
        }
        return set;
    }

    /**
     * 创建ConcurrentHashMap支持的并发Set
     */
    public static <T> Set<T> newConcurrentHashSet() {
        return ConcurrentHashMap.newKeySet();
    }

    /**
     * 创建HashSet
     */
    @SafeVarargs
    public static <T> Set<T> newHashSet(T... elements) {
        if (elements == null || elements.length == 0) {
            return new HashSet<>();
        }
        return Arrays.stream(elements).collect(Collectors.toSet());
    }

    /**
     * 创建LinkedHashSet
     */
    @SafeVarargs
    public static <T> Set<T> newLinkedHashSet(T... elements) {
        if (elements == null || elements.length == 0) {
            return new LinkedHashSet<>();
        }
        return Arrays.stream(elements).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 创建TreeSet
     */
    @SafeVarargs
    public static <T extends Comparable<? super T>> Set<T> newTreeSet(T... elements) {
        Set<T> set = new TreeSet<>();
        if (elements != null && elements.length > 0) {
            Collections.addAll(set, elements);
        }
        return set;
    }

    /**
     * 判断Set是否为空
     */
    public static <T> boolean isEmpty(Set<T> set) {
        return set == null || set.isEmpty();
    }

    /**
     * 判断Set是否不为空
     */
    public static <T> boolean isNotEmpty(Set<T> set) {
        return !isEmpty(set);
    }

    /**
     * 获取Set的大小，空安全
     */
    public static <T> int size(Set<T> set) {
        return set == null ? 0 : set.size();
    }

    /**
     * 合并多个Set
     */
    @SafeVarargs
    public static <T> Set<T> union(Set<T>... sets) {
        if (sets == null || sets.length == 0) {
            return new HashSet<>();
        }
        Set<T> result = new HashSet<>();
        for (Set<T> set : sets) {
            if (isNotEmpty(set)) {
                result.addAll(set);
            }
        }
        return result;
    }

    /**
     * 求两个Set的交集
     */
    public static <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
        if (isEmpty(set1) || isEmpty(set2)) {
            return new HashSet<>();
        }
        Set<T> result = new HashSet<>(set1);
        result.retainAll(set2);
        return result;
    }

    /**
     * 求两个Set的差集 (set1 - set2)
     */
    public static <T> Set<T> difference(Set<T> set1, Set<T> set2) {
        if (isEmpty(set1)) {
            return new HashSet<>();
        }
        if (isEmpty(set2)) {
            return new HashSet<>(set1);
        }
        Set<T> result = new HashSet<>(set1);
        result.removeAll(set2);
        return result;
    }

    /**
     * 判断set1是否包含set2的所有元素
     */
    public static <T> boolean containsAll(Set<T> set1, Set<T> set2) {
        if (isEmpty(set2)) {
            return true;
        }
        if (isEmpty(set1)) {
            return false;
        }
        return set1.containsAll(set2);
    }

    /**
     * 并行流处理Set中的元素
     */
    public static <T, R> Set<R> parallelMap(Set<T> set, Function<T, R> mapper) {
        return parallelMap(set, mapper, ForkJoinPool.commonPool());
    }

    /**
     * 使用指定的线程池并行处理Set中的元素
     */
    public static <T, R> Set<R> parallelMap(Set<T> set, Function<T, R> mapper, ExecutorService executor) {
        if (isEmpty(set) || mapper == null) {
            return new HashSet<>();
        }

        try {
            List<CompletableFuture<R>> futures = set.stream()
                    .map(element -> CompletableFuture.supplyAsync(() -> mapper.apply(element), executor))
                    .collect(Collectors.toList());

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );

            return allFutures.thenApply(v -> futures.stream()
                            .map(CompletableFuture::join)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet()))
                    .get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Parallel map operation failed", e);
            throw new RuntimeException("Parallel map operation failed", e);
        }
    }

    /**
     * 并行过滤Set中的元素
     */
    public static <T> Set<T> parallelFilter(Set<T> set, Predicate<T> predicate) {
        return parallelFilter(set, predicate, ForkJoinPool.commonPool());
    }

    /**
     * 使用指定的线程池并行过滤Set中的元素
     */
    public static <T> Set<T> parallelFilter(Set<T> set, Predicate<T> predicate, ExecutorService executor) {
        if (isEmpty(set) || predicate == null) {
            return new HashSet<>();
        }

        try {
            // 显式指定泛型类型，解决类型推断问题
            List<CompletableFuture<Optional<T>>> futures = set.stream()
                    .map(element -> CompletableFuture.supplyAsync(() ->
                            predicate.test(element) ? Optional.<T>of(element) : Optional.<T>empty(), executor))
                    .collect(Collectors.toList());

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );

            return allFutures.thenApply(v -> futures.stream()
                            .map(CompletableFuture::join)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toSet()))
                    .get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Parallel filter operation failed", e);
            throw new RuntimeException("Parallel filter operation failed", e);
        }
    }

    /**
     * 批量并行处理Set元素
     */
    public static <T> void parallelForEach(Set<T> set, Consumer<T> consumer) {
        parallelForEach(set, consumer, ForkJoinPool.commonPool());
    }

    /**
     * 使用指定的线程池批量并行处理Set元素
     */
    public static <T> void parallelForEach(Set<T> set, Consumer<T> consumer, ExecutorService executor) {
        if (isEmpty(set) || consumer == null) {
            return;
        }

        try {
            List<CompletableFuture<Void>> futures = set.stream()
                    .map(element -> CompletableFuture.runAsync(() -> {
                        try {
                            consumer.accept(element);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }, executor))
                    .collect(Collectors.toList());

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );

            allFutures.get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Parallel forEach operation failed", e);
            throw new RuntimeException("Parallel forEach operation failed", e);
        }
    }

    /**
     * 安全的Set转换（避免NPE）
     */
    public static <T, R> Set<R> safeTransform(Set<T> set, Function<T, R> mapper) {
        if (isEmpty(set) || mapper == null) {
            return new HashSet<>();
        }
        return set.stream()
                .filter(Objects::nonNull)
                .map(mapper)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * 将Set按指定大小分割成多个子Set
     */
    public static <T> List<Set<T>> partition(Set<T> set, int batchSize) {
        if (isEmpty(set) || batchSize <= 0) {
            return new ArrayList<>();
        }

        List<T> list = new ArrayList<>(set);
        List<Set<T>> partitions = new ArrayList<>();

        for (int i = 0; i < list.size(); i += batchSize) {
            int end = Math.min(i + batchSize, list.size());
            partitions.add(new HashSet<>(list.subList(i, end)));
        }

        return partitions;
    }

    /**
     * 并行分批处理Set元素
     */
    public static <T> void parallelBatchProcess(Set<T> set, int batchSize, Consumer<Set<T>> batchProcessor) {
        parallelBatchProcess(set, batchSize, batchProcessor, ForkJoinPool.commonPool());
    }

    /**
     * 使用指定的线程池并行分批处理Set元素
     */
    public static <T> void parallelBatchProcess(Set<T> set, int batchSize, Consumer<Set<T>> batchProcessor, ExecutorService executor) {
        if (isEmpty(set) || batchProcessor == null || batchSize <= 0) {
            return;
        }

        List<Set<T>> batches = partition(set, batchSize);

        try {
            List<CompletableFuture<Void>> futures = batches.stream()
                    .map(batch -> CompletableFuture.runAsync(() -> {
                        try {
                            batchProcessor.accept(batch);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }, executor))
                    .collect(Collectors.toList());

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );

            allFutures.get(300, TimeUnit.SECONDS); // 5分钟超时
        } catch (Exception e) {
            log.error("Parallel batch processing failed", e);
            throw new RuntimeException("Parallel batch processing failed", e);
        }
    }

    /**
     * 从集合创建Set
     */
    public static <T> Set<T> fromCollection(Collection<T> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            return new HashSet<>();
        }
        return new HashSet<>(collection);
    }

    /**
     * 转换为不可修改的Set
     */
    public static <T> Set<T> unmodifiableSet(Set<T> set) {
        if (set == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(set);
    }

    /**
     * 自定义函数式接口 - 消费者
     */
    @FunctionalInterface
    public interface Consumer<T> {
        void accept(T t) throws Exception;
    }
}
