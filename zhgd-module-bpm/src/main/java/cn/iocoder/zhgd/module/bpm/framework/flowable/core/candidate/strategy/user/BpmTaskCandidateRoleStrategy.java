package cn.iocoder.zhgd.module.bpm.framework.flowable.core.candidate.strategy.user;

import cn.iocoder.zhgd.framework.common.util.string.StrUtils;
import cn.iocoder.zhgd.framework.common.util.collection.CollectionUtils;
import cn.iocoder.zhgd.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateStrategy;
import cn.iocoder.zhgd.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import cn.iocoder.zhgd.module.system.api.permission.PermissionApi;
import cn.iocoder.zhgd.module.system.api.permission.RoleApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 角色 {@link BpmTaskCandidateStrategy} 实现类
 *
 * @author kyle
 */
@Component
public class BpmTaskCandidateRoleStrategy implements BpmTaskCandidateStrategy {

    @Resource
    private RoleApi roleApi;
    @Resource
    private PermissionApi permissionApi;

    @Override
    public BpmTaskCandidateStrategyEnum getStrategy() {
        return BpmTaskCandidateStrategyEnum.ROLE;
    }

    @Override
    public void validateParam(String param) {
        Set<Long> roleIds = StrUtils.splitToLongSet(param);
        roleApi.validRoleList(roleIds);
    }

    @Override
    public Set<String> calculateUsers(String param) {
        Set<Long> roleIds = StrUtils.splitToLongSet(param);
        return CollectionUtils.convertSet(permissionApi.getUserRoleIdListByRoleIds(roleIds), String::valueOf);
    }

}