package hbnu.project.zhiyanwiki.model.enums;

/**
 * Wiki页面类型枚举
 * 用于区分目录节点和文档节点
 *
 * @author ErgouTree
 */
public enum PageType {
    /**
     * 目录节点（不包含实际文档内容）
     * 只用于组织结构，可以包含子目录和子文档
     */
    DIRECTORY("目录"),

    /**
     * 文档节点（包含Markdown文档内容）
     * 存储实际的文档内容，不能再包含子节点
     */
    DOCUMENT("文档");

    private final String description;

    PageType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
