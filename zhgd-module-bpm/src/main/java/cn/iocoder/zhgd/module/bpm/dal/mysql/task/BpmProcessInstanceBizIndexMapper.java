package cn.iocoder.zhgd.module.bpm.dal.mysql.task;

import cn.iocoder.zhgd.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.zhgd.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.zhgd.module.bpm.dal.dataobject.task.BpmProcessInstanceBizIndexDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

import static cn.iocoder.zhgd.framework.common.util.collection.CollectionUtils.convertList;

@Mapper
public interface BpmProcessInstanceBizIndexMapper extends BaseMapperX<BpmProcessInstanceBizIndexDO> {

    default List<String> selectProcessInstanceIds(Long companyId, Long projectId) {
        List<Object> list = selectObjs(new LambdaQueryWrapperX<BpmProcessInstanceBizIndexDO>()
                .eqIfPresent(BpmProcessInstanceBizIndexDO::getCompanyId, companyId)
                .eqIfPresent(BpmProcessInstanceBizIndexDO::getProjectId, projectId)
                .select(BpmProcessInstanceBizIndexDO::getProcessInstanceId));
        return convertList(list, item -> item != null ? item.toString() : null);
    }

    default BpmProcessInstanceBizIndexDO selectByProcessInstanceId(String processInstanceId) {
        return selectOne(BpmProcessInstanceBizIndexDO::getProcessInstanceId, processInstanceId);
    }

}
