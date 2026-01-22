package cn.iocoder.zhgd.module.bpm.api.task;

import cn.iocoder.zhgd.framework.common.pojo.PageResult;
import cn.iocoder.zhgd.framework.common.util.date.DateUtils;
import cn.iocoder.zhgd.framework.common.util.object.BeanUtils;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmProcessInstancePageReqDTO;
import cn.iocoder.zhgd.module.bpm.api.task.dto.BpmProcessInstanceSimpleRespDTO;
import cn.iocoder.zhgd.module.bpm.controller.admin.task.vo.instance.BpmProcessInstancePageReqVO;
import cn.iocoder.zhgd.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.zhgd.module.bpm.framework.flowable.core.util.FlowableUtils;
import cn.iocoder.zhgd.module.bpm.service.definition.BpmProcessDefinitionService;
import cn.iocoder.zhgd.module.bpm.service.task.BpmProcessInstanceService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static cn.iocoder.zhgd.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.zhgd.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * 流程实例查询 Api 实现类
 */
@Service
@DubboService
public class BpmProcessInstanceQueryApiImpl implements BpmProcessInstanceQueryApi {

    @Resource
    private BpmProcessInstanceService processInstanceService;
    @Resource
    private BpmProcessDefinitionService processDefinitionService;

    @Override
    public PageResult<BpmProcessInstanceSimpleRespDTO> getMyProcessInstancePage(String userId,
                                                                              BpmProcessInstancePageReqDTO pageReqDTO) {
        BpmProcessInstancePageReqVO pageReqVO = BeanUtils.toBean(pageReqDTO, BpmProcessInstancePageReqVO.class);
        PageResult<HistoricProcessInstance> pageResult = processInstanceService.getProcessInstancePage(userId, pageReqVO);
        if (pageResult.getList().isEmpty()) {
            return PageResult.empty();
        }
        Map<String, ProcessDefinition> definitionMap = processDefinitionService.getProcessDefinitionMap(
                convertSet(pageResult.getList(), HistoricProcessInstance::getProcessDefinitionId));
        Map<String, BpmProcessDefinitionInfoDO> definitionInfoMap = processDefinitionService.getProcessDefinitionInfoMap(
                convertSet(pageResult.getList(), HistoricProcessInstance::getProcessDefinitionId));
        List<BpmProcessInstanceSimpleRespDTO> list = convertList(pageResult.getList(),
                instance -> buildProcessInstanceResp(instance, definitionMap, definitionInfoMap));
        return new PageResult<>(list, pageResult.getTotal());
    }

    private BpmProcessInstanceSimpleRespDTO buildProcessInstanceResp(HistoricProcessInstance instance,
                                                                     Map<String, ProcessDefinition> definitionMap,
                                                                     Map<String, BpmProcessDefinitionInfoDO> definitionInfoMap) {
        BpmProcessInstanceSimpleRespDTO resp = new BpmProcessInstanceSimpleRespDTO();
        resp.setId(instance.getId());
        resp.setName(instance.getName());
        resp.setStartTime(DateUtils.of(instance.getStartTime()));
        resp.setEndTime(DateUtils.of(instance.getEndTime()));
        resp.setStatus(FlowableUtils.getProcessInstanceStatus(instance));
        resp.setProcessDefinitionId(instance.getProcessDefinitionId());
        resp.setStartUserId(instance.getStartUserId());
        resp.setFormVariables(FlowableUtils.getProcessInstanceFormVariable(instance));

        ProcessDefinition definition = definitionMap.get(instance.getProcessDefinitionId());
        if (definition != null) {
            resp.setProcessDefinitionKey(definition.getKey());
        }
        BpmProcessDefinitionInfoDO info = definitionInfoMap.get(instance.getProcessDefinitionId());
        if (info != null) {
            resp.setCategory(info.getCategory());
        } else if (definition != null) {
            resp.setCategory(definition.getCategory());
        }
        return resp;
    }

}
