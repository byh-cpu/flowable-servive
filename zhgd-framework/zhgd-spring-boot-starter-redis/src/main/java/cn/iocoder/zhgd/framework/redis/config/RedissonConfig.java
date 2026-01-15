package cn.iocoder.zhgd.framework.redis.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.JedisPoolConfig; // 现在能正常导入了

@Configuration
public class RedissonConfig {

    // 第一步：配置 Jedis 连接池（可选，提升性能）
    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(16); // 最大连接数
        poolConfig.setMaxIdle(8);   // 最大空闲连接数
        poolConfig.setMinIdle(2);   // 最小空闲连接数
        poolConfig.setTestOnBorrow(true); // 借连接时测试是否可用
        return poolConfig;
    }

    // 第二步：创建 Redis 连接工厂（基于 Jedis）
    @Bean
    @Primary
    public JedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName("172.16.9.201"); // 目标 Redis IP
        config.setPort(6379);
        config.setDatabase(0);
        // 如果 Redis 有密码，添加
        // config.setPassword("你的 Redis 密码");

        // 关联连接池配置
        JedisConnectionFactory factory = new JedisConnectionFactory(config);
        factory.setPoolConfig(jedisPoolConfig());
        factory.afterPropertiesSet(); // 初始化连接工厂
        return factory;
    }

    // 第三步：创建 StringRedisTemplate
    @Bean
    @Primary
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory());
        return template;
    }

    // 第四步：创建 RedissonClient
    @Bean
    @Primary
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://172.16.9.201:6379")
                .setDatabase(0)
                .setTimeout(5000)
                .setConnectionPoolSize(8)
                .setConnectionMinimumIdleSize(0);

        config.setThreads(16);
        config.setNettyThreads(32);

        try {
            RedissonClient client = Redisson.create(config);
            client.getKeys().count(); // 验证连接
            System.out.println("======= Redisson 手动配置生效 =======");
            //System.out.println("Redis 连接地址: " + config.getSingleServerConfig().getAddress());
            System.out.println("====================================");
            return client;
        } catch (Exception e) {
            System.err.println("======= Redisson 连接失败 =======");
            e.printStackTrace();
            throw new RuntimeException("Redisson 初始化失败", e);
        }
    }
}