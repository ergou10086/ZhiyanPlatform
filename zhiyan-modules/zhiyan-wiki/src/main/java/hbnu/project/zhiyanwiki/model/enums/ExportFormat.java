package hbnu.project.zhiyanwiki.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Wiki导出格式枚举
 *
 * @author Tokito
 */
@Getter
@RequiredArgsConstructor
public enum ExportFormat {
    
    /**
     * Markdown格式
     */
    MARKDOWN("markdown", "text/markdown", ".md"),
    
    /**
     * PDF格式
     */
    PDF("pdf", "application/pdf", ".pdf"),
    
    /**
     * Word格式
     */
    WORD("word", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx");
    
    /**
     * 格式名称
     */
    private final String name;
    
    /**
     * MIME类型
     */
    private final String mimeType;
    
    /**
     * 文件扩展名
     */
    private final String extension;
}

