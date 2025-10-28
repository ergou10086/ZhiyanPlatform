package hbnu.project.zhiyanknowledge.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hbnu.project.zhiyanknowledge.model.dto.*;
import hbnu.project.zhiyanknowledge.model.entity.Achievement;
import hbnu.project.zhiyanknowledge.model.entity.AchievementDetail;
import hbnu.project.zhiyanknowledge.model.entity.AchievementFile;
import hbnu.project.zhiyanknowledge.model.enums.AchievementStatus;
import hbnu.project.zhiyanknowledge.model.enums.AchievementType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 成果相关实体转换器
 * 使用 MapStruct-Plus 进行对象转换
 *
 * @author ErgouTree
 */
@Mapper(componentModel = "spring")
public abstract class AchievementConverter {

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== Achievement 相关转换 ====================

    /**
     * Achievement 转 AchievementDTO（列表展示用）
     */
    @Mapping(target = "typeName", expression = "java(getTypeName(achievement.getType()))")
    @Mapping(target = "fileCount", expression = "java(getFileCount(achievement))")
    @Mapping(target = "abstractText", expression = "java(getAbstractText(achievement))")
    @Mapping(target = "creatorName", ignore = true) // 需要从auth服务获取
    public abstract AchievementDTO toDTO(Achievement achievement);

    /**
     * Achievement 列表转 AchievementDTO 列表
     */
    public abstract List<AchievementDTO> toDTOList(List<Achievement> achievements);

    /**
     * Achievement的CreateDTO -> Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", defaultValue = "draft")
    @Mapping(target = "detail", ignore = true)
    @Mapping(target = "files", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    public abstract Achievement CreateDTOtoEntity(CreateAchievementDTO dto);

    /**
     * Achievement 转 AchievementDetailDTO（详情展示用）
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "projectId", source = "projectId")
    @Mapping(target = "creatorId", source = "creatorId")
    @Mapping(target = "typeName", expression = "java(getTypeName(achievement.getType()))")
    @Mapping(target = "fileCount", expression = "java(getFileCount(achievement))")
    @Mapping(target = "abstractText", expression = "java(getAbstractFromDetail(achievement))")
    @Mapping(target = "detailData", expression = "java(parseDetailData(achievement.getDetail()))")
    @Mapping(target = "files", source = "files")
    @Mapping(target = "projectName", ignore = true) // 需要从其他服务获取
    @Mapping(target = "creatorName", ignore = true) // 需要从其他服务获取
    @Mapping(target = "tags", ignore = true) // AchievementDetail 中已注释掉 tags 字段
    public abstract AchievementDetailDTO toDetailDTO(Achievement achievement);

    /**
     * CreateAchievementDTO 转 Achievement
     */
    @Mapping(target = "id", ignore = true) // 由雪花算法生成
    @Mapping(target = "status", expression = "java(getStatusOrDefault(dto.getStatus()))")
    @Mapping(target = "detail", ignore = true) // 需要单独处理
    @Mapping(target = "files", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    public abstract Achievement toEntity(CreateAchievementDTO dto);

    /**
     * UpdateAchievementDTO 部分字段更新到 Achievement
     * 注意：实际使用时建议在 Service 层手动处理更新逻辑
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "projectId", ignore = true) // 项目ID不允许修改
    @Mapping(target = "creatorId", ignore = true) // 创建者不允许修改
    @Mapping(target = "detail", ignore = true)
    @Mapping(target = "files", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    public abstract void updateEntityFromDTO(UpdateAchievementDTO dto, @org.mapstruct.MappingTarget Achievement achievement);

    // ==================== AchievementDetail 相关转换 ====================

    /**
     * CreateAchievementDTO 转 AchievementDetail
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "achievementId", ignore = true) // 需要设置
    @Mapping(target = "detailData", expression = "java(mapToJson(dto.getDetailData()))")
    @Mapping(target = "abstractText", source = "abstractText")
    @Mapping(target = "achievement", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    public abstract AchievementDetail createDTOToDetail(CreateAchievementDTO dto);

    // ==================== AchievementFile 相关转换 ====================

    /**
     * AchievementFile 转 AchievementFileDTO
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "achievementId", source = "achievementId")
    @Mapping(target = "uploadBy", source = "uploadBy")
    @Mapping(target = "fileSizeFormatted", expression = "java(formatFileSize(file.getFileSize()))")
    @Mapping(target = "fileUrl", source = "minioUrl")
    @Mapping(target = "uploaderName", ignore = true) // 需要从auth服务获取
    public abstract AchievementFileDTO fileToDTO(AchievementFile file);

    /**
     * AchievementFile 列表转 AchievementFileDTO 列表
     * 批量转换文件列表
     */
    public abstract List<AchievementFileDTO> fileToDTOList(List<AchievementFile> files);

    /**
     * UploadFileDTO 转 AchievementFile
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fileName", ignore = true) // 从 MultipartFile 获取
    @Mapping(target = "fileSize", ignore = true)
    @Mapping(target = "fileType", ignore = true)
    @Mapping(target = "bucketName", ignore = true)
    @Mapping(target = "objectKey", ignore = true)
    @Mapping(target = "minioUrl", ignore = true)
    @Mapping(target = "uploadAt", ignore = true)
    @Mapping(target = "achievement", ignore = true)
    public abstract AchievementFile uploadDTOToFile(UploadFileDTO dto);


    // ==================== Page 转换 ====================
    /**
     * Page<Entity> -> PageResultDTO<DTO>
     */
    protected PageResultDTO<AchievementDTO> toPageDTO(Page<Achievement> page) {
        if (page == null) {
            return PageResultDTO.<AchievementDTO>builder().build();
        }

        return PageResultDTO.<AchievementDTO>builder()
                .content(page.getContent().stream()
                        .map(this::toDTO)
                        .collect(Collectors.toList()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .empty(page.isEmpty())
                .build();
    }


    // ==================== 辅助方法 ====================
    /**
     * 截取摘要前200字符
     */
    protected String truncateAbstract(AchievementDetail detail) {
        if (detail == null || detail.getAbstractText() == null) {
            return null;
        }
        String abstractText = detail.getAbstractText();
        return abstractText.length() > 200
                ? abstractText.substring(0, 200) + "..."
                : abstractText;
    }

    /**
     * 获取类型中文名称
     */
    @Named("getTypeName")
    protected String getTypeName(AchievementType type) {
        if (type == null) {
            return "";
        }
        // 直接使用枚举的getName()方法
        return type.getName();
    }

    /**
     * 获取文件数量
     */
    @Named("getFileCount")
    protected Integer getFileCount(Achievement achievement) {
        if (achievement == null || achievement.getFiles() == null) {
            return 0;
        }
        return achievement.getFiles().size();
    }

    /**
     * 从 Achievement 的 Detail 获取摘要（用于列表展示，截取前200字符）
     */
    @Named("getAbstractText")
    protected String getAbstractText(Achievement achievement) {
        if (achievement == null || achievement.getDetail() == null) {
            return null;
        }
        String abstractText = achievement.getDetail().getAbstractText();
        if (abstractText != null && abstractText.length() > 200) {
            return abstractText.substring(0, 200) + "...";
        }
        return abstractText;
    }

    /**
     * 从 Achievement 的 Detail 获取完整摘要
     */
    @Named("getAbstractFromDetail")
    protected String getAbstractFromDetail(Achievement achievement) {
        if (achievement == null || achievement.getDetail() == null) {
            return null;
        }
        return achievement.getDetail().getAbstractText();
    }

    /**
     * 解析 JSON 字符串为 Map
     */
    @Named("parseDetailData")
    protected Map<String, Object> parseDetailData(AchievementDetail detail) {
        if (detail == null || detail.getDetailData() == null) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(
                    detail.getDetailData(),
                    new TypeReference<>() {
                    }
            );
        } catch (JsonProcessingException e) {
            return Collections.emptyMap();
        }
    }

    /**
     * 将 Map 转换为 JSON 字符串
     */
    @Named("mapToJson")
    protected String mapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    /**
     * 格式化文件大小
     */
    @Named("formatFileSize")
    protected String formatFileSize(Long size) {
        if (size == null || size == 0) {
            return "0 B";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * 获取状态或默认值
     */
    @Named("getStatusOrDefault")
    protected AchievementStatus getStatusOrDefault(AchievementStatus status) {
        return status != null ? status : AchievementStatus.draft;
    }
}