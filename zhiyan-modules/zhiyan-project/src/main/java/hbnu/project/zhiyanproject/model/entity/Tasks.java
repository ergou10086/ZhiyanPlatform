package hbnu.project.zhiyanproject.model.entity;

import com.fasterxml.jackson.annotation.JsonRawValue;
import hbnu.project.zhiyancommonbasic.annotation.LongToString;
import hbnu.project.zhiyancommonbasic.domain.BaseAuditEntity;
import hbnu.project.zhiyancommonbasic.utils.id.SnowflakeIdUtil;
import hbnu.project.zhiyanproject.model.enums.TaskPriority;
import hbnu.project.zhiyanproject.model.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;

/**
 * 任务实体类
 *
 * @author ErgouTree
 */
@Entity
@Table(name = "tasks")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Tasks extends BaseAuditEntity {
}
