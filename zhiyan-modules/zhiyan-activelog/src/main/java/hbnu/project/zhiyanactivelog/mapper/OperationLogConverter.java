package hbnu.project.zhiyanactivelog.mapper;

import hbnu.project.zhiyanactivelog.model.dto.ProjectLogExportDTO;
import hbnu.project.zhiyanactivelog.model.dto.UnifiedLogExportDTO;
import hbnu.project.zhiyanactivelog.model.entity.*;
import hbnu.project.zhiyanactivelog.model.vo.UnifiedOperationLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 操作日志转换类
 *
 * @author yui
 */
@Component
@RequiredArgsConstructor
public class OperationLogConverter {

    private final OperationLogMapper operationLogMapper;

    public UnifiedOperationLogVO toUnifiedVO(ProjectOperationLog log) {
        return operationLogMapper.mapProject(log);
    }

    public UnifiedOperationLogVO toUnifiedVO(TaskOperationLog log) {
        return operationLogMapper.mapTask(log);
    }

    public UnifiedOperationLogVO toUnifiedVO(WikiOperationLog log) {
        return operationLogMapper.mapWiki(log);
    }

    public UnifiedOperationLogVO toUnifiedVO(AchievementOperationLog log) {
        return operationLogMapper.mapAchievement(log);
    }

    public UnifiedOperationLogVO toUnifiedVO(LoginLog log) {
        return operationLogMapper.mapLogin(log);
    }

    public List<UnifiedOperationLogVO> toUnifiedVOList(List<?> logs) {
        return logs.stream().map(log -> {
            if (log instanceof ProjectOperationLog) {
                return toUnifiedVO((ProjectOperationLog) log);
            } else if (log instanceof TaskOperationLog) {
                return toUnifiedVO((TaskOperationLog) log);
            } else if (log instanceof WikiOperationLog) {
                return toUnifiedVO((WikiOperationLog) log);
            } else if (log instanceof AchievementOperationLog) {
                return toUnifiedVO((AchievementOperationLog) log);
            } else if (log instanceof LoginLog) {
                return toUnifiedVO((LoginLog) log);
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 将项目操作日志转换为导出DTO
     */
    public ProjectLogExportDTO toProjectExportDTO(ProjectOperationLog log) {
        return operationLogMapper.toProjectExportDTO(log);
    }

    /**
     * 将统一日志VO转换为导出DTO
     */
    public UnifiedLogExportDTO toUnifiedExportDTO(UnifiedOperationLogVO vo) {
        return operationLogMapper.toUnifiedExportDTO(vo);
    }

    /**
     * 批量将项目操作日志转换为导出DTO列表
     */
    public List<ProjectLogExportDTO> toProjectExportDTOList(List<ProjectOperationLog> logs) {
        return logs.stream()
                .map(this::toProjectExportDTO)
                .collect(Collectors.toList());
    }

    /**
     * 批量将统一日志VO转换为导出DTO列表
     */
    public List<UnifiedLogExportDTO> toUnifiedExportDTOList(List<UnifiedOperationLogVO> vos) {
        return vos.stream()
                .map(this::toUnifiedExportDTO)
                .collect(Collectors.toList());
    }
}