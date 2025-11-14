package hbnu.project.zhiyanactivelog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步配置类
 *
 * @author ErgouTree
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 操作日志专用线程池
     * 独立线程池避免影响业务线程
     */
    @Bean("operationLogTaskExecutor")
    public Executor operationLogTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(4);
        // 最大线程数
        executor.setMaxPoolSize(8);
        // 队列容量
        executor.setQueueCapacity(500);
        // 线程名前缀
        executor.setThreadNamePrefix("log-async-");
        // 空闲线程存活时间
        executor.setKeepAliveSeconds(60);

        // 拒绝策略：由调用线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务完成后关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }
}
