package hbnu.project.zhiyanactivelog.service;

import hbnu.project.zhiyanactivelog.annotation.BizOperationLog;
import hbnu.project.zhiyanactivelog.model.entity.*;
import hbnu.project.zhiyanactivelog.model.enums.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 给AOP切面类使用的保存业务日志服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogSaveService {

    private final OperationLogService operationLogService;

    /**
     * 保存项目操作日志
     */
    public void saveProjectLog(BizOperationLog annotation, Long projectId, String projectName,
                               Long userId, String username, String ipAddress, String userAgent,
                               LocalDateTime operationTime, OperationResult operationResult,
                               String errorMessage, String requestParams) {

        ProjectOperationLog log = ProjectOperationLog.builder()
                .projectId(projectId)
                .projectName(projectName)
                .userId(userId)
                .username(username)
                .operationType(ProjectOperationType.valueOf(annotation.type()))
                .operationModule(getModuleName(annotation))
                .operationDesc(annotation.description())
                .requestParams(requestParams)
                .operationResult(operationResult)
                .errorMessage(errorMessage)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .operationTime(operationTime)
                .build();

        operationLogService.saveProjectLog(log);
    }

    /**
     * 保存任务操作日志
     */
    public void saveTaskLog(BizOperationLog annotation, Long projectId, Long taskId,
                            String taskTitle, Long userId, String username, String ipAddress,
                            String userAgent, LocalDateTime operationTime,
                            OperationResult operationResult, String errorMessage,
                            String requestParams) {

        TaskOperationLog log = TaskOperationLog.builder()
                .projectId(projectId)
                .taskId(taskId)
                .taskTitle(taskTitle)
                .userId(userId)
                .username(username)
                .operationType(TaskOperationType.valueOf(annotation.type()))
                .operationModule(getModuleName(annotation))
                .operationDesc(annotation.description())
                .requestParams(requestParams)
                .operationResult(operationResult)
                .errorMessage(errorMessage)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .operationTime(operationTime)
                .build();

        operationLogService.saveTaskLog(log);
    }

    /**
     * 保存Wiki操作日志
     */
    public void saveWikiLog(BizOperationLog annotation, Long projectId, Long wikiPageId,
                            String wikiPageTitle, Long userId, String username, String ipAddress,
                            String userAgent, LocalDateTime operationTime,
                            OperationResult operationResult, String errorMessage,
                            String requestParams) {

        WikiOperationLog log = WikiOperationLog.builder()
                .projectId(projectId)
                .wikiPageId(wikiPageId)
                .wikiPageTitle(wikiPageTitle)
                .userId(userId)
                .username(username)
                .operationType(WikiOperationType.valueOf(annotation.type()))
                .operationModule(getModuleName(annotation))
                .operationDesc(annotation.description())
                .requestParams(requestParams)
                .operationResult(operationResult)
                .errorMessage(errorMessage)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .operationTime(operationTime)
                .build();

        operationLogService.saveWikiLog(log);
    }

    /**
     * 保存成果操作日志
     */
    public void saveAchievementLog(BizOperationLog annotation, Long projectId,
                                   Long achievementId, String achievementTitle, Long userId,
                                   String username, String ipAddress, String userAgent,
                                   LocalDateTime operationTime, OperationResult operationResult,
                                   String errorMessage, String requestParams) {

        AchievementOperationLog log = AchievementOperationLog.builder()
                .projectId(projectId)
                .achievementId(achievementId)
                .achievementTitle(achievementTitle)
                .userId(userId)
                .username(username)
                .operationType(AchievementOperationType.valueOf(annotation.type()))
                .operationModule(getModuleName(annotation))
                .operationDesc(annotation.description())
                .requestParams(requestParams)
                .operationResult(operationResult)
                .errorMessage(errorMessage)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .operationTime(operationTime)
                .build();

        operationLogService.saveAchievementLog(log);
    }



    /**
     * 根据模块类型保存日志（统一入口方法）
     */
    public void saveLogByModule(BizOperationLog annotation, Long projectId, Long bizId,
                                String bizTitle, Long userId, String username, String ipAddress,
                                String userAgent, LocalDateTime operationTime,
                                OperationResult operationResult, String errorMessage,
                                String requestParams) {

        switch (annotation.module()) {
            case PROJECT:
                saveProjectLog(annotation, projectId, bizTitle, userId, username,
                        ipAddress, userAgent, operationTime, operationResult,
                        errorMessage, requestParams);
                break;

            case TASK:
                saveTaskLog(annotation, projectId, bizId, bizTitle, userId, username,
                        ipAddress, userAgent, operationTime, operationResult,
                        errorMessage, requestParams);
                break;

            case WIKI:
                saveWikiLog(annotation, projectId, bizId, bizTitle, userId, username,
                        ipAddress, userAgent, operationTime, operationResult,
                        errorMessage, requestParams);
                break;

            case ACHIEVEMENT:
                saveAchievementLog(annotation, projectId, bizId, bizTitle, userId, username,
                        ipAddress, userAgent, operationTime, operationResult,
                        errorMessage, requestParams);
                break;

            default:
                log.warn("未知的业务模块类型: {}", annotation.module());
        }
    }


    /**
     * 获取模块名称
     */
    private String getModuleName(BizOperationLog annotation) {
        if (hbnu.project.zhiyancommonbasic.utils.StringUtils.isNotEmpty(annotation.moduleName())) {
            return annotation.moduleName();
        }
        return annotation.module().displayName();
    }
}
