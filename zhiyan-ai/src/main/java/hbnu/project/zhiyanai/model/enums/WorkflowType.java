package hbnu.project.zhiyanai.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * N8N 工作流类型枚举
 *
 * @author ErgouTree
 */
@Getter
@RequiredArgsConstructor
public enum WorkflowType {
    /**
     * RAG 问答工作流
     */
    RAG("rag", "RAG问答工作流", "结合知识库进行检索增强生成"),

    /**
     * 文献检索工作流
     */
    LITERATURE_SEARCH("literature-search", "论文AI赋能工作流", "AI智能搜索文献");

    /**
     * 工作流标识
     */
    private final String code;

    /**
     * 工作流名称
     */
    private final String name;

    /**
     * 工作流描述
     */
    private final String description;
}
