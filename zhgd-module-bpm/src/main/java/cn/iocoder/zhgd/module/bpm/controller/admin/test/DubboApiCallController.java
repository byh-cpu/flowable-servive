// 包路径：cn.iocoder.zhgd.module.bpm.controller.admin.test
package cn.iocoder.zhgd.module.bpm.controller.admin.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bpm/dubbo") // 自动加/admin-api前缀
public class DubboApiCallController {
    @GetMapping("say-hello")
    public String test() { return "test"; }
}
// 最终接口路径：/admin-api/bpm/dubbo/say-hello