package hbnu.project.zhiyanproject.utils.message;

import hbnu.project.zhiyanproject.model.entity.Tasks;
import hbnu.project.zhiyanproject.repository.TaskRepository;
import hbnu.project.zhiyanproject.repository.TaskUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaskReminderService {

    private final TaskRepository taskRepository;
    private final TaskUserRepository taskUserRepository;
    private final TaskMessageUtils taskMessageUtils;

    /**
     * 发送任务到期提醒（每天执行）
     */
    @Scheduled(cron = "0 0 9 * * ?") // 每天上午9点执行
    public void sendDeadlineReminders() {
        LocalDate threeDaysLater = LocalDate.now().plusDays(3);
        List<Tasks> upcomingTasks = taskRepository.findUpcomingTasks(threeDaysLater);

        for (Tasks task : upcomingTasks) {
            List<Long> assigneeIds = taskUserRepository.findActiveExecutorsByTaskId(task.getId())
                    .stream()
                    .map(taskUser -> taskUser.getUserId())
                    .collect(Collectors.toList());
            if (!assigneeIds.isEmpty()) {
                taskMessageUtils.sendTaskDeadlineReminder(task, assigneeIds);
            }
        }
    }

    /**
     * 发送任务逾期警告（每天执行）
     */
    @Scheduled(cron = "0 0 10 * * ?") // 每天上午10点执行
    public void sendOverdueWarnings() {
        LocalDate today = LocalDate.now();
        List<Tasks> overdueTasks = taskRepository.findOverdueTasks(today);

        for (Tasks task : overdueTasks) {
            List<Long> assigneeIds = taskUserRepository.findActiveExecutorsByTaskId(task.getId())
                    .stream()
                    .map(taskUser -> taskUser.getUserId())
                    .collect(Collectors.toList());
            if (!assigneeIds.isEmpty()) {
                taskMessageUtils.sendTaskOverdueWarning(task, assigneeIds);
            }
        }
    }
}
