package cn.iocoder.zhgd.module.bpm.framework.flowable.core.candidate.strategy.user;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.zhgd.framework.common.util.number.NumberUtils;
import cn.iocoder.zhgd.framework.common.util.collection.CollectionUtils;
import cn.iocoder.zhgd.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateStrategy;
import cn.iocoder.zhgd.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.zhgd.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * 用户 {@link BpmTaskCandidateStrategy} 实现类
 *
 * @author kyle
 */
@Component
public class BpmTaskCandidateUserStrategy implements BpmTaskCandidateStrategy {

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public BpmTaskCandidateStrategyEnum getStrategy() {
        return BpmTaskCandidateStrategyEnum.USER;
    }

    @Override
    public void validateParam(String param) {
        List<String> userIds = StrUtil.split(param, StrPool.COMMA);
        if (NumberUtils.isAllNumber(userIds)) {
            adminUserApi.validateUserList(CollectionUtils.convertSet(userIds, Long::valueOf));
        }
    }

    @Override
    public LinkedHashSet<String> calculateUsers(String param) {
        List<String> userIds = StrUtil.split(param, StrPool.COMMA);
        return new LinkedHashSet<>(userIds);
    }

}