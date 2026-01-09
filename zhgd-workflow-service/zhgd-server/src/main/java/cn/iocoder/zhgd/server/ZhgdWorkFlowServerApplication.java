package cn.iocoder.zhgd.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 直接写死包名，避免注解解析时机问题
@SpringBootApplication(scanBasePackages = {
        "cn.iocoder.zhgd.server",
        "cn.iocoder.zhgd.module"
})

public class ZhgdWorkFlowServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZhgdWorkFlowServerApplication.class, args);
    }
}