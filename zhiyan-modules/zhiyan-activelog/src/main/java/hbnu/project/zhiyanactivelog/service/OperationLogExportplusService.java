package hbnu.project.zhiyanactivelog.service;

import hbnu.project.zhiyanactivelog.mapper.OperationLogMapper;
import hbnu.project.zhiyanactivelog.model.dto.ProjectLogExportDTO;
import hbnu.project.zhiyanactivelog.model.dto.UnifiedLogExportDTO;
import hbnu.project.zhiyanactivelog.model.entity.*;
import hbnu.project.zhiyanactivelog.model.vo.UnifiedOperationLogVO;
import hbnu.project.zhiyanactivelog.repository.*;
import hbnu.project.zhiyancommonexcel.exception.ExcelException;
import hbnu.project.zhiyancommonexcel.utils.ExcelUtil;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 操作日志导出服务（优化版）
 * <p>
 * 优化点：
 * 1. 使用泛型 + 函数式接口抽象公共查询逻辑，消除重复代码
 * 2. 使用多路归并算法合并已排序的日志列表，避免全量内存排序
 * 3. 添加查询量限制，防止全表扫描导致的性能问题
 * 4. 修复原代码中的bug（任务日志被项目日志重复添加）
 * 5. 统一异常处理，提供更清晰的错误信息
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogExportplusService {

    private final ProjectOperationLogRepository projectLogRepository;
    private final TaskOperationLogRepository taskLogRepository;
    private final WikiOperationLogRepository wikiLogRepository;
    private final AchievementOperationLogRepository achievementLogRepository;
    private final LoginLogRepository loginLogRepository;
    private final OperationLogMapper operationLogMapper;

    /**
     * 默认最大查询条数，防止全表扫描
     */
    private static final int DEFAULT_MAX_QUERY_LIMIT = 10000;

    /**
     * 操作时间字段名
     */
    private static final String OPERATION_TIME_FIELD = "operationTime";

    /**
     * 登录时间字段名
     */
    private static final String LOGIN_TIME_FIELD = "loginTime";

    /**
     * 日志查询任务定义
     * 封装了实体类型、Repository、Mapper转换函数和时间字段名
     */
    private static class LogQueryTask<T> {
        final Class<T> entityClass;
        final JpaSpecificationExecutor<T> repository;
        final Function<T, UnifiedOperationLogVO> mapper;
        final String timeField;

        LogQueryTask(Class<T> entityClass, JpaSpecificationExecutor<T> repository,
                     Function<T, UnifiedOperationLogVO> mapper, String timeField) {
            this.entityClass = entityClass;
            this.repository = repository;
            this.mapper = mapper;
            this.timeField = timeField;
        }
    }

    /**
     * 导出项目操作日志
     * 可以指定导出的条数，不指定默认全部导出（但不超过最大限制）
     *
     * @param projectId     项目ID
     * @param operationType 操作类型
     * @param username      用户名
     * @param startTime     开始时间
     * @param endTime       结束时间
     * @param limit         导出条数限制，null表示不限制（但不超过默认最大值）
     * @param response      HTTP响应
     */
    public void exportProjectLogs(Long projectId, String operationType, String username,
                                  LocalDateTime startTime, LocalDateTime endTime,
                                  Integer limit, HttpServletResponse response) {
        try {
            // 构建查询条件
            Specification<ProjectOperationLog> spec = buildProjectLogSpec(
                    projectId, operationType, username, startTime, endTime);

            // 按时间倒序排序
            Sort sort = Sort.by(Sort.Direction.DESC, OPERATION_TIME_FIELD);

            // 应用查询限制
            int effectiveLimit = applyQueryLimit(limit);
            Pageable pageable = PageRequest.of(0, effectiveLimit, sort);

            // 查询日志
            List<ProjectOperationLog> logs = projectLogRepository.findAll(spec, pageable).getContent();

            // 转换为导出DTO
            List<ProjectLogExportDTO> exportList = logs.stream()
                    .map(operationLogMapper::toProjectExportDTO)
                    .collect(Collectors.toList());

            // 导出Excel
            ExcelUtil.exportExcel(exportList, "项目操作日志", ProjectLogExportDTO.class, response);

            log.info("导出项目操作日志成功，项目ID: {}, 条数: {}", projectId, exportList.size());
        } catch (ExcelException e) {
            log.error("导出项目操作日志失败（Excel异常）: {}", e.getMessage(), e);
            throw new RuntimeException("导出项目操作日志失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("导出项目操作日志失败: {}", e.getMessage(), e);
            throw new RuntimeException("导出项目操作日志失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导出我的操作日志
     * 可以指定导出的条数，不指定默认全部导出（但不超过最大限制）
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param limit     导出条数限制，null表示不限制（但不超过默认最大值）
     * @param response  HTTP响应
     */
    public void exportMyLogs(Long userId, LocalDateTime startTime, LocalDateTime endTime,
                             Integer limit, HttpServletResponse response) {
        try {
            // 定义所有需要查询的日志类型
            List<LogQueryTask<?>> queryTasks = Arrays.asList(
                    new LogQueryTask<>(
                            ProjectOperationLog.class,
                            projectLogRepository,
                            operationLogMapper::mapProject,
                            OPERATION_TIME_FIELD
                    ),
                    new LogQueryTask<>(
                            TaskOperationLog.class,
                            taskLogRepository,
                            operationLogMapper::mapTask,
                            OPERATION_TIME_FIELD
                    ),
                    new LogQueryTask<>(
                            WikiOperationLog.class,
                            wikiLogRepository,
                            operationLogMapper::mapWiki,
                            OPERATION_TIME_FIELD
                    ),
                    new LogQueryTask<>(
                            AchievementOperationLog.class,
                            achievementLogRepository,
                            operationLogMapper::mapAchievement,
                            OPERATION_TIME_FIELD
                    ),
                    new LogQueryTask<>(
                            LoginLog.class,
                            loginLogRepository,
                            operationLogMapper::mapLogin,
                            LOGIN_TIME_FIELD
                    )
            );

            // 应用查询限制
            int effectiveLimit = applyQueryLimit(limit);

            // 使用泛型方法查询所有类型的日志
            List<List<UnifiedOperationLogVO>> allLogLists = queryTasks.stream()
                    .map(task -> queryLogsByUserAndTime(task, userId, startTime, endTime, effectiveLimit))
                    .filter(list -> !list.isEmpty())
                    .collect(Collectors.toList());

            // 使用多路归并算法合并已排序的列表
            List<UnifiedOperationLogVO> mergedLogs = mergeSortedLogs(allLogLists, effectiveLimit);

            // 转换为导出DTO
            List<UnifiedLogExportDTO> exportList = mergedLogs.stream()
                    .map(operationLogMapper::toUnifiedExportDTO)
                    .collect(Collectors.toList());

            // 导出Excel
            ExcelUtil.exportExcel(exportList, "我的操作日志", UnifiedLogExportDTO.class, response);

            log.info("导出我的操作日志成功，用户ID: {}, 条数: {}", userId, exportList.size());
        } catch (ExcelException e) {
            log.error("导出我的操作日志失败（Excel异常）: {}", e.getMessage(), e);
            throw new RuntimeException("导出我的操作日志失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("导出我的操作日志失败: {}", e.getMessage(), e);
            throw new RuntimeException("导出我的操作日志失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导出我在指定项目内相关的所有操作日志
     *
     * @param userId    用户ID
     * @param projectId 项目ID
     * @param limit     导出条数限制，null表示不限制（但不超过默认最大值）
     * @param response  HTTP响应
     */
    public void exportMyProjectLogs(Long userId, Long projectId, Integer limit, HttpServletResponse response) {
        try {
            // 定义所有需要查询的日志类型（登录日志不包含项目ID，所以不查询）
            List<LogQueryTask<?>> queryTasks = Arrays.asList(
                    new LogQueryTask<>(
                            ProjectOperationLog.class,
                            projectLogRepository,
                            operationLogMapper::mapProject,
                            OPERATION_TIME_FIELD
                    ),
                    new LogQueryTask<>(
                            TaskOperationLog.class,
                            taskLogRepository,
                            operationLogMapper::mapTask,
                            OPERATION_TIME_FIELD
                    ),
                    new LogQueryTask<>(
                            WikiOperationLog.class,
                            wikiLogRepository,
                            operationLogMapper::mapWiki,
                            OPERATION_TIME_FIELD
                    ),
                    new LogQueryTask<>(
                            AchievementOperationLog.class,
                            achievementLogRepository,
                            operationLogMapper::mapAchievement,
                            OPERATION_TIME_FIELD
                    )
            );

            // 应用查询限制
            int effectiveLimit = applyQueryLimit(limit);

            // 使用泛型方法查询所有类型的日志
            List<List<UnifiedOperationLogVO>> allLogLists = queryTasks.stream()
                    .map(task -> queryLogsByUserAndProject(task, userId, projectId, effectiveLimit))
                    .filter(list -> !list.isEmpty())
                    .collect(Collectors.toList());

            // 使用多路归并算法合并已排序的列表
            List<UnifiedOperationLogVO> mergedLogs = mergeSortedLogs(allLogLists, effectiveLimit);

            // 转换为导出DTO
            List<UnifiedLogExportDTO> exportList = mergedLogs.stream()
                    .map(operationLogMapper::toUnifiedExportDTO)
                    .collect(Collectors.toList());

            // 导出Excel
            ExcelUtil.exportExcel(exportList, "项目内我的操作日志", UnifiedLogExportDTO.class, response);

            log.info("导出项目内我的操作日志成功，用户ID: {}, 项目ID: {}, 条数: {}", userId, projectId, exportList.size());
        } catch (ExcelException e) {
            log.error("导出项目内我的操作日志失败（Excel异常）: {}", e.getMessage(), e);
            throw new RuntimeException("导出项目内我的操作日志失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("导出项目内我的操作日志失败: {}", e.getMessage(), e);
            throw new RuntimeException("导出项目内我的操作日志失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建项目操作日志查询条件
     */
    private Specification<ProjectOperationLog> buildProjectLogSpec(Long projectId, String operationType,
                                                                    String username, LocalDateTime startTime,
                                                                    LocalDateTime endTime) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("projectId"), projectId));

            if (operationType != null && !operationType.isEmpty()) {
                predicates.add(cb.equal(root.get("operationType"), operationType));
            }

            if (username != null && !username.isEmpty()) {
                predicates.add(cb.like(root.get("username"), "%" + username + "%"));
            }

            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(OPERATION_TIME_FIELD), startTime));
            }

            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get(OPERATION_TIME_FIELD), endTime));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 通用方法：根据用户ID和时间范围查询日志
     * 使用泛型消除重复代码
     */
    @SuppressWarnings("unchecked")
    private <T> List<UnifiedOperationLogVO> queryLogsByUserAndTime(LogQueryTask<T> task, Long userId,
                                                                     LocalDateTime startTime, LocalDateTime endTime,
                                                                     int limit) {
        Specification<T> spec = buildUserTimeSpec(task.entityClass, userId, startTime, endTime, task.timeField);
        Sort sort = Sort.by(Sort.Direction.DESC, task.timeField);
        Pageable pageable = PageRequest.of(0, limit, sort);

        Page<T> page = task.repository.findAll(spec, pageable);
        return page.getContent().stream()
                .map(task.mapper)
                .collect(Collectors.toList());
    }

    /**
     * 通用方法：根据用户ID和项目ID查询日志
     * 使用泛型消除重复代码
     */
    @SuppressWarnings("unchecked")
    private <T> List<UnifiedOperationLogVO> queryLogsByUserAndProject(LogQueryTask<T> task, Long userId,
                                                                        Long projectId, int limit) {
        Specification<T> spec = buildUserProjectSpec(task.entityClass, userId, projectId, task.timeField);
        Sort sort = Sort.by(Sort.Direction.DESC, task.timeField);
        Pageable pageable = PageRequest.of(0, limit, sort);

        Page<T> page = task.repository.findAll(spec, pageable);
        return page.getContent().stream()
                .map(task.mapper)
                .collect(Collectors.toList());
    }

    /**
     * 构建用户和时间范围的查询条件（泛型方法）
     */
    private <T> Specification<T> buildUserTimeSpec(Class<T> clazz, Long userId,
                                                    LocalDateTime startTime, LocalDateTime endTime,
                                                    String timeField) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));

            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(timeField), startTime));
            }

            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get(timeField), endTime));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 构建用户和项目的查询条件（泛型方法）
     */
    private <T> Specification<T> buildUserProjectSpec(Class<T> clazz, Long userId, Long projectId, String timeField) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));
            predicates.add(cb.equal(root.get("projectId"), projectId));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 多路归并算法：合并多个已排序的日志列表
     * 由于每个列表已经按时间倒序排序，使用多路归并可以高效合并
     * 时间复杂度：O(n * log(k))，其中n是总元素数，k是列表数量
     *
     * @param sortedLists 已排序的日志列表集合
     * @param limit       最大返回条数
     * @return 合并后的排序列表
     */
    private List<UnifiedOperationLogVO> mergeSortedLogs(List<List<UnifiedOperationLogVO>> sortedLists, int limit) {
        if (sortedLists.isEmpty()) {
            return Collections.emptyList();
        }

        // 如果只有一个列表，直接返回（截取到limit）
        if (sortedLists.size() == 1) {
            List<UnifiedOperationLogVO> singleList = sortedLists.get(0);
            return singleList.size() > limit ? singleList.subList(0, limit) : singleList;
        }

        // 使用优先队列（最小堆）实现多路归并
        // 由于需要按时间倒序（最新的在前），所以使用时间最大的优先
        PriorityQueue<LogIterator> heap = new PriorityQueue<>(
                (a, b) -> b.current().getTime().compareTo(a.current().getTime())
        );

        // 初始化：将每个列表的第一个元素加入堆
        for (List<UnifiedOperationLogVO> list : sortedLists) {
            if (!list.isEmpty()) {
                heap.offer(new LogIterator(list));
            }
        }

        List<UnifiedOperationLogVO> result = new ArrayList<>(Math.min(limit, sortedLists.stream()
                .mapToInt(List::size)
                .sum()));

        // 归并过程
        while (!heap.isEmpty() && result.size() < limit) {
            LogIterator iterator = heap.poll();
            result.add(iterator.current());

            // 如果该迭代器还有下一个元素，继续加入堆
            if (iterator.hasNext()) {
                iterator.next();
                heap.offer(iterator);
            }
        }

        return result;
    }

    /**
     * 日志列表迭代器包装类
     * 用于多路归并算法
     */
    private static class LogIterator {
        private final List<UnifiedOperationLogVO> list;
        private int index;

        LogIterator(List<UnifiedOperationLogVO> list) {
            this.list = list;
            this.index = 0;
        }

        UnifiedOperationLogVO current() {
            return list.get(index);
        }

        boolean hasNext() {
            return index < list.size() - 1;
        }

        void next() {
            index++;
        }
    }

    /**
     * 应用查询限制
     * 如果limit为null或0，使用默认最大值；否则使用指定的limit，但不超过最大值
     *
     * @param limit 用户指定的限制
     * @return 有效的查询限制
     */
    private int applyQueryLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_MAX_QUERY_LIMIT;
        }
        return Math.min(limit, DEFAULT_MAX_QUERY_LIMIT);
    }
}
