package hbnu.project.zhiyancommonredis.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis连接测试工具
 * 应用启动时自动测试Redis连接
 *
 * @author ErgouTree
 */
@Slf4j
@Component
public class RedisConnectionTest implements CommandLineRunner {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private Environment environment;

    @Override
    public void run(String... args) {
        String activeProfile = environment.getProperty("spring.profiles.active", "unknown");
        String redisHost = environment.getProperty("spring.data.redis.host", "unknown");
        String redisPort = environment.getProperty("spring.data.redis.port", "unknown");

        log.info("=================================================");
        log.info("Redis连接测试");
        log.info("=================================================");
        log.info("当前环境: {}", activeProfile);
        log.info("Redis地址: {}:{}", redisHost, redisPort);
        log.info("-------------------------------------------------");

        try {
            // 测试PING命令
            String pong = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();

            log.info("✓ Redis连接成功！");
            log.info("响应: {}", pong);

            // 测试SET和GET命令
            String testKey = "redis:connection:test";
            String testValue = "连接测试成功 - " + System.currentTimeMillis();
            redisTemplate.opsForValue().set(testKey, testValue);
            Object getValue = redisTemplate.opsForValue().get(testKey);

            log.info("✓ Redis读写测试成功！");
            log.info("写入: {} = {}", testKey, testValue);
            log.info("读取: {}", getValue);

            // 清理测试数据
            redisTemplate.delete(testKey);
            log.info("✓ Redis测试数据已清理");

        } catch (Exception e) {
            log.error("=================================================");
            log.error("× Redis连接失败！");
            log.error("=================================================");
            log.error("错误类型: {}", e.getClass().getName());
            log.error("错误信息: {}", e.getMessage());
            log.error("");
            log.error("可能的原因：");
            log.error("1. Redis服务未启动");
            log.error("2. Redis地址或端口配置错误");
            log.error("3. Redis密码错误");
            log.error("4. 网络连接问题（无法访问 {}:{}）", redisHost, redisPort);
            log.error("5. 防火墙阻止连接");
            log.error("");
            log.error("解决方案：");
            
            if ("dev".equals(activeProfile)) {
                log.error("【开发环境】");
                log.error("1. 启动本地Redis：");
                log.error("   Windows: 运行 bin/start-local-redis.bat");
                log.error("   Linux/Mac: 运行 bin/start-local-redis.sh");
                log.error("2. 或使用Docker：");
                log.error("   docker run -d --name redis-dev -p 6379:6379 redis:latest --requirepass zjm10086");
            } else if ("prod".equals(activeProfile)) {
                log.error("【生产环境】");
                log.error("1. 检查网络连通性：");
                log.error("   ping {}", redisHost);
                log.error("   telnet {} {}", redisHost, redisPort);
                log.error("2. 确认Redis服务已启动");
                log.error("3. 检查防火墙规则");
                log.error("4. 如果在本地开发，请切换到dev环境：");
                log.error("   修改 application.yml: spring.profiles.active=dev");
            }
            
            log.error("");
            log.error("详细文档: apifox-tests/Redis连接超时问题解决方案.md");
            log.error("=================================================");
            
            // 开发环境下抛出异常，阻止应用启动
            if ("dev".equals(activeProfile)) {
                log.error("开发环境Redis连接失败，应用将退出");
                throw new RuntimeException("Redis连接失败，请检查Redis服务是否启动", e);
            }
        }

        log.info("=================================================");
    }
}

