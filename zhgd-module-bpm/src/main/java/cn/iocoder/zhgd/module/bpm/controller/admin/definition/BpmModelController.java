package cn.iocoder.zhgd.module.bpm.controller.admin.definition;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.iocoder.zhgd.framework.common.pojo.CommonResult;
import cn.iocoder.zhgd.module.bpm.controller.admin.definition.vo.model.*;
import cn.iocoder.zhgd.module.bpm.controller.admin.definition.vo.model.*;
import cn.iocoder.zhgd.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelNodeVO;
import cn.iocoder.zhgd.module.bpm.controller.admin.definition.vo.model.simple.BpmSimpleModelUpdateReqVO;
import cn.iocoder.zhgd.module.bpm.convert.definition.BpmModelConvert;
import cn.iocoder.zhgd.module.bpm.dal.dataobject.definition.BpmCategoryDO;
import cn.iocoder.zhgd.module.bpm.dal.dataobject.definition.BpmFormDO;
import cn.iocoder.zhgd.module.bpm.service.definition.BpmCategoryService;
import cn.iocoder.zhgd.module.bpm.service.definition.BpmFormService;
import cn.iocoder.zhgd.module.bpm.service.definition.BpmModelService;
import cn.iocoder.zhgd.module.bpm.service.definition.BpmProcessDefinitionService;
import cn.iocoder.zhgd.module.system.api.dept.DeptApi;
import cn.iocoder.zhgd.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.zhgd.module.system.api.user.AdminUserApi;
import cn.iocoder.zhgd.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static cn.iocoder.zhgd.framework.common.pojo.CommonResult.success;
import static cn.iocoder.zhgd.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.zhgd.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 流程模型")
@RestController
@RequestMapping("/bpm/model")
@Validated
public class BpmModelController {

    @Resource
    private BpmModelService modelService;
    @Resource
    private BpmFormService formService;
    @Resource
    private BpmCategoryService categoryService;
    @Resource
    private BpmProcessDefinitionService processDefinitionService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @GetMapping("/list")
    @Operation(summary = "获得模型分页")
    @Parameter(name = "name", description = "模型名称", example = "芋艿")
    public CommonResult<List<BpmModelRespVO>> getModelList(@RequestParam(value = "name", required = false) String name) {
        List<Model> list = modelService.getModelList(name);
        if (CollUtil.isEmpty(list)) {
            return success(Collections.emptyList());
        }

        // 获得 Form 表单
        Set<Long> formIds = convertSet(list, model -> {
            BpmModelMetaInfoVO metaInfo = BpmModelConvert.INSTANCE.parseMetaInfo(model);
            return metaInfo != null && metaInfo.getFormId() != null ? metaInfo.getFormId() : null;
        });
        formIds.remove(null);
        Map<Long, BpmFormDO> formMap = formService.getFormMap(formIds);
        // 获得 Category Map
        Map<String, BpmCategoryDO> categoryMap = categoryService.getCategoryMap(
                convertSet(list, Model::getCategory));
        // 获得 Deployment Map
        Map<String, Deployment> deploymentMap = processDefinitionService.getDeploymentMap(
                convertSet(list, Model::getDeploymentId));
        // 获得 ProcessDefinition Map
        List<ProcessDefinition> processDefinitions = processDefinitionService.getProcessDefinitionListByDeploymentIds(
                deploymentMap.keySet());
        Map<String, ProcessDefinition> processDefinitionMap = convertMap(processDefinitions, ProcessDefinition::getDeploymentId);
        // 获得 User Map、Dept Map
        Set<Long> userIds = convertSetByFlatMap(list, model -> {
            BpmModelMetaInfoVO metaInfo = BpmModelConvert.INSTANCE.parseMetaInfo(model);
            return metaInfo != null
                    ? metaInfo.getStartUserIds().stream()
                        .map(userId -> NumberUtil.parseLong(userId, null))
                        .filter(Objects::nonNull)
                    : Stream.empty();
        });
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        Set<Long> deptIds = convertSetByFlatMap(list, model -> {
            BpmModelMetaInfoVO metaInfo = BpmModelConvert.INSTANCE.parseMetaInfo(model);
            return metaInfo != null && metaInfo.getStartDeptIds() != null ? metaInfo.getStartDeptIds().stream() : Stream.empty();
        });
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(deptIds);
        return success(BpmModelConvert.INSTANCE.buildModelList(list,
                formMap, categoryMap, deploymentMap, processDefinitionMap, userMap, deptMap));
    }

    @GetMapping("/get")
    @Operation(summary = "获得模型")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('bpm:model:query')")
    public CommonResult<BpmModelRespVO> getModel(@RequestParam("id") String id) {
        Model model = modelService.getModel(id);
        if (model == null) {
            return null;
        }
        byte[] bpmnBytes = modelService.getModelBpmnXML(id);
        BpmSimpleModelNodeVO simpleModel = modelService.getSimpleModel(id);
        return success(BpmModelConvert.INSTANCE.buildModel(model, bpmnBytes, simpleModel));
    }

    @PostMapping("/create")
    @Operation(summary = "新建模型")
    @PreAuthorize("@ss.hasPermission('bpm:model:create')")
    public CommonResult<String> createModel(@Valid @RequestBody BpmModelSaveReqVO createRetVO) {
        return success(modelService.createModel(createRetVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改模型")
    @PreAuthorize("@ss.hasPermission('bpm:model:update')")
    public CommonResult<Boolean> updateModel(@Valid @RequestBody BpmModelSaveReqVO modelVO) {
        String loginUserId = getLoginUserId();
        modelService.updateModel(loginUserId, modelVO);
        return success(true);
    }

    @PutMapping("/update-sort-batch")
    @Operation(summary = "批量修改模型排序")
    @Parameter(name = "ids", description = "编号数组", required = true, example = "1,2,3")
    public CommonResult<Boolean> updateModelSortBatch(@RequestParam("ids") List<String> ids) {
        String loginUserId = getLoginUserId();
        modelService.updateModelSortBatch(loginUserId, ids);
        return success(true);
    }

    @PostMapping("/deploy")
    @Operation(summary = "部署模型")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('bpm:model:deploy')")
    public CommonResult<Boolean> deployModel(@RequestParam("id") String id) {
        String loginUserId = getLoginUserId();
        modelService.deployModel(loginUserId, id);
        return success(true);
    }

    @PutMapping("/update-state")
    @Operation(summary = "修改模型的状态", description = "实际更新的部署的流程定义的状态")
    @PreAuthorize("@ss.hasPermission('bpm:model:update')")
    public CommonResult<Boolean> updateModelState(@Valid @RequestBody BpmModelUpdateStateReqVO reqVO) {
        String loginUserId = getLoginUserId();
        modelService.updateModelState(loginUserId, reqVO.getId(), reqVO.getState());
        return success(true);
    }

    @Deprecated
    @PutMapping("/update-bpmn")
    @Operation(summary = "修改模型的 BPMN")
    @PreAuthorize("@ss.hasPermission('bpm:model:update')")
    public CommonResult<Boolean> updateModelBpmn(@Valid @RequestBody BpmModeUpdateBpmnReqVO reqVO) {
        modelService.updateModelBpmnXml(reqVO.getId(), reqVO.getBpmnXml());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除模型")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('bpm:model:delete')")
    public CommonResult<Boolean> deleteModel(@RequestParam("id") String id) {
        String loginUserId = getLoginUserId();
        modelService.deleteModel(loginUserId, id);
        return success(true);
    }

    @DeleteMapping("/clean")
    @Operation(summary = "清理模型")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('bpm:model:clean')")
    public CommonResult<Boolean> cleanModel(@RequestParam("id") String id) {
        String loginUserId = getLoginUserId();
        modelService.cleanModel(loginUserId, id);
        return success(true);
    }

    // ========== 仿钉钉/飞书的精简模型 =========

    @GetMapping("/simple/get")
    @Operation(summary = "获得仿钉钉流程设计模型")
    @Parameter(name = "modelId", description = "流程模型编号", required = true, example = "a2c5eee0-eb6c-11ee-abf4-0c37967c420a")
    public CommonResult<BpmSimpleModelNodeVO> getSimpleModel(@RequestParam("id") String modelId){
        return success(modelService.getSimpleModel(modelId));
    }

    @Deprecated
    @PostMapping("/simple/update")
    @Operation(summary = "保存仿钉钉流程设计模型")
    @PreAuthorize("@ss.hasPermission('bpm:model:update')")
    public CommonResult<Boolean> updateSimpleModel(@Valid @RequestBody BpmSimpleModelUpdateReqVO reqVO) {
        String loginUserId = getLoginUserId();
        modelService.updateSimpleModel(loginUserId, reqVO);
        return success(Boolean.TRUE);
    }

}
