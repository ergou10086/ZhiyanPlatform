package hbnu.project.zhiyanwiki.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Wiki附件类型枚举
 *
 * @author Tokito
 */
@Getter
@RequiredArgsConstructor
public enum AttachmentType {
    
    /**
     * 图片类型（jpg/png/gif/webp等）
     * 用于Wiki内容中的图片展示
     */
    IMAGE("图片"),
    
    /**
     * 普通文件（pdf/doc/zip/txt等）
     * 用于Wiki的附件下载
     */
    FILE("文件");
    
    /**
     * 类型描述
     */
    private final String description;
}


