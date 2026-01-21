package cn.iocoder.zhgd.module.bpm.service.task;

import cn.iocoder.zhgd.module.bpm.dal.dataobject.task.BpmProcessInstanceBizIndexDO;
import cn.iocoder.zhgd.module.bpm.dal.mysql.task.BpmProcessInstanceBizIndexMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 流程实例业务索引 Service 实现
 */
@Service
public class BpmProcessInstanceBizIndexServiceImpl implements BpmProcessInstanceBizIndexService {

    @Resource
    private BpmProcessInstanceBizIndexMapper bizIndexMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createIndex(String processInstanceId, Long companyId, Long projectId, String startUserId) {
        if (processInstanceId == null) {
            return;
        }
        // 避免重复插入
        if (bizIndexMapper.selectByProcessInstanceId(processInstanceId) != null) {
            return;
        }
        BpmProcessInstanceBizIndexDO indexDO = new BpmProcessInstanceBizIndexDO()
                .setProcessInstanceId(processInstanceId)
                .setCompanyId(companyId)
                .setProjectId(projectId)
                .setStartUserId(startUserId);
        bizIndexMapper.insert(indexDO);
    }

    @Override
    public List<String> listProcessInstanceIds(Long companyId, Long projectId) {
        return bizIndexMapper.selectProcessInstanceIds(companyId, projectId);
    }

}
