package hbnu.project.zhiyanactivelog.service;

import hbnu.project.zhiyanactivelog.mapper.OperationLogMapper;
import hbnu.project.zhiyanactivelog.model.entity.*;
import hbnu.project.zhiyanactivelog.model.vo.UnifiedOperationLogVO;
import hbnu.project.zhiyanactivelog.repository.*;
import hbnu.project.zhiyancommonbasic.exception.BusinessException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 操作日志服务（优化版）
 * <p>
 * 优化点：
 * 1. 使用泛型 + 函数式接口抽象公共逻辑，消除重复代码
 * 2. 优化多表合并分页逻辑：先查id+时间（轻量），合并排序后取分页范围，再批量查详情
 * 3. 统一返回类型为 Page<UnifiedOperationLogVO>，增强可读性
 * 4. 完善参数校验，避免无效查询
 * 5. 异常处理规范化，使用 BusinessException
 * 6. 日志打印优化，保存成功时打印关键信息
 * 7. 支持异步保存日志（可选，提升接口响应速度）
 *
 * @author ErgouTree
 * @rewrite yui
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogplusService {

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
        final String source; // 标识来源表（PROJECT/TASK/WIKI/ACHIEVEMENT）

        LogIdTimeInfo(Long id, LocalDateTime time, String source) {
            this.id = id;
            this.time = time;
            this.source = source;
        }
    }

    // ==================== 保存日志（通用方法） ====================

    /**
     * 通用保存日志方法
     * 使用泛型消除重复代码
     *
     * @param logEntity 日志实体
     * @param repository Repository
     * @param logType   日志类型（用于日志打印）
     * @param async     是否异步保存，默认false
     */
    private <T> void saveLog(T logEntity, CrudRepository<T, Long> repository, String logType, boolean async) {
        if (async) {
            saveLogAsync(logEntity, repository, logType);
        } else {
            saveLogSync(logEntity, repository, logType);
        }
    }

    /**
     * 同步保存日志
     */
    @Transactional
    protected <T> void saveLogSync(T logEntity, CrudRepository<T, Long> repository, String logType) {
        try {
            T saved = repository.save(logEntity);
            Long logId = extractId(saved);
            log.info("保存{}成功，日志ID: {}", logType, logId);
        } catch (Exception e) {
            log.error("保存{}失败", logType, e);
            throw new BusinessException("activelog", "SAVE_LOG_FAILED", null,
                    String.format("保存%s失败: %s", logType, e.getMessage()));
        }
    }

    /**
     * 异步保存日志
     * 适用于非核心业务场景，提升接口响应速度
     */
    @Async
    @Transactional
    protected <T> void saveLogAsync(T logEntity, CrudRepository<T, Long> repository, String logType) {
        try {
            T saved = repository.save(logEntity);
            Long logId = extractId(saved);
            log.info("异步保存{}成功，日志ID: {}", logType, logId);
        } catch (Exception e) {
            log.error("异步保存{}失败", logType, e);
            // 异步保存失败不抛出异常，避免影响主流程
        }
    }

    /**
     * 保存项目操作日志
     */
    public void saveProjectLog(ProjectOperationLog projectLog) {
        Assert.notNull(projectLog, "项目操作日志不能为空");
        saveLog(projectLog, projectLogRepository, "项目操作日志", false);
    }

    /**
     * 保存任务操作日志
     */
    public void saveTaskLog(TaskOperationLog taskLog) {
        Assert.notNull(taskLog, "任务操作日志不能为空");
        saveLog(taskLog, taskLogRepository, "任务操作日志", false);
    }

    /**
     * 保存Wiki操作日志
     */
    public void saveWikiLog(WikiOperationLog wikiLog) {
        Assert.notNull(wikiLog, "Wiki操作日志不能为空");
        saveLog(wikiLog, wikiLogRepository, "Wiki操作日志", false);
    }

    /**
     * 保存成果操作日志
     */
    public void saveAchievementLog(AchievementOperationLog achievementLog) {
        Assert.notNull(achievementLog, "成果操作日志不能为空");
        saveLog(achievementLog, achievementLogRepository, "成果操作日志", false);
    }

    /**
     * 保存登录日志
     */
    public void saveLoginLog(LoginLog loginLog) {
        Assert.notNull(loginLog, "登录日志不能为空");
        saveLog(loginLog, loginLogRepository, "登录日志", false);
    }

    /**
     * 异步保存项目操作日志（可选，提升接口响应速度）
     */
    public void saveProjectLogAsync(ProjectOperationLog projectLog) {
        Assert.notNull(projectLog, "项目操作日志不能为空");
        saveLog(projectLog, projectLogRepository, "项目操作日志", true);
    }

    // ==================== 查询项目内日志 ====================

    /**
     * 查询项目内所有类型的操作日志（分页）
     * 用于项目详情页的日志展示
     * <p>
     * 优化后的分页逻辑：
     * 1. 先全量查询各表的id和时间（轻量查询）
     * 2. 合并排序后取分页范围内的数据
     * 3. 再按主键批量查询详情（避免N+1查询）
     *
     * @param projectId 项目ID
     * @param pageable  分页参数
     * @return 统一日志VO分页结果
     */
    public Page<UnifiedOperationLogVO> getProjectAllLogs(Long projectId, Pageable pageable) {
        // 参数校验
        Assert.notNull(projectId, "项目ID不能为空");
        Assert.notNull(pageable, "分页参数不能为空");

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

            // 第一步：轻量查询 - 只查询各表的id和时间
            List<LogIdTimeInfo> allIdTimeInfos = new ArrayList<>();
            for (LogQueryTask<?> task : queryTasks) {
                List<LogIdTimeInfo> idTimeInfos = queryIdAndTimeByProject(task, projectId);
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
            log.error("查询项目内所有操作日志失败，项目ID: {}", projectId, e);
            throw new BusinessException("activelog", "QUERY_PROJECT_LOGS_FAILED", null,
                    String.format("查询项目内所有操作日志失败: %s", e.getMessage()));
        }
    }

    /**
     * 通用方法：查询项目内指定类型的日志（带筛选）
     * 使用泛型消除重复代码
     *
     * @param task         日志查询任务
     * @param projectId    项目ID
     * @param operationType 操作类型
     * @param username     用户名
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @param pageable     分页参数
     * @param extraPredicate 额外的查询条件（如wikiPageId、achievementId），通过函数式接口传入
     * @return 日志分页结果
     */
    private <T> Page<T> queryProjectLogsWithFilter(LogQueryTask<T> task, Long projectId,
                                                    String operationType, String username,
                                                    LocalDateTime startTime, LocalDateTime endTime,
                                                    Pageable pageable,
                                                    Function<Specification<T>, Specification<T>> extraPredicate) {
        // 参数校验
        Assert.notNull(projectId, "项目ID不能为空");
        Assert.notNull(pageable, "分页参数不能为空");

        Specification<T> baseSpec = buildBaseProjectLogSpec(task.entityClass, projectId, operationType,
                username, startTime, endTime, task.timeField);

        // 应用额外的查询条件
        Specification<T> finalSpec = extraPredicate != null ? extraPredicate.apply(baseSpec) : baseSpec;

        Sort sort = Sort.by(Sort.Direction.DESC, task.timeField);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        return task.repository.findAll(finalSpec, sortedPageable);
    }

    /**
     * 查询项目内项目操作日志（带筛选）
     */
    public Page<ProjectOperationLog> getProjectLogs(Long projectId, String operationType, String username,
                                                     LocalDateTime startTime, LocalDateTime endTime,
                                                     Pageable pageable) {
        LogQueryTask<ProjectOperationLog> task = new LogQueryTask<>(
                ProjectOperationLog.class,
                projectLogRepository,
                operationLogMapper::mapProject,
                OPERATION_TIME_FIELD
        );
        return queryProjectLogsWithFilter(task, projectId, operationType, username, startTime, endTime,
                pageable, null);
    }

    /**
     * 查询项目内Wiki操作日志（带筛选）
     */
    public Page<WikiOperationLog> getWikiLogs(Long projectId, Long wikiPageId, String operationType,
                                               String username, LocalDateTime startTime, LocalDateTime endTime,
                                               Pageable pageable) {
        LogQueryTask<WikiOperationLog> task = new LogQueryTask<>(
                WikiOperationLog.class,
                wikiLogRepository,
                operationLogMapper::mapWiki,
                OPERATION_TIME_FIELD
        );

        // 通过函数式接口传入额外的查询条件（wikiPageId）
        Function<Specification<WikiOperationLog>, Specification<WikiOperationLog>> extraPredicate = baseSpec ->
                (root, query, cb) -> {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(baseSpec.toPredicate(root, query, cb));

                    if (wikiPageId != null) {
                        predicates.add(cb.equal(root.get("wikiPageId"), wikiPageId));
                    }

                    return cb.and(predicates.toArray(new Predicate[0]));
                };

        return queryProjectLogsWithFilter(task, projectId, operationType, username, startTime, endTime,
                pageable, extraPredicate);
    }

    /**
     * 查询项目内成果操作日志（带筛选）
     */
    public Page<AchievementOperationLog> getAchievementLogs(Long projectId, Long achievementId,
                                                             String operationType, String username,
                                                             LocalDateTime startTime, LocalDateTime endTime,
                                                             Pageable pageable) {
        LogQueryTask<AchievementOperationLog> task = new LogQueryTask<>(
                AchievementOperationLog.class,
                achievementLogRepository,
                operationLogMapper::mapAchievement,
                OPERATION_TIME_FIELD
        );

        // 通过函数式接口传入额外的查询条件（achievementId）
        Function<Specification<AchievementOperationLog>, Specification<AchievementOperationLog>> extraPredicate = baseSpec ->
                (root, query, cb) -> {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(baseSpec.toPredicate(root, query, cb));

                    if (achievementId != null) {
                        predicates.add(cb.equal(root.get("achievementId"), achievementId));
                    }

                    return cb.and(predicates.toArray(new Predicate[0]));
                };

        return queryProjectLogsWithFilter(task, projectId, operationType, username, startTime, endTime,
                pageable, extraPredicate);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建基础的项目日志查询条件（泛型方法）
     * 包含：项目ID + 操作类型 + 用户名 + 时间范围
     */
    private <T> Specification<T> buildBaseProjectLogSpec(Class<T> clazz, Long projectId, String operationType,
                                                          String username, LocalDateTime startTime,
                                                          LocalDateTime endTime, String timeField) {
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
                predicates.add(cb.greaterThanOrEqualTo(root.get(timeField), startTime));
            }

            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get(timeField), endTime));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 轻量查询：根据项目ID查询id和时间字段
     * 用于多表合并分页的场景
     */
    private <T> List<LogIdTimeInfo> queryIdAndTimeByProject(LogQueryTask<T> task, Long projectId) {
        Specification<T> spec = (root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("projectId"), projectId);
            return predicate;
        };

        // 只查询id和时间字段，减少数据传输
        // 注意：虽然JPA Specification默认查询所有字段，但这里我们只提取id和时间。
        // 如果数据量特别大（百万级），可以考虑使用原生SQL查询优化，只查询id和时间字段。
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
        Iterable<T> entitiesIterable = ((CrudRepository<T, Long>) task.repository).findAllById(ids);
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
     * 提取实体ID（使用反射）
     */
    private <T> Long extractId(T entity) {
        try {
            var method = entity.getClass().getMethod("getId");
            return (Long) method.invoke(entity);
        } catch (Exception e) {
            log.error("提取实体ID失败: {}", entity.getClass().getName(), e);
            throw new BusinessException("activelog", "EXTRACT_ID_FAILED", null, "提取实体ID失败");
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
            throw new BusinessException("activelog", "EXTRACT_TIME_FAILED", null, "提取实体时间字段失败");
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
        }
        throw new BusinessException("activelog", "UNKNOWN_ENTITY_TYPE", null,
                "未知的实体类型: " + task.entityClass.getName());
    }
}

