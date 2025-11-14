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
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志导出服务
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogExportService {

    private final ProjectOperationLogRepository projectLogRepository;
    private final TaskOperationLogRepository taskLogRepository;
    private final WikiOperationLogRepository wikiLogRepository;
    private final AchievementOperationLogRepository achievementLogRepository;
    private final LoginLogRepository loginLogRepository;
    private final OperationLogMyselfActionService myselfActionService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final OperationLogMapper operationLogMapper;


    /**
     * 导出项目操作日志
     * 可以指定导出的条数
     * 不指定默认全部导出
     */
    public void exportProjectLogs(Long projectId, String operationType, String username,
                                  LocalDateTime startTime, LocalDateTime endTime,
                                  Integer limit, HttpServletResponse response) {
        try {
            // 构建查询条件
            Specification<ProjectOperationLog> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();

                predicates.add(cb.equal(root.get("projectId"), projectId));

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

                return cb.and(predicates.toArray(new Predicate[0]));
            };

            // 按时间倒序排序
            Sort sort = Sort.by(Sort.Direction.DESC, "operationTime");

            // 如果指定了限制条数，则使用限制查询
            List<ProjectOperationLog> logs;
            if (limit != null && limit > 0) {
                Pageable pageable = PageRequest.of(0, limit, sort);
                Page<ProjectOperationLog> page = projectLogRepository.findAll(spec, pageable);
                logs = page.getContent();
            } else {
                // 不限制条数，查询所有
                logs = projectLogRepository.findAll(spec, sort);
            }

            // 转换为导出DTO
            List<ProjectLogExportDTO> exportList = logs.stream()
                    .map(operationLogMapper::toProjectExportDTO)
                    .collect(Collectors.toList());

            // 导出Excel
            ExcelUtil.exportExcel(exportList, "项目操作日志", ProjectLogExportDTO.class, response);

            log.info("导出项目操作日志成功，项目ID: {}, 条数: {}", projectId, exportList.size());
        }catch (ExcelException e){
            log.error("导出项目操作日志失败: {}", e.getMessage(), e);
            throw new RuntimeException("导出项目操作日志失败: " + e.getMessage());
        }
    }


    /**
     * 导出我的操作日志
     * 可以指定导出的条数
     * 不指定默认全部导出
     */
    public void exportMyLogs(Long userId, LocalDateTime startTime, LocalDateTime endTime,
                             Integer limit, HttpServletResponse response) {
        try {
            // 查询所有类型的日志
            Sort optSort = Sort.by(Sort.Direction.DESC, "operationTime");
            Sort loginSort = Sort.by(Sort.Direction.DESC, "loginTime");

            List<UnifiedOperationLogVO> allLogs = new ArrayList<>();

            // 查询项目的操作日志
            Specification<ProjectOperationLog> projectOperationLogSpecification = buildUserTimeSpec(ProjectOperationLog.class, userId, startTime, endTime, "operationTime");
            List<ProjectOperationLog> projectOperationLogs = limit != null && limit > 0
                    ? projectLogRepository.findAll(projectOperationLogSpecification, PageRequest.of(0, limit, optSort)).getContent()
                    : projectLogRepository.findAll(projectOperationLogSpecification, optSort);
            // 加载到全部日志的list
            allLogs.addAll(projectOperationLogs.stream()
                    .map(operationLogMapper::mapProject)
                    .toList());

            // 查询任务操作日志
            Specification<TaskOperationLog> taskOperationLogSpecification = buildUserTimeSpec(TaskOperationLog.class, userId, startTime, endTime, "operationTime");
            List<TaskOperationLog> taskOperationLogs = limit != null && limit > 0
                    ? taskLogRepository.findAll(taskOperationLogSpecification, PageRequest.of(0, limit, optSort)).getContent()
                    : taskLogRepository.findAll(taskOperationLogSpecification, optSort);
            // 加载到全部日志的list
            allLogs.addAll(taskOperationLogs.stream()
                    .map(operationLogMapper::mapTask)
                    .toList());

            // 查询Wiki操作日志
            Specification<WikiOperationLog> wikiOperationLogSpecification = buildUserTimeSpec(WikiOperationLog.class, userId, startTime, endTime, "operationTime");
            List<WikiOperationLog> wikiOperationLogs = limit != null && limit > 0
                    ? wikiLogRepository.findAll(wikiOperationLogSpecification, PageRequest.of(0, limit, optSort)).getContent()
                    : wikiLogRepository.findAll(wikiOperationLogSpecification, optSort);
            // 加载到全部日志的list
            allLogs.addAll(wikiOperationLogs.stream()
                    .map(operationLogMapper::mapWiki)
                    .toList());

            // 查询成果操作日志
            Specification<AchievementOperationLog> achievementOperationLogSpecification = buildUserTimeSpec(AchievementOperationLog.class, userId, startTime, endTime, "operationTime");
            List<AchievementOperationLog> achievementOperationLogs = limit != null && limit > 0
                    ? achievementLogRepository.findAll(achievementOperationLogSpecification, PageRequest.of(0, limit, optSort)).getContent()
                    : achievementLogRepository.findAll(achievementOperationLogSpecification, optSort);
            // 加载到全部日志的list
            allLogs.addAll(achievementOperationLogs.stream()
                    .map(operationLogMapper::mapAchievement)
                    .toList());

            // 查询登录日志
            Specification<LoginLog> loginLogSpecification = buildUserTimeSpec(LoginLog.class, userId, startTime, endTime, "loginTime");
            List<LoginLog> loginLogs = limit != null && limit > 0
                    ? loginLogRepository.findAll(loginLogSpecification, PageRequest.of(0, limit, loginSort)).getContent()
                    : loginLogRepository.findAll(loginLogSpecification, loginSort);
            // 加载到全部日志的list
            allLogs.addAll(loginLogs.stream()
                    .map(operationLogMapper::mapLogin)
                    .toList());

            // 按时间排序
            allLogs.sort((a, b) -> b.getTime().compareTo(a.getTime()));

            // 如果指定了限制的条数，截取前n条
            if(limit != null && limit > 0 && allLogs.size() > limit){
                allLogs = allLogs.subList(0, limit);
            }

            // 转换为导出DTO
            List<UnifiedLogExportDTO> exportList = allLogs.stream().map(operationLogMapper::toUnifiedExportDTO).collect(Collectors.toList());

            // 导出Excel
            ExcelUtil.exportExcel(exportList, "我的操作日志", UnifiedLogExportDTO.class, response);

            log.info("导出我的操作日志成功，用户ID: {}, 条数: {}", userId, exportList.size());
        }catch (ExcelException e){
            log.error("导出我的操作日志失败: {}", e.getMessage(), e);
            throw new RuntimeException("导出我的操作日志失败: " + e.getMessage());
        }
    }


    /**
     * 导出我在指定项目内相关的所有操作日志
     */
    public void exportMyProjectLogs(Long userId, Long projectId, Integer limit, HttpServletResponse response) {
        try{
            Sort optSort = Sort.by(Sort.Direction.DESC, "operationTime");

            List<UnifiedOperationLogVO> allLogs = new ArrayList<>();

            // 查询项目操作日志
            Specification<ProjectOperationLog> projectSpec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("projectId"), projectId));
                predicates.add(cb.equal(root.get("userId"), userId));
                return cb.and(predicates.toArray(new Predicate[0]));
            };
            List<ProjectOperationLog> projectLogs = limit != null && limit > 0
                    ? projectLogRepository.findAll(projectSpec, PageRequest.of(0, limit, optSort)).getContent()
                    : projectLogRepository.findAll(projectSpec, optSort);
            allLogs.addAll(projectLogs.stream().map(operationLogMapper::mapProject).toList());

            // 查询任务操作日志
            Specification<TaskOperationLog> taskOperationLogSpecification = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("projectId"), projectId));
                predicates.add(cb.equal(root.get("userId"), userId));
                return cb.and(predicates.toArray(new Predicate[0]));
            };
            List<TaskOperationLog> taskOperationLogs = limit != null && limit > 0
                    ? taskLogRepository.findAll(taskOperationLogSpecification, PageRequest.of(0, limit, optSort)).getContent()
                    : taskLogRepository.findAll(taskOperationLogSpecification, optSort);
            allLogs.addAll(projectLogs.stream().map(operationLogMapper::mapProject).toList());

            // 查询Wiki操作日志
            Specification<WikiOperationLog> wikiSpec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("projectId"), projectId));
                predicates.add(cb.equal(root.get("userId"), userId));
                return cb.and(predicates.toArray(new Predicate[0]));
            };
            List<WikiOperationLog> wikiLogs = limit != null && limit > 0
                    ? wikiLogRepository.findAll(wikiSpec, PageRequest.of(0, limit, optSort)).getContent()
                    : wikiLogRepository.findAll(wikiSpec, optSort);
            allLogs.addAll(wikiLogs.stream().map(operationLogMapper::mapWiki).toList());

            // 查询成果操作日志
            Specification<AchievementOperationLog> achievementSpec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("projectId"), projectId));
                predicates.add(cb.equal(root.get("userId"), userId));
                return cb.and(predicates.toArray(new Predicate[0]));
            };
            List<AchievementOperationLog> achievementLogs = limit != null && limit > 0
                    ? achievementLogRepository.findAll(achievementSpec, PageRequest.of(0, limit, optSort)).getContent()
                    : achievementLogRepository.findAll(achievementSpec, optSort);
            allLogs.addAll(achievementLogs.stream().map(operationLogMapper::mapAchievement).toList());

            // 按时间倒序排序
            allLogs.sort((a, b) -> b.getTime().compareTo(a.getTime()));

            // 如果指定了限制条数，截取前N条
            if (limit != null && limit > 0 && allLogs.size() > limit) {
                allLogs = allLogs.subList(0, limit);
            }

            // 转换为导出DTO
            List<UnifiedLogExportDTO> exportList = allLogs.stream().map(operationLogMapper::toUnifiedExportDTO).collect(Collectors.toList());

            // 导出Excel
            ExcelUtil.exportExcel(exportList, "项目内我的操作日志", UnifiedLogExportDTO.class, response);

            log.info("导出项目内我的操作日志成功，用户ID: {}, 项目ID: {}, 条数: {}", userId, projectId, exportList.size());
        }catch (ExcelException e){
            log.error("导出项目内我的操作日志失败: {}", e.getMessage(), e);
            throw new RuntimeException("导出项目内我的操作日志失败: " + e.getMessage());
        }
    }



    /**
     * 构建用户和时间范围的查询条件
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
}
