package com.manwang.smartengine.demo.database.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.smart.framework.engine.SmartEngine;
import com.alibaba.smart.framework.engine.constant.RequestMapSpecialKeyConstant;
import com.alibaba.smart.framework.engine.model.instance.ProcessInstance;
import com.alibaba.smart.framework.engine.model.instance.TaskInstance;
import com.alibaba.smart.framework.engine.service.command.ProcessCommandService;
import com.alibaba.smart.framework.engine.service.command.TaskCommandService;
import com.alibaba.smart.framework.engine.service.param.query.PendingTaskQueryParam;
import com.alibaba.smart.framework.engine.service.query.ActivityQueryService;
import com.alibaba.smart.framework.engine.service.query.ProcessQueryService;
import com.alibaba.smart.framework.engine.service.query.TaskQueryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Created by 高海军 帝奇 74394 on 2017 January  22:36.
 */

@RestController
public class AskForLeaveProcess {

    @Autowired
    private  SmartEngine smartEngine;




    //2.获得常用服务

    @RequestMapping("/leave/new")
    public ModelAndView _new() {
        return new ModelAndView("newLeaveProcess");
    }


    @RequestMapping("/leave/submitLeave")
    public ModelAndView submit(@RequestParam(value = "desc",required = false) String  desc, @RequestParam(value = "creatorId",required = false) String  creatorId, @RequestParam(value = "assigneeId",required = false) String  assigneeId,
                         RedirectAttributes attr) {
        ProcessCommandService processCommandService = smartEngine.getProcessCommandService();
        TaskCommandService taskCommandService = smartEngine.getTaskCommandService();

        ProcessQueryService processQueryService = smartEngine.getProcessQueryService();
        ActivityQueryService activityQueryService = smartEngine.getActivityQueryService();
        TaskQueryService taskQueryService = smartEngine.getTaskQueryService();


        //4.启动流程实例
        ProcessInstance processInstance = processCommandService.start(
                "order", "1.0.0"
        );

        // DONT USE findAllPendingTaskList IN PRODUCTION ENV.
        List<TaskInstance> submitTaskInstanceList = taskQueryService.findAllPendingTaskList(processInstance.getInstanceId());
        TaskInstance submitTaskInstance = submitTaskInstanceList.get(0);

        //5.流程流转:构造提交申请参数
        Map<String, Object> submitFormRequest = new HashMap<String, Object>();
        submitFormRequest.put(RequestMapSpecialKeyConstant.PROCESS_TITLE, "申请今天调休");
        submitFormRequest.put("desc", desc);

        //为了方便测试,这里没和用户系统集成。
        if(null == creatorId){
            creatorId = "1";
        }
        submitFormRequest.put("creator",creatorId);


        //6.流程流转:处理 submitTask,完成任务申请.
        taskCommandService.complete(submitTaskInstance.getInstanceId(), submitFormRequest);


        PendingTaskQueryParam pendingTaskQueryParam = new PendingTaskQueryParam();
        pendingTaskQueryParam.setAssigneeUserId("2");

        submitTaskInstanceList = taskQueryService.findPendingTaskList(pendingTaskQueryParam);
        return new ModelAndView("homePage", "taskAssigneeList", submitTaskInstanceList);

    }



    @RequestMapping("/leave/viewLeave/{taskInstanceId}")
    public ModelAndView viewLeave(@PathVariable(value = "taskInstanceId") String taskInstanceId) {

        return new ModelAndView("viewLeaveProcess","taskInstanceId",taskInstanceId);

    }


    @RequestMapping("/leave/auditLeave")
    public ModelAndView auditLeave(@RequestParam(value = "taskInstanceId") String  taskInstanceId,@RequestParam(value = "opinion") String  opinion,RedirectAttributes attr) {

        // 可以考虑增加权限校验。
        //7. 获取当前待处理任务.
//        List<TaskInstance> auditTaskInstanceList = taskQueryService.findPendingTask(taskInstanceId);
//        TaskInstance auditTaskInstance = auditTaskInstanceList.get(0);
        Map<String, Object> approveFormRequest = new HashMap<String, Object>();

        //10.
        approveFormRequest.put("approve", opinion);
        approveFormRequest.put("desc", opinion);

        //9.审批通过,驱动流程节点到自动执行任务环节
        TaskCommandService taskCommandService = smartEngine.getTaskCommandService();
        TaskQueryService taskQueryService = smartEngine.getTaskQueryService();


        taskCommandService.complete(taskInstanceId, approveFormRequest);

        PendingTaskQueryParam pendingTaskQueryParam = new PendingTaskQueryParam();
        pendingTaskQueryParam.setAssigneeUserId("2");

        List<TaskInstance> submitTaskInstanceList = taskQueryService.findPendingTaskList(pendingTaskQueryParam);
        return new ModelAndView("homePage", "taskAssigneeList", submitTaskInstanceList);


    }
}
