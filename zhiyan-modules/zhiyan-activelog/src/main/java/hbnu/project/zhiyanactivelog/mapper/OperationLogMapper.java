package hbnu.project.zhiyanactivelog.mapper;

import hbnu.project.zhiyanactivelog.model.dto.ProjectLogExportDTO;
import hbnu.project.zhiyanactivelog.model.dto.UnifiedLogExportDTO;
import hbnu.project.zhiyanactivelog.model.entity.*;
import hbnu.project.zhiyanactivelog.model.vo.UnifiedOperationLogVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * 操作日志映射接口
 *
 * @author ErgouTree
 */
@Mapper(componentModel = "spring")
public interface OperationLogMapper {

    @Mapping(target = "operationType", source = "operationType", qualifiedByName = "enumName")
    @Mapping(target = "source", constant = "PROJECT")
    @Mapping(target = "relatedId", expression = "java(null)")
    @Mapping(target = "time", source = "operationTime")
    @Mapping(target = "ip", source = "ipAddress")
    @Mapping(target = "title", source = "projectName")
    UnifiedOperationLogVO mapProject(ProjectOperationLog e);

    @Mapping(target = "operationType", source = "operationType", qualifiedByName = "enumName")
    @Mapping(target = "source", constant = "TASK")
    @Mapping(target = "relatedId", source = "taskId")
    @Mapping(target = "time", source = "operationTime")
    @Mapping(target = "ip", source = "ipAddress")
    @Mapping(target = "title", source = "taskTitle")
    UnifiedOperationLogVO mapTask(TaskOperationLog e);

    @Mapping(target = "operationType", source = "operationType", qualifiedByName = "enumName")
    @Mapping(target = "source", constant = "WIKI")
    @Mapping(target = "relatedId", source = "wikiPageId")
    @Mapping(target = "time", source = "operationTime")
    @Mapping(target = "ip", source = "ipAddress")
    @Mapping(target = "title", source = "wikiPageTitle")
    UnifiedOperationLogVO mapWiki(WikiOperationLog e);

    @Mapping(target = "operationType", source = "operationType", qualifiedByName = "enumName")
    @Mapping(target = "source", constant = "ACHIEVEMENT")
    @Mapping(target = "relatedId", source = "achievementId")
    @Mapping(target = "time", source = "operationTime")
    @Mapping(target = "ip", source = "ipAddress")
    @Mapping(target = "title", source = "achievementTitle")
    UnifiedOperationLogVO mapAchievement(AchievementOperationLog e);

    @Mapping(target = "source", constant = "LOGIN")
    @Mapping(target = "operationModule", constant = "登录")
    @Mapping(target = "relatedId", expression = "java(null)")
    @Mapping(target = "projectId", expression = "java(null)")
    @Mapping(target = "title", expression = "java(null)")
    @Mapping(target = "time", source = "loginTime")
    @Mapping(target = "ip", source = "loginIp")
    @Mapping(target = "description", source = "failureReason")
    UnifiedOperationLogVO mapLogin(LoginLog e);

    /**
     * 将项目操作日志转换为导出DTO
     */
    @Mapping(target = "id", source = "id", qualifiedByName = "toStringOrEmpty")
    @Mapping(target = "projectId", source = "projectId", qualifiedByName = "toStringOrEmpty")
    @Mapping(target = "userId", source = "userId", qualifiedByName = "toStringOrEmpty")
    @Mapping(target = "operationType", source = "operationType", qualifiedByName = "enumCode")
    @Mapping(target = "operationResult", source = "operationResult", qualifiedByName = "enumCode")
    ProjectLogExportDTO toProjectExportDTO(ProjectOperationLog log);

    /**
     * 将统一日志VO转换为导出DTO
     */
    @Mapping(target = "id", source = "id", qualifiedByName = "toStringOrEmpty")
    @Mapping(target = "projectId", source = "projectId", qualifiedByName = "toStringOrEmpty")
    @Mapping(target = "userId", source = "userId", qualifiedByName = "toStringOrEmpty")
    @Mapping(target = "relatedId", source = "relatedId", qualifiedByName = "toStringOrEmpty")
    UnifiedLogExportDTO toUnifiedExportDTO(UnifiedOperationLogVO vo);

    @Named("enumName")
    default String enumName(Enum<?> value) {
        return value != null ? value.name() : null;
    }

    @Named("enumCode")
    default String enumCode(Enum<?> value) {
        if (value == null) {
            return "";
        }
        // 假设枚举类型有 getCode() 方法，如果没有请根据实际情况调整
        try {
            // 使用反射调用 getCode() 方法
            var method = value.getClass().getMethod("getCode");
            Object code = method.invoke(value);
            return code != null ? code.toString() : "";
        } catch (Exception e) {
            // 如果枚举没有 getCode() 方法，返回枚举名称
            return value.name();
        }
    }

    @Named("toStringOrEmpty")
    default String toStringOrEmpty(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }
}
