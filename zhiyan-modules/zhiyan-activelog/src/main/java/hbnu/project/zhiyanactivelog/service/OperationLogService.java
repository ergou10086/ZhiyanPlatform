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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志服务
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final ProjectOperationLogRepository projectLogRepository;
    private final TaskOperationLogRepository taskLogRepository;
    private final WikiOperationLogRepository wikiLogRepository;
    private final AchievementOperationLogRepository achievementLogRepository;
    private final LoginLogRepository loginLogRepository;

    private final OperationLogMapper operationLogMapper;

    // ==================== 保存日志 ====================

    /**
     * 保持项目操作日志
     */
    @Transactional
    public void saveProjectLog(ProjectOperationLog projectLog){
        try {
            projectLogRepository.save(projectLog);
        } catch (Exception e) {
            log.error("保存项目操作日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 保存任务操作日志
     */
    @Transactional
    public void saveTaskLog(TaskOperationLog taskLog) {
        try {
            taskLogRepository.save(taskLog);
        } catch (Exception e) {
            log.error("保存任务操作日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 保存Wiki操作日志
     */
    @Transactional
    public void saveWikiLog(WikiOperationLog wikiLog) {
        try {
            wikiLogRepository.save(wikiLog);
        } catch (Exception e) {
            log.error("保存Wiki操作日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 保存成果操作日志
     */
    @Transactional
    public void saveAchievementLog(AchievementOperationLog achievementLog) {
        try {
            achievementLogRepository.save(achievementLog);
        } catch (Exception e) {
            log.error("保存成果操作日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 保存登录日志
     */
    @Transactional
    public void saveLoginLog(LoginLog loginLog) {
        try {
            loginLogRepository.save(loginLog);
        } catch (Exception e) {
            log.error("保存登录日志失败: {}", e.getMessage(), e);
        }
    }

    // ==================== 查询项目内日志 ====================

    /**
     * 查询项目内所有类型的操作日志（分页）
     * 用于项目详情页的日志展示
     */
    public Page<Object> getProjectAllLogs(Long projectId, Pageable pageable) {
        // FinishTODO:这里需要自定义实现联合查询，或者分别查询后合并,我不会写了
        // @Comments 飞舞二狗，虽然 JPA 很难直接跨实体做联合查询，那你再开一个聚合的视图实体不就可以了？然后分别按条件查询各表，映射成刚刚说的统一实体，再合并排序并做分页。

        // 为了尽量保证分页准确性，这里分别从各表按时间倒序取对应页的数据，要从的新取
        // 同时统计总数用于前端分页
        Sort sort = Sort.by(Sort.Direction.DESC, "operationTime");

        // 项目操作日志
        Page<ProjectOperationLog> projectOperationLogPage = projectLogRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("projectId"), projectId),
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort)
        );

        // 任务操作日志
        Page<TaskOperationLog> taskOperationLogPage = taskLogRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("projectId"), projectId),
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort)
        );

        // 成果操作日志
        Page<AchievementOperationLog> achievementOperationLogPage = achievementLogRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("projectId"), projectId),
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort)
        );

        // wiki操作日志
        Page<WikiOperationLog> wikiOperationLogsPage = wikiLogRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("projectId"), projectId),
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort)
        );

        long total = projectOperationLogPage.getTotalElements()
                + taskOperationLogPage.getTotalElements()
                + wikiOperationLogsPage.getTotalElements()
                + achievementOperationLogPage.getTotalElements();

        List<UnifiedOperationLogVO> mergedLog = new ArrayList<>();
        mergedLog.addAll(projectOperationLogPage.getContent().stream().map(operationLogMapper::mapProject).toList());
        mergedLog.addAll(taskOperationLogPage.getContent().stream().map(operationLogMapper::mapTask).toList());
        mergedLog.addAll(wikiOperationLogsPage.getContent().stream().map(operationLogMapper::mapWiki).toList());
        mergedLog.addAll(achievementOperationLogPage.getContent().stream().map(operationLogMapper::mapAchievement).toList());

        // 合并后再按时间倒序
        mergedLog.sort((a, b) -> b.getTime().compareTo(a.getTime()));

        return new PageImpl<>(new ArrayList<>(mergedLog), pageable, total).map(o -> o);
    }

    /**
     * 查询项目内项目操作日志（带筛选）
     */
    public Page<ProjectOperationLog> getProjectLogs(
            Long projectId,
            String operationType,
            String username,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable
    ){
        Specification<ProjectOperationLog> spec = (root,query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("projectId"), projectId));

            if(operationType != null && !operationType.isEmpty()) {
                predicates.add(cb.equal(root.get("operationType"), operationType));
            }

            if(username != null && !username.isEmpty()) {
                predicates.add(cb.equal(root.get("username"), username));
            }

            if(startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("operationTime"), startTime));
            }

            if(endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("operationTime"), endTime));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return projectLogRepository.findAll(spec, pageable);
    }

    /**
     * 查询项目内Wiki操作日志（带筛选）
     */
    public Page<WikiOperationLog> getWikiLogs(
            Long projectId,
            Long wikiPageId,
            String operationType,
            String username,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable) {

        Specification<WikiOperationLog> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("projectId"), projectId));

            if (wikiPageId != null) {
                predicates.add(cb.equal(root.get("wikiPageId"), wikiPageId));
            }

            if (operationType != null && !operationType.isEmpty()) {
                predicates.add(cb.equal(root.get("operationType"), operationType));
            }

            if (username != null && !username.isEmpty()) {
                predicates.add(cb.like(root.get("username"), "%" + username + "%"));
            }

            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("operationTime"), startTime));
            }

            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("operationTime"), endTime));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return wikiLogRepository.findAll(spec, pageable);
    }


    /**
     * 查询项目内成果操作日志（带筛选）
     */
    public Page<AchievementOperationLog> getAchievementLogs(
            Long projectId,
            Long achievementId,
            String operationType,
            String username,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable) {

        Specification<AchievementOperationLog> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("projectId"), projectId));

            if (achievementId != null) {
                predicates.add(cb.equal(root.get("achievementId"), achievementId));
            }

            if (operationType != null && !operationType.isEmpty()) {
                predicates.add(cb.equal(root.get("operationType"), operationType));
            }

            if (username != null && !username.isEmpty()) {
                predicates.add(cb.like(root.get("username"), "%" + username + "%"));
            }

            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("operationTime"), startTime));
            }

            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("operationTime"), endTime));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return achievementLogRepository.findAll(spec, pageable);
    }
}
