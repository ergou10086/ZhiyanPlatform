package hbnu.project.zhiyanactivelog.mapper;

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

    @Mapping(target = "operationType", source = "loginType", qualifiedByName = "enumName")
    @Mapping(target = "source", constant = "LOGIN")
    @Mapping(target = "operationModule", constant = "登录")
    @Mapping(target = "relatedId", expression = "java(null)")
    @Mapping(target = "projectId", expression = "java(null)")
    @Mapping(target = "title", expression = "java(null)")
    @Mapping(target = "time", source = "loginTime")
    @Mapping(target = "ip", source = "loginIp")
    @Mapping(target = "description", source = "failureReason")
    UnifiedOperationLogVO mapLogin(LoginLog e);

    @Named("enumName")
    default String enumName(Enum<?> value) {
        return value != null ? value.name() : null;
    }
}
