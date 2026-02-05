package cn.iocoder.zhgd.module.bpm.controller.admin.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.zhgd.framework.common.pojo.CommonResult;
import cn.iocoder.zhgd.framework.common.pojo.PageResult;
import cn.hutool.core.util.NumberUtil;
import cn.iocoder.zhgd.module.bpm.controller.admin.base.user.UserSimpleBaseVO;
import cn.iocoder.zhgd.module.bpm.controller.admin.task.vo.task.*;
import cn.iocoder.zhgd.module.bpm.convert.task.BpmTaskConvert;
import cn.iocoder.zhgd.module.bpm.dal.dataobject.definition.BpmFormDO;
import cn.iocoder.zhgd.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO;
import cn.iocoder.zhgd.module.bpm.service.definition.BpmCategoryService;
import cn.iocoder.zhgd.module.bpm.service.definition.BpmFormService;
import cn.iocoder.zhgd.module.bpm.service.definition.BpmProcessDefinitionService;
import cn.iocoder.zhgd.module.bpm.service.task.BpmProcessInstanceService;
import cn.iocoder.zhgd.module.bpm.service.task.BpmTaskService;
import cn.iocoder.zhgd.module.system.api.dept.DeptApi;
import cn.iocoder.zhgd.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.zhgd.module.system.api.user.AdminUserApi;
import cn.iocoder.zhgd.module.system.api.user.dto.AdminUserRespDTO;
import cn.pinming.v2.authority.api.dto.AuthorityDepartmentQueryDto;
import cn.pinming.v2.authority.api.dto.AuthorityRoleDto;
import cn.pinming.v2.authority.api.dto.OrganizeRoleQueryDto;
import cn.pinming.v2.authority.api.service.AuthorityRoleService;

import cn.pinming.v2.authority.api.service.DepartmentRoleService;
import cn.pinming.zhuang.api.company.dto.EmployeeDto;
import cn.pinming.zhuang.api.company.dto.EmployeeFrontDto;
import cn.pinming.zhuang.api.company.dto.EmployeeQueryDto;
import cn.pinming.zhuang.api.company.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import cn.pinming.v2.passport.api.service.MemberService;

import java.util.*;
import java.util.stream.Stream;

import static cn.iocoder.zhgd.framework.common.exception.enums.GlobalErrorCodeConstants.UNAUTHORIZED;
import static cn.iocoder.zhgd.framework.common.pojo.CommonResult.error;
import static cn.iocoder.zhgd.framework.common.pojo.CommonResult.success;
import static cn.iocoder.zhgd.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.zhgd.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 流程任务实例")
@RestController
@RequestMapping("/bpm/task")
@Validated
@Slf4j
public class BpmTaskController {

    @Resource
    private BpmTaskService taskService;
    @Resource
    private BpmProcessInstanceService processInstanceService;
    @Resource
    private BpmFormService formService;
    @Resource
    private BpmProcessDefinitionService processDefinitionService;
    @Resource
    private BpmCategoryService categoryService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;
    @DubboReference
    private MemberService memberService;
    @DubboReference
    private AuthorityRoleService authorityRoleService;
    @DubboReference
    private EmployeeService employeeService;
    @DubboReference
    private cn.pinming.v2.company.api.service.EmployeeService v2EmployeeService;
    @DubboReference
    private DepartmentRoleService DepartmentRoleService;


    @GetMapping("say-hello")
    public List<AuthorityRoleDto> test() {
        // 1. 初始化组织角色查询DTO（核心修改：替换不可变列表为ArrayList）
        OrganizeRoleQueryDto organizeRoleQueryDto = new OrganizeRoleQueryDto();

        // 创建普通ArrayList（解决Java 17 + Hessian序列化兼容问题）
        List<AuthorityDepartmentQueryDto> deptList = new ArrayList<>();
        AuthorityDepartmentQueryDto authorityDepartmentQueryDto = new AuthorityDepartmentQueryDto();
        //authorityDepartmentQueryDto.setDepartmentId(4402);
        deptList.add(authorityDepartmentQueryDto); // 加入普通列表

        organizeRoleQueryDto.setDepartment(deptList); // 赋值普通列表（关键修复）
        organizeRoleQueryDto.setCompanyId(11864);

        // 2. 初始化员工查询DTO
        EmployeeQueryDto employeeQueryDto = new EmployeeQueryDto();
        employeeQueryDto.setCoid(11864);
        employeeQueryDto.setDepartmentId(4402);

        // 3. 调用服务（补充空指针防护，避免NPE）
        String abs = "";
        try {
            // 防护：避免memberService返回null导致String.valueOf(null)报错
            Object member = memberService.findMemberByMemberNo("1213");
            abs = String.valueOf(member == null ? "无会员信息" : member);
        } catch (Exception e) {
            abs = "查询会员失败：" + e.getMessage();
            e.printStackTrace(); // 本地开发可打印异常，生产建议用日志框架
        }

        // 防护：避免员工列表查询返回null
        List<EmployeeDto> employeeFrontDtos = new ArrayList<>();
        try {
            employeeFrontDtos = employeeService.findEmployeeListByDepartMentId(employeeQueryDto);
            if (employeeFrontDtos == null) {
                employeeFrontDtos = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 4. 调用Dubbo服务（修复序列化后可正常调用）
        List<AuthorityRoleDto> authorityRoleDtoList = new ArrayList<>();
        try {
            authorityRoleDtoList = authorityRoleService.organizeRoleList(organizeRoleQueryDto);
            if (authorityRoleDtoList == null) {
                authorityRoleDtoList = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 异常时返回空列表，避免接口返回null导致前端报错
            authorityRoleDtoList = new ArrayList<>();
        }

        // 5. 查询部门角色（补充防护）
        List<AuthorityRoleDto> authorityRoleDtoList1 = new ArrayList<>();
        try {
            authorityRoleDtoList1 = DepartmentRoleService.findDepartmentRoleId(4402);
            if (authorityRoleDtoList1 == null) {
                authorityRoleDtoList1 = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(abs);
        return authorityRoleDtoList;
    }
    @Value("${spring.redis.host}")
    private String redisHost;

    @GetMapping("/debug/redis")
    public String getRedisHost() {
        return "Current Redis Host: " + redisHost;
    }

    @GetMapping("todo-page")
    @Operation(summary = "获取 Todo 待办任务分页")
    @PreAuthorize("@ss.hasPermission('bpm:task:query')")
    public CommonResult<PageResult<BpmTaskRespVO>> getTaskTodoPage(@Valid BpmTaskPageReqVO pageVO) {
        String loginUserId = getLoginUserId();
        log.info("[BpmTask] todo-page loginUserId={}", loginUserId);
        if (loginUserId == null) {
            return error(UNAUTHORIZED.getCode(), "当前登录用户凭证已失效");
        }
        PageResult<Task> pageResult = taskService.getTaskTodoPage(loginUserId, pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }

        // 拼接数据
        Map<String, ProcessInstance> processInstanceMap = processInstanceService.getProcessInstanceMap(
                convertSet(pageResult.getList(), Task::getProcessInstanceId));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(processInstanceMap.values(), instance -> NumberUtil.parseLong(instance.getStartUserId(), null)));
        Map<String, BpmProcessDefinitionInfoDO> processDefinitionInfoMap = processDefinitionService.getProcessDefinitionInfoMap(
                convertSet(pageResult.getList(), Task::getProcessDefinitionId));
        Map<String, cn.iocoder.zhgd.module.bpm.dal.dataobject.definition.BpmCategoryDO> categoryMap = categoryService.getCategoryMap(
                convertSet(filterList(processDefinitionInfoMap.values(), info -> info != null && StrUtil.isNotBlank(info.getCategory())), BpmProcessDefinitionInfoDO::getCategory));
        PageResult<BpmTaskRespVO> result = BpmTaskConvert.INSTANCE.buildTodoTaskPage(pageResult, processInstanceMap, userMap, processDefinitionInfoMap, categoryMap);
        fillAssigneeAndOwnerUser(result, pageVO.getCompanyId());
        return success(result);
    }

    @GetMapping("done-page")
    @Operation(summary = "获取 Done 已办任务分页")
    @PreAuthorize("@ss.hasPermission('bpm:task:query')")
    public CommonResult<PageResult<BpmTaskRespVO>> getTaskDonePage(@Valid BpmTaskPageReqVO pageVO) {
        String loginUserId = getLoginUserId();
        if (loginUserId == null) {
            return error(UNAUTHORIZED.getCode(), "当前登录用户凭证已失效");
        }
        PageResult<HistoricTaskInstance> pageResult = taskService.getTaskDonePage(loginUserId, pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }

        // 拼接数据
        Map<String, HistoricProcessInstance> processInstanceMap = processInstanceService.getHistoricProcessInstanceMap(
                convertSet(pageResult.getList(), HistoricTaskInstance::getProcessInstanceId));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(processInstanceMap.values(), instance -> NumberUtil.parseLong(instance.getStartUserId(), null)));
        Map<String, BpmProcessDefinitionInfoDO> processDefinitionInfoMap = processDefinitionService.getProcessDefinitionInfoMap(
                convertSet(pageResult.getList(), HistoricTaskInstance::getProcessDefinitionId));
        Map<String, cn.iocoder.zhgd.module.bpm.dal.dataobject.definition.BpmCategoryDO> categoryMap = categoryService.getCategoryMap(
                convertSet(filterList(processDefinitionInfoMap.values(), info -> info != null && StrUtil.isNotBlank(info.getCategory())), BpmProcessDefinitionInfoDO::getCategory));
        PageResult<BpmTaskRespVO> result = BpmTaskConvert.INSTANCE.buildTaskPage(pageResult, processInstanceMap, userMap, null, processDefinitionInfoMap, categoryMap);
        fillAssigneeAndOwnerUser(result, pageVO.getCompanyId());
        return success(result);
    }

    @GetMapping("manager-page")
    @Operation(summary = "获取全部任务的分页", description = "用于【流程任务】菜单")
    @PreAuthorize("@ss.hasPermission('bpm:task:mananger-query')")
    public CommonResult<PageResult<BpmTaskRespVO>> getTaskManagerPage(@Valid BpmTaskPageReqVO pageVO) {
        String loginUserId = getLoginUserId();
        if (loginUserId == null) {
            return error(UNAUTHORIZED.getCode(), "当前登录用户凭证已失效");
        }
        PageResult<HistoricTaskInstance> pageResult = taskService.getTaskPage(loginUserId, pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }

        // 拼接数据
        Map<String, HistoricProcessInstance> processInstanceMap = processInstanceService.getHistoricProcessInstanceMap(
                convertSet(pageResult.getList(), HistoricTaskInstance::getProcessInstanceId));
        // 获得 User 和 Dept Map
        Set<Long> userIds = convertSet(processInstanceMap.values(),
                instance -> NumberUtil.parseLong(instance.getStartUserId(), null));
        userIds.addAll(convertSet(pageResult.getList(), task -> NumberUtil.parseLong(task.getAssignee(), null)));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        Map<String, BpmProcessDefinitionInfoDO> processDefinitionInfoMap = processDefinitionService.getProcessDefinitionInfoMap(
                convertSet(pageResult.getList(), HistoricTaskInstance::getProcessDefinitionId));
        Map<String, cn.iocoder.zhgd.module.bpm.dal.dataobject.definition.BpmCategoryDO> categoryMap = categoryService.getCategoryMap(
                convertSet(filterList(processDefinitionInfoMap.values(), info -> info != null && StrUtil.isNotBlank(info.getCategory())), BpmProcessDefinitionInfoDO::getCategory));
        return success(BpmTaskConvert.INSTANCE.buildTaskPage(pageResult, processInstanceMap, userMap, deptMap, processDefinitionInfoMap, categoryMap));
    }

    @GetMapping("/list-by-process-instance-id")
    @Operation(summary = "获得指定流程实例的任务列表", description = "包括完成的、未完成的")
    @Parameter(name = "processInstanceId", description = "流程实例的编号", required = true)
    @PreAuthorize("@ss.hasPermission('bpm:task:query')")
    public CommonResult<List<BpmTaskRespVO>> getTaskListByProcessInstanceId(
            @RequestParam("processInstanceId") String processInstanceId) {
        List<HistoricTaskInstance> taskList = taskService.getTaskListByProcessInstanceId(processInstanceId, true);
        if (CollUtil.isEmpty(taskList)) {
            return success(Collections.emptyList());
        }

        // 拼接数据
        Set<Long> userIds = convertSetByFlatMap(taskList, task ->
                Stream.of(NumberUtil.parseLong(task.getAssignee(), null), NumberUtil.parseLong(task.getOwner(), null)));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        // 获得 Form Map
        Map<Long, BpmFormDO> formMap = formService.getFormMap(
                convertSet(taskList, task -> NumberUtil.parseLong(task.getFormKey(), null)));
        return success(BpmTaskConvert.INSTANCE.buildTaskListByProcessInstanceId(taskList,
                formMap, userMap, deptMap));
    }

    @PutMapping("/approve")
    @Operation(summary = "通过任务")
    @PreAuthorize("@ss.hasPermission('bpm:task:update')")
    public CommonResult<Boolean> approveTask(@Valid @RequestBody BpmTaskApproveReqVO reqVO) {
        String loginUserId = getLoginUserId();
        taskService.approveTask(loginUserId, reqVO);
        return success(true);
    }

    @PutMapping("/reject")
    @Operation(summary = "不通过任务")
    @PreAuthorize("@ss.hasPermission('bpm:task:update')")
    public CommonResult<Boolean> rejectTask(@Valid @RequestBody BpmTaskRejectReqVO reqVO) {
        String loginUserId = getLoginUserId();
        taskService.rejectTask(loginUserId, reqVO);
        return success(true);
    }

    @GetMapping("/list-by-return")
    @Operation(summary = "获取所有可退回的节点", description = "用于【流程详情】的【退回】按钮")
    @Parameter(name = "taskId", description = "当前任务ID", required = true)
    @PreAuthorize("@ss.hasPermission('bpm:task:update')")
    public CommonResult<List<BpmTaskRespVO>> getTaskListByReturn(@RequestParam("id") String id) {
        List<UserTask> userTaskList = taskService.getUserTaskListByReturn(id);
        return success(convertList(userTaskList, userTask -> // 只返回 id 和 name
                new BpmTaskRespVO().setName(userTask.getName()).setTaskDefinitionKey(userTask.getId())));
    }

    @PutMapping("/return")
    @Operation(summary = "退回任务", description = "用于【流程详情】的【退回】按钮")
    @PreAuthorize("@ss.hasPermission('bpm:task:update')")
    public CommonResult<Boolean> returnTask(@Valid @RequestBody BpmTaskReturnReqVO reqVO) {
        String loginUserId = getLoginUserId();
        taskService.returnTask(loginUserId, reqVO);
        return success(true);
    }

    @PutMapping("/delegate")
    @Operation(summary = "委派任务", description = "用于【流程详情】的【委派】按钮")
    @PreAuthorize("@ss.hasPermission('bpm:task:update')")
    public CommonResult<Boolean> delegateTask(@Valid @RequestBody BpmTaskDelegateReqVO reqVO) {
        String loginUserId = getLoginUserId();
        taskService.delegateTask(loginUserId, reqVO);
        return success(true);
    }

    @PutMapping("/transfer")
    @Operation(summary = "转派任务", description = "用于【流程详情】的【转派】按钮")
    @PreAuthorize("@ss.hasPermission('bpm:task:update')")
    public CommonResult<Boolean> transferTask(@Valid @RequestBody BpmTaskTransferReqVO reqVO) {
        String loginUserId = getLoginUserId();
        taskService.transferTask(loginUserId, reqVO);
        return success(true);
    }

    @PutMapping("/create-sign")
    @Operation(summary = "加签", description = "before 前加签，after 后加签")
    @PreAuthorize("@ss.hasPermission('bpm:task:update')")
    public CommonResult<Boolean> createSignTask(@Valid @RequestBody BpmTaskSignCreateReqVO reqVO) {
        String loginUserId = getLoginUserId();
        taskService.createSignTask(loginUserId, reqVO);
        return success(true);
    }

    @DeleteMapping("/delete-sign")
    @Operation(summary = "减签")
    @PreAuthorize("@ss.hasPermission('bpm:task:update')")
    public CommonResult<Boolean> deleteSignTask(@Valid @RequestBody BpmTaskSignDeleteReqVO reqVO) {
        String loginUserId = getLoginUserId();
        taskService.deleteSignTask(loginUserId, reqVO);
        return success(true);
    }

    @PutMapping("/copy")
    @Operation(summary = "抄送任务")
    @PreAuthorize("@ss.hasPermission('bpm:task:update')")
    public CommonResult<Boolean> copyTask(@Valid @RequestBody BpmTaskCopyReqVO reqVO) {
        String loginUserId = getLoginUserId();
        taskService.copyTask(loginUserId, reqVO);
        return success(true);
    }

    @PutMapping("/withdraw")
    @Operation(summary = "撤回任务")
    @PreAuthorize("@ss.hasPermission('bpm:task:update')")
    public CommonResult<Boolean> withdrawTask(@RequestParam("taskId") String taskId) {
        String loginUserId = getLoginUserId();
        taskService.withdrawTask(loginUserId, taskId);
        return success(true);
    }

    @GetMapping("/list-by-parent-task-id")
    @Operation(summary = "获得指定父级任务的子任务列表") // 目前用于，减签的时候，获得子任务列表
    @Parameter(name = "parentTaskId", description = "父级任务编号", required = true)
    @PreAuthorize("@ss.hasPermission('bpm:task:query')")
    public CommonResult<List<BpmTaskRespVO>> getTaskListByParentTaskId(@RequestParam("parentTaskId") String parentTaskId) {
        List<Task> taskList = taskService.getTaskListByParentTaskId(parentTaskId);
        if (CollUtil.isEmpty(taskList)) {
            return success(Collections.emptyList());
        }
        // 拼接数据
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSetByFlatMap(taskList,
                user -> Stream.of(NumberUtil.parseLong(user.getAssignee(), null), NumberUtil.parseLong(user.getOwner(), null))));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(
                convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        return success(BpmTaskConvert.INSTANCE.buildTaskListByParentTaskId(taskList, userMap, deptMap));
    }

    /**
     * 仅当请求传入 companyId 时，根据 assignee/owner/startUserId 的 memberId 调用 Dubbo 补全 assigneeUser、ownerUser、processInstance.startUser；
     * 未传 companyId 则不调用，上述用户信息保持 null。
     */
    private void fillAssigneeAndOwnerUser(PageResult<BpmTaskRespVO> pageResult, Long requestCompanyId) {
        if (pageResult == null || CollUtil.isEmpty(pageResult.getList()) || requestCompanyId == null) {
            return;
        }
        int companyId = requestCompanyId.intValue();
        Set<String> memberIds = new HashSet<>();
        for (BpmTaskRespVO task : pageResult.getList()) {
            collectAssigneeOwnerIds(task, memberIds);
            // 发起人 ID 一并收集，用于 Dubbo 补全 processInstance.startUser
            if (task.getProcessInstance() != null && StrUtil.isNotBlank(task.getProcessInstance().getStartUserId())) {
                memberIds.add(task.getProcessInstance().getStartUserId());
            }
        }
        Map<String, cn.pinming.v2.company.api.dto.EmployeeDto> employeeMap = new HashMap<>();
        for (String memberId : memberIds) {
            if (StrUtil.isBlank(memberId)) {
                continue;
            }
            try {
                cn.pinming.v2.company.api.dto.EmployeeDto emp = v2EmployeeService.findEmployee(companyId, memberId);
                if (emp != null) {
                    employeeMap.put(memberId, emp);
                }
            } catch (Exception e) {
                log.warn("[fillAssigneeAndOwnerUser] findEmployee failed, companyId={}, memberId={}", companyId, memberId, e);
            }
        }
        for (BpmTaskRespVO task : pageResult.getList()) {
            fillTaskAssigneeOwner(task, employeeMap);
            // 补全发起人用户信息
            if (task.getProcessInstance() != null && StrUtil.isNotBlank(task.getProcessInstance().getStartUserId())) {
                cn.pinming.v2.company.api.dto.EmployeeDto startEmp = employeeMap.get(task.getProcessInstance().getStartUserId());
                task.getProcessInstance().setStartUser(employeeToUserSimple(startEmp));
            }
        }
    }

    private void collectAssigneeOwnerIds(BpmTaskRespVO task, Set<String> memberIds) {
        if (task == null) {
            return;
        }
        if (StrUtil.isNotBlank(task.getAssignee())) {
            memberIds.add(task.getAssignee());
        }
        if (StrUtil.isNotBlank(task.getOwner())) {
            memberIds.add(task.getOwner());
        }
        if (CollUtil.isNotEmpty(task.getChildren())) {
            for (BpmTaskRespVO child : task.getChildren()) {
                collectAssigneeOwnerIds(child, memberIds);
            }
        }
    }

    private void fillTaskAssigneeOwner(BpmTaskRespVO task, Map<String, cn.pinming.v2.company.api.dto.EmployeeDto> employeeMap) {
        if (task == null) {
            return;
        }
        if (StrUtil.isNotBlank(task.getAssignee())) {
            cn.pinming.v2.company.api.dto.EmployeeDto emp = employeeMap.get(task.getAssignee());
            task.setAssigneeUser(employeeToUserSimple(emp));
        }
        if (StrUtil.isNotBlank(task.getOwner())) {
            cn.pinming.v2.company.api.dto.EmployeeDto emp = employeeMap.get(task.getOwner());
            task.setOwnerUser(employeeToUserSimple(emp));
        }
        if (CollUtil.isNotEmpty(task.getChildren())) {
            for (BpmTaskRespVO child : task.getChildren()) {
                fillTaskAssigneeOwner(child, employeeMap);
            }
        }
    }

    private static UserSimpleBaseVO employeeToUserSimple(cn.pinming.v2.company.api.dto.EmployeeDto emp) {
        if (emp == null) {
            return null;
        }
        UserSimpleBaseVO vo = new UserSimpleBaseVO();
        vo.setId(emp.getId() != null ? Long.valueOf(emp.getId()) : null);
        vo.setNickname(emp.getMemberName());
        vo.setAvatar(null);
        vo.setDeptId(null);
        vo.setDeptName(null);
        return vo;
    }

}
