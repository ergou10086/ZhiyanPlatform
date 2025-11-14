package hbnu.project.zhiyanactivelog.service;

import hbnu.project.zhiyanactivelog.mapper.OperationLogMapper;
import hbnu.project.zhiyanactivelog.model.entity.*;
import hbnu.project.zhiyanactivelog.model.vo.UnifiedOperationLogVO;
import hbnu.project.zhiyanactivelog.repository.*;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 为我的活动部分提供查询的日志服务（优化）
 * <p>
 * 优化点：
 * 1. 使用泛型 + 函数式接口抽象公共查询逻辑，消除重复代码
 * 2. 优化多表合并分页逻辑：先查id+时间（轻量），合并排序后取分页范围，再批量查详情
 * 3. 统一返回类型为 Page<UnifiedOperationLogVO>，增强可读性
 * 4. 添加参数校验，避免无效查询
 * 5. 统一排序参数处理，通过timeFieldName区分不同表的时间字段
 * 6. 避免 N+1 查询问题，使用批量查询优化
 *
 * @author ErgouTree
 * @rewrite yui
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogMyselfActionplusService {

    private final ProjectOperationLogRepository projectLogRepository;
    private final TaskOperationLogRepository taskLogRepository;
    private final WikiOperationLogRepository wikiLogRepository;
    private final AchievementOperationLogRepository achievementLogRepository;
    private final LoginLogRepository loginLogRepository;
    private final OperationLogMapper operationLogMapper;

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
     * 日志ID和时间信息
     * 用于轻量查询，只查询主键和时间字段
     */
    private static class LogIdTimeInfo {
        final Long id;
        final LocalDateTime time;
        final String source; // 标识来源表（PROJECT/TASK/WIKI/ACHIEVEMENT/LOGIN）

        LogIdTimeInfo(Long id, LocalDateTime time, String source) {
            this.id = id;
            this.time = time;
            this.source = source;
        }
    }

    /**
     * 查询用户在所有项目的所有日志（分页）
     * <p>
     * 优化后的分页逻辑：
     * 1. 先全量查询各表的id和时间（轻量查询）
     * 2. 合并排序后取分页范围内的数据
     * 3. 再按主键批量查询详情（避免N+1查询）
     *
     * @param userId   用户ID
     * @param pageable 分页参数
     * @return 统一日志VO分页结果
     */
    public Page<UnifiedOperationLogVO> getProjectAllLogsByMyself(Long userId, Pageable pageable) {
        // 参数校验
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(pageable, "分页参数不能为空");

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

            // 第一步：轻量查询 - 只查询各表的id和时间
            List<LogIdTimeInfo> allIdTimeInfos = new ArrayList<>();
            for (LogQueryTask<?> task : queryTasks) {
                List<LogIdTimeInfo> idTimeInfos = queryIdAndTime(task, userId);
                allIdTimeInfos.addAll(idTimeInfos);
            }

            // 第二步：合并排序（按时间倒序）
            allIdTimeInfos.sort((a, b) -> b.time.compareTo(a.time));

            // 第三步：计算总数和分页范围
            long total = allIdTimeInfos.size();
            int pageNumber = pageable.getPageNumber();
            int pageSize = pageable.getPageSize();
            int start = pageNumber * pageSize;
            int end = Math.min(start + pageSize, allIdTimeInfos.size());

            // 如果超出范围，返回空结果
            if (start >= total) {
                return new PageImpl<>(Collections.emptyList(), pageable, total);
            }

            // 第四步：取分页范围内的id列表
            List<LogIdTimeInfo> pagedIdTimeInfos = allIdTimeInfos.subList(start, end);

            // 第五步：按来源分组，批量查询详情
            Map<String, List<Long>> idsBySource = pagedIdTimeInfos.stream()
                    .collect(Collectors.groupingBy(
                            info -> info.source,
                            Collectors.mapping(info -> info.id, Collectors.toList())
                    ));

            // 第六步：批量查询详情并映射
            List<UnifiedOperationLogVO> result = new ArrayList<>();
            for (LogQueryTask<?> task : queryTasks) {
                String source = getSourceByTask(task);
                List<Long> ids = idsBySource.get(source);
                if (ids != null && !ids.isEmpty()) {
                    List<UnifiedOperationLogVO> vos = batchQueryByIds(task, ids);
                    result.addAll(vos);
                }
            }

            // 第七步：按时间倒序排序（因为批量查询后顺序可能被打乱）
            result.sort((a, b) -> b.getTime().compareTo(a.getTime()));

            return new PageImpl<>(result, pageable, total);
        } catch (Exception e) {
            log.error("查询用户所有操作日志失败，用户ID: {}", userId, e);
            throw new RuntimeException("查询用户所有操作日志失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通用方法：查询用户在所有项目的指定类型日志（带时间范围筛选）
     * 使用泛型消除重复代码
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param pageable  分页参数
     * @param task      日志查询任务
     * @return 日志分页结果
     */
    private <T> Page<T> queryUserLogsByTimeRange(LogQueryTask<T> task, Long userId,
                                                  LocalDateTime startTime, LocalDateTime endTime,
                                                  Pageable pageable) {
        // 参数校验
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(pageable, "分页参数不能为空");

        Specification<T> spec = buildUserTimeSpec(task.entityClass, userId, startTime, endTime, task.timeField);
        Sort sort = Sort.by(Sort.Direction.DESC, task.timeField);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        return task.repository.findAll(spec, sortedPageable);
    }

    /**
     * 查询用户在所有项目的项目操作日志
     */
    public Page<ProjectOperationLog> getUserProjectLogs(Long userId, LocalDateTime startTime,
                                                         LocalDateTime endTime, Pageable pageable) {
        LogQueryTask<ProjectOperationLog> task = new LogQueryTask<>(
                ProjectOperationLog.class,
                projectLogRepository,
                operationLogMapper::mapProject,
                OPERATION_TIME_FIELD
        );
        return queryUserLogsByTimeRange(task, userId, startTime, endTime, pageable);
    }

    /**
     * 查询用户在所有项目的任务操作日志
     */
    public Page<TaskOperationLog> getUserTaskLogs(Long userId, LocalDateTime startTime,
                                                   LocalDateTime endTime, Pageable pageable) {
        LogQueryTask<TaskOperationLog> task = new LogQueryTask<>(
                TaskOperationLog.class,
                taskLogRepository,
                operationLogMapper::mapTask,
                OPERATION_TIME_FIELD
        );
        return queryUserLogsByTimeRange(task, userId, startTime, endTime, pageable);
    }

    /**
     * 查询用户在所有项目的Wiki操作日志
     */
    public Page<WikiOperationLog> getUserWikiLogs(Long userId, LocalDateTime startTime,
                                                   LocalDateTime endTime, Pageable pageable) {
        LogQueryTask<WikiOperationLog> task = new LogQueryTask<>(
                WikiOperationLog.class,
                wikiLogRepository,
                operationLogMapper::mapWiki,
                OPERATION_TIME_FIELD
        );
        return queryUserLogsByTimeRange(task, userId, startTime, endTime, pageable);
    }

    /**
     * 查询用户在所有项目的成果操作日志
     */
    public Page<AchievementOperationLog> getUserAchievementLogs(Long userId, LocalDateTime startTime,
                                                                 LocalDateTime endTime, Pageable pageable) {
        LogQueryTask<AchievementOperationLog> task = new LogQueryTask<>(
                AchievementOperationLog.class,
                achievementLogRepository,
                operationLogMapper::mapAchievement,
                OPERATION_TIME_FIELD
        );
        return queryUserLogsByTimeRange(task, userId, startTime, endTime, pageable);
    }

    /**
     * 查询用户的登录日志
     */
    public Page<LoginLog> getUserLoginLogs(Long userId, LocalDateTime startTime,
                                           LocalDateTime endTime, Pageable pageable) {
        LogQueryTask<LoginLog> task = new LogQueryTask<>(
                LoginLog.class,
                loginLogRepository,
                operationLogMapper::mapLogin,
                LOGIN_TIME_FIELD
        );
        return queryUserLogsByTimeRange(task, userId, startTime, endTime, pageable);
    }

    /**
     * 轻量查询：只查询id和时间字段
     * 用于多表合并分页的场景
     * <p>
     * 注意：虽然JPA Specification默认查询所有字段，但这里我们只提取id和时间。
     * 如果数据量特别大（百万级），可以考虑使用原生SQL查询优化，只查询id和时间字段。
     */
    private <T> List<LogIdTimeInfo> queryIdAndTime(LogQueryTask<T> task, Long userId) {
        Specification<T> spec = (root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("userId"), userId);
            return predicate;
        };

        // 只查询id和时间字段，减少数据传输
        // 注意：JPA Specification 默认查询所有字段，这里我们查询所有记录后只提取id和时间
        // 如果数据量特别大，可以考虑使用原生SQL查询优化
        List<T> entities = task.repository.findAll(spec, Sort.by(Sort.Direction.DESC, task.timeField));

        String source = getSourceByTask(task);
        return entities.stream()
                .map(entity -> {
                    Long id = extractId(entity);
                    LocalDateTime time = extractTime(entity, task.timeField);
                    return new LogIdTimeInfo(id, time, source);
                })
                .collect(Collectors.toList());
    }

    /**
     * 批量查询详情：根据id列表批量查询实体
     * 避免N+1查询问题
     */
    @SuppressWarnings("unchecked")
    private <T> List<UnifiedOperationLogVO> batchQueryByIds(LogQueryTask<T> task, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        // 使用findAllById批量查询，避免N+1问题
        Iterable<T> entitiesIterable = ((org.springframework.data.repository.CrudRepository<T, Long>) task.repository)
                .findAllById(ids);
        List<T> entities = new ArrayList<>();
        entitiesIterable.forEach(entities::add);

        // 保持原有顺序（按ids的顺序）
        Map<Long, T> entityMap = entities.stream()
                .collect(Collectors.toMap(this::extractId, Function.identity()));

        return ids.stream()
                .map(entityMap::get)
                .filter(Objects::nonNull)
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
     * 提取实体ID（使用反射）
     */
    private <T> Long extractId(T entity) {
        try {
            var method = entity.getClass().getMethod("getId");
            return (Long) method.invoke(entity);
        } catch (Exception e) {
            log.error("提取实体ID失败: {}", entity.getClass().getName(), e);
            throw new RuntimeException("提取实体ID失败", e);
        }
    }

    /**
     * 提取实体时间字段（使用反射）
     */
    private <T> LocalDateTime extractTime(T entity, String timeField) {
        try {
            // 根据字段名获取getter方法
            String methodName = "get" + timeField.substring(0, 1).toUpperCase() + timeField.substring(1);
            var method = entity.getClass().getMethod(methodName);
            return (LocalDateTime) method.invoke(entity);
        } catch (Exception e) {
            log.error("提取实体时间字段失败: {}, 字段: {}", entity.getClass().getName(), timeField, e);
            throw new RuntimeException("提取实体时间字段失败", e);
        }
    }

    /**
     * 根据任务获取来源标识
     */
    private String getSourceByTask(LogQueryTask<?> task) {
        if (task.entityClass == ProjectOperationLog.class) {
            return "PROJECT";
        } else if (task.entityClass == TaskOperationLog.class) {
            return "TASK";
        } else if (task.entityClass == WikiOperationLog.class) {
            return "WIKI";
        } else if (task.entityClass == AchievementOperationLog.class) {
            return "ACHIEVEMENT";
        } else if (task.entityClass == LoginLog.class) {
            return "LOGIN";
        }
        throw new IllegalArgumentException("未知的实体类型: " + task.entityClass.getName());
    }
}

