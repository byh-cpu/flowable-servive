package cn.iocoder.zhgd.server;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.core.env.ConfigurableEnvironment;

// 直接写死包名，避免注解解析时机问题
@SpringBootApplication(
        scanBasePackages = {"cn.iocoder.zhgd"},
        // 核心修正：只排除符合规范的自动配置类，去掉 RedissonAutoConfiguration
        exclude = {
                RedissonAutoConfigurationV2.class,     // 新版 Redisson 自动配置（唯一需要排除的Redisson类）
                RedisAutoConfiguration.class,          // Spring 核心 Redis 自动配置
                RedisReactiveAutoConfiguration.class   // 响应式 Redis 自动配置（兜底）
        }
)
@EnableDubbo
// 如果有 Mapper 扫描需求，补充 MapperScan
@MapperScan("cn.iocoder.zhgd.**.mapper")
public class ZhgdWorkFlowServerApplication {
    public static void main(String[] args) {
        // 只调用一次 run，避免重复启动
        ConfigurableEnvironment env = SpringApplication.run(ZhgdWorkFlowServerApplication.class, args).getEnvironment();

        System.out.println("======= 配置验证 =======");
        System.out.println("spring.profiles.active: " + env.getProperty("spring.profiles.active"));
        System.out.println("spring.redis.host: " + env.getProperty("spring.redis.host"));
        System.out.println("redisson.singleServerConfig.address: " + env.getProperty("redisson.singleServerConfig.address"));
        // 新增：打印自定义 RedissonClient Bean 是否加载
        //System.out.println("自定义 RedissonConfig 是否存在: " + env.containsBean("redissonClient"));
        System.out.println("========================");
    }
}