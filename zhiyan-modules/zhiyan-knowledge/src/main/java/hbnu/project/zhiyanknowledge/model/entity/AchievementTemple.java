package hbnu.project.zhiyanknowledge.model.entity;

import hbnu.project.zhiyanknowledge.model.dto.AchievementTemplateDTO;
import hbnu.project.zhiyanknowledge.model.dto.CustomAchievementFieldDTO;
import hbnu.project.zhiyanknowledge.model.enums.AchievementType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;

/**
 * 预设模板定义类
 *
 * @author ErgouTree
 */
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class AchievementTemple {
    /**
     * 论文模板
     */
    public static AchievementTemplateDTO getPaperTemplate() {
        List<CustomAchievementFieldDTO> fields = Arrays.asList(
                CustomAchievementFieldDTO.builder()
                        .fieldKey("authors")
                        .fieldLabel("作者")
                        .fieldType("textarea")
                        .required(true)
                        .placeholder("多个作者用逗号分隔")
                        .sortOrder(1)
                        .build(),
                CustomAchievementFieldDTO.builder()
                        .fieldKey("title")
                        .fieldLabel("论文标题")
                        .fieldType("text")
                        .required(true)
                        .placeholder("请输入论文标题")
                        .sortOrder(2)
                        .build(),
                CustomAchievementFieldDTO.builder()
                        .fieldKey("journal")
                        .fieldLabel("期刊名称")
                        .fieldType("text")
                        .required(true)
                        .placeholder("请输入期刊名称")
                        .sortOrder(3)
                        .build(),
                CustomAchievementFieldDTO.builder()
                        .fieldKey("publishYear")
                        .fieldLabel("发表年份")
                        .fieldType("number")
                        .required(true)
                        .sortOrder(4)
                        .build(),
                CustomAchievementFieldDTO.builder()
                        .fieldKey("volume")
                        .fieldLabel("卷号")
                        .fieldType("text")
                        .sortOrder(5)
                        .build(),
                CustomAchievementFieldDTO.builder()
                        .fieldKey("issue")
                        .fieldLabel("期号")
                        .fieldType("text")
                        .sortOrder(6)
                        .build()
        );

        return AchievementTemplateDTO.builder()
                .type(AchievementType.PAPER)
                .templateName("学术论文")
                .description("用于记录学术论文的详细信息")
                .fields(fields)
                .isSystem(true)
                .build();
    }


    /**
     * 专利模板
     */
    public static AchievementTemplateDTO getPatentTemplate() {
        List<CustomAchievementFieldDTO> fields = Arrays.asList(
                CustomAchievementFieldDTO.builder()
                        .fieldKey("patentNo")
                        .fieldLabel("专利号")
                        .fieldType("text")
                        .required(true)
                        .placeholder("请输入专利号")
                        .sortOrder(1)
                        .build(),
                CustomAchievementFieldDTO.builder()
                        .fieldKey("patentType")
                        .fieldLabel("专利类型")
                        .fieldType("select")
                        .required(true)
                        .options(new String[]{"发明专利", "实用新型", "外观设计"})
                        .sortOrder(2)
                        .build(),
                CustomAchievementFieldDTO.builder()
                        .fieldKey("patentName")
                        .fieldLabel("专利名")
                        .fieldType("text")
                        .required(true)
                        .placeholder("请输入专利名")
                        .sortOrder(3)
                        .build(),
                CustomAchievementFieldDTO.builder()
                        .fieldKey("inventors")
                        .fieldLabel("发明人")
                        .fieldType("textarea")
                        .required(true)
                        .placeholder("多个发明人用逗号分隔")
                        .sortOrder(4)
                        .build(),
                CustomAchievementFieldDTO.builder()
                        .fieldKey("applicant")
                        .fieldLabel("申请人")
                        .fieldType("text")
                        .required(true)
                        .sortOrder(4)
                        .build()
        );

        return AchievementTemplateDTO.builder()
                .type(AchievementType.PATENT)
                .templateName("专利")
                .description("用于记录专利的详细信息")
                .fields(fields)
                .isSystem(true)
                .build();
    }


    /**
     * 数据集模板
     */
    public static AchievementTemplateDTO getDatasetTemplate() {
        List<CustomAchievementFieldDTO> fields = Arrays.asList(
                CustomAchievementFieldDTO.builder()
                        .fieldKey("datasetVersion")
                        .fieldLabel("数据集版本")
                        .fieldType("text")
                        .required(true)
                        .defaultValue("v1.0")
                        .sortOrder(1)
                        .build(),
                CustomAchievementFieldDTO.builder()
                        .fieldKey("datasetName")
                        .fieldLabel("数据集名")
                        .fieldType("text")
                        .placeholder("请输入数据集名称")
                        .required(true)
                        .sortOrder(2)
                        .build(),
                CustomAchievementFieldDTO.builder()
                        .fieldKey("dataFormat")
                        .fieldLabel("数据格式")
                        .fieldType("select")
                        .required(true)
                        .options(new String[]{"CSV", "JSON", "XML", "Excel", "数据库", "其他"})
                        .sortOrder(3)
                        .build(),
                CustomAchievementFieldDTO.builder()
                        .fieldKey("dataSize")
                        .fieldLabel("数据规模")
                        .fieldType("text")
                        .placeholder("例如: 十万条级记录")
                        .sortOrder(4)
                        .build(),
                CustomAchievementFieldDTO.builder()
                        .fieldKey("dataSource")
                        .fieldLabel("数据来源")
                        .fieldType("text")
                        .sortOrder(5)
                        .build()
        );

        return AchievementTemplateDTO.builder()
                .type(AchievementType.DATASET)
                .templateName("数据集")
                .description("用于记录数据集的详细信息")
                .fields(fields)
                .isSystem(true)
                .build();
    }


    /**
     * 模型模板
     */
    public static AchievementTemplateDTO getModelTemplate() {
        List<CustomAchievementFieldDTO> fields = Arrays.asList(
                CustomAchievementFieldDTO.builder()
                        .fieldKey("modelFramework")
                        .fieldLabel("模型框架")
                        .fieldType("select")
                        .required(true)
                        .options(new String[]{"TensorFlow", "PyTorch", "Keras", "Scikit-learn", "其他"})
                        .sortOrder(1)
                        .build(),
                CustomAchievementFieldDTO.builder()
                        .fieldKey("modelName")
                        .fieldLabel("模型名称")
                        .fieldType("text")
                        .required(true)
                        .sortOrder(2)
                        .build(),
                CustomAchievementFieldDTO.builder()
                        .fieldKey("modelVersion")
                        .fieldLabel("模型版本")
                        .fieldType("text")
                        .required(true)
                        .defaultValue("v1.0")
                        .sortOrder(3)
                        .build(),
                CustomAchievementFieldDTO.builder()
                        .fieldKey("modelType")
                        .fieldLabel("模型类型")
                        .fieldType("text")
                        .placeholder("例如: CNN, RNN, Transformer等")
                        .sortOrder(4)
                        .build(),
                CustomAchievementFieldDTO.builder()
                        .fieldKey("hyperparameters")
                        .fieldLabel("超参数")
                        .fieldType("textarea")
                        .placeholder("记录重要的超参数设置")
                        .sortOrder(5)
                        .build()
        );

        return AchievementTemplateDTO.builder()
                .type(AchievementType.MODEL)
                .templateName("算法模型")
                .description("用于记录机器学习模型的详细信息")
                .fields(fields)
                .isSystem(true)
                .build();
    }


    /**
     * 报告模板
     */
    public static AchievementTemplateDTO getReportTemplate() {
        List<CustomAchievementFieldDTO> fields = Arrays.asList(
                CustomAchievementFieldDTO.builder()
                        .fieldKey("reportType")
                        .fieldLabel("报告类型")
                        .fieldType("select")
                        .required(true)
                        .options(new String[]{"技术报告", "研究报告", "调研报告", "定期报告", "其他"})
                        .sortOrder(1)
                        .build(),
                CustomAchievementFieldDTO.builder()
                        .fieldKey("modelName")
                        .fieldLabel("报告名称")
                        .fieldType("text")
                        .required(true)
                        .sortOrder(2)
                        .build(),
                CustomAchievementFieldDTO.builder()
                        .fieldKey("reportDate")
                        .fieldLabel("报告日期")
                        .fieldType("date")
                        .required(true)
                        .sortOrder(3)
                        .build()
        );
        return AchievementTemplateDTO.builder()
                .type(AchievementType.REPORT)
                .templateName("报告")
                .description("用于记录各类报告的详细信息")
                .fields(fields)
                .isSystem(true)
                .build();
    }

    /**
     * 自定义类型模板
     * 用户最多可以添加十条自定义字段
     * 默认添加的字段都是文本，都是必填
     */
    public static AchievementTemplateDTO getCustomTemplate() {
        List<CustomAchievementFieldDTO> fields = Arrays.asList(
                CustomAchievementFieldDTO.builder()
                        .fieldKey("customField1")
                        .fieldLabel("自定义字段1")
                        .fieldType("text")
                        .sortOrder(1)
                        .build(),
                CustomAchievementFieldDTO.builder()
                        .fieldKey("customField2")
                        .fieldLabel("自定义字段2")
                        .fieldType("text")
                        .sortOrder(2)
                        .build()
        );

        return AchievementTemplateDTO.builder()
                .type(AchievementType.CUSTOM)
                .templateName("自定义成果")
                .description("用户可以自定义字段结构")
                .fields(fields)
                .isSystem(true)
                .build();
    }

    /**
     * 任务成果模板
     */
    public static AchievementTemplateDTO getTaskResultTemplate() {
        List<CustomAchievementFieldDTO> fields = Arrays.asList(
                CustomAchievementFieldDTO.builder()
                        .fieldKey("taskName")
                        .fieldLabel("任务名称")
                        .fieldType("text")
                        .required(true)
                        .placeholder("请输入任务名称")
                        .sortOrder(1)
                        .build(),
                CustomAchievementFieldDTO.builder()
                        .fieldKey("taskDescription")
                        .fieldLabel("任务描述")
                        .fieldType("textarea")
                        .placeholder("请输入任务描述")
                        .sortOrder(2)
                        .build(),
                CustomAchievementFieldDTO.builder()
                        .fieldKey("completionDate")
                        .fieldLabel("完成日期")
                        .fieldType("date")
                        .required(true)
                        .sortOrder(3)
                        .build(),
                CustomAchievementFieldDTO.builder()
                        .fieldKey("resultSummary")
                        .fieldLabel("成果摘要")
                        .fieldType("textarea")
                        .placeholder("请输入成果摘要")
                        .sortOrder(4)
                        .build()
        );

        return AchievementTemplateDTO.builder()
                .type(AchievementType.TASK_RESULT)
                .templateName("任务成果")
                .description("用于记录任务成果的详细信息")
                .fields(fields)
                .isSystem(true)
                .build();
    }
}
