package hbnu.project.zhiyanactivelog.service;

import hbnu.project.zhiyanactivelog.mapper.OperationLogMapper;
import hbnu.project.zhiyanactivelog.model.entity.*;
import hbnu.project.zhiyanactivelog.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import hbnu.project.zhiyanactivelog.model.vo.UnifiedOperationLogVO;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 为我的活动部分提供查询的日志
 * 查询用户在所有项目的操作日志
 * 性能存在问题需要优化
 *
 * @author ErgouTree
 */
@Deprecated
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogMyselfActionService {

    private final ProjectOperationLogRepository projectLogRepository;
    private final TaskOperationLogRepository taskLogRepository;
    private final WikiOperationLogRepository wikiLogRepository;
    private final AchievementOperationLogRepository achievementLogRepository;
    private final LoginLogRepository loginLogRepository;

    private final OperationLogMapper operationLogMapper;

    /**
     * 查询用户在所有项目的所有日志（分页）
     */
    public Page<Object> getProjectAllLogsByMyself(Long userId, Pageable pageable) {
        Sort opSort = Sort.by(Sort.Direction.DESC, "operationTime");
        Sort loginSort = Sort.by(Sort.Direction.DESC, "loginTime");

        Page<ProjectOperationLog> projectOperationLogPage = projectLogRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("userId"), userId),
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), opSort)
        );

        Page<TaskOperationLog> taskOperationLogPage = taskLogRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("userId"), userId),
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), opSort)
        );

        Page<WikiOperationLog> wikiOperationLogPage = wikiLogRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("userId"), userId),
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), opSort)
        );

        Page<AchievementOperationLog> achievementOperationLogPage = achievementLogRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("userId"), userId),
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), opSort)
        );

        Page<LoginLog> loginOperationLogPage = loginLogRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("userId"), userId),
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), loginSort)
        );

        long total = projectOperationLogPage.getTotalElements()
                + taskOperationLogPage.getTotalElements()
                + wikiOperationLogPage.getTotalElements()
                + achievementOperationLogPage.getTotalElements()
                + loginOperationLogPage.getTotalElements();

        List<UnifiedOperationLogVO> merged = new ArrayList<>();
        merged.addAll(projectOperationLogPage.getContent().stream().map(operationLogMapper::mapProject).toList());
        merged.addAll(taskOperationLogPage.getContent().stream().map(operationLogMapper::mapTask).toList());
        merged.addAll(wikiOperationLogPage.getContent().stream().map(operationLogMapper::mapWiki).toList());
        merged.addAll(achievementOperationLogPage.getContent().stream().map(operationLogMapper::mapAchievement).toList());
        merged.addAll(loginOperationLogPage.getContent().stream().map(operationLogMapper::mapLogin).toList());

        merged.sort((a, b) -> b.getTime().compareTo(a.getTime()));

        return new PageImpl<>(new ArrayList<>(merged), pageable, total).map(o -> o);
    }


    /**
     * 查询用户在所有项目的项目操作日志
     */
    public Page<ProjectOperationLog> getUserProjectLogs(
            Long userId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable) {

        Specification<ProjectOperationLog> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("userId"), userId));

            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("operationTime"), startTime));
            }

            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("operationTime"), endTime));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return projectLogRepository.findAll(spec, pageable);
    }

    /**
     * 查询用户在所有项目的任务操作日志
     */
    public Page<TaskOperationLog> getUserTaskLogs(
            Long userId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable) {

        Specification<TaskOperationLog> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("userId"), userId));

            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("operationTime"), startTime));
            }

            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("operationTime"), endTime));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return taskLogRepository.findAll(spec, pageable);
    }

    /**
     * 查询用户在所有项目的Wiki操作日志
     */
    public Page<WikiOperationLog> getUserWikiLogs(
            Long userId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable) {

        Specification<WikiOperationLog> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("userId"), userId));

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
     * 查询用户在所有项目的成果操作日志
     */
    public Page<AchievementOperationLog> getUserAchievementLogs(
            Long userId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable) {

        Specification<AchievementOperationLog> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("userId"), userId));

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

    /**
     * 查询用户的登录日志
     */
    public Page<LoginLog> getUserLoginLogs(
            Long userId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Pageable pageable) {

        Specification<LoginLog> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("userId"), userId));

            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("loginTime"), startTime));
            }

            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("loginTime"), endTime));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return loginLogRepository.findAll(spec, pageable);
    }
}
