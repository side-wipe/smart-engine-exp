package com.manwang.smartengine.demo.database.test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.smart.framework.engine.SmartEngine;
import com.alibaba.smart.framework.engine.bpmn.assembly.task.UserTask;
import com.alibaba.smart.framework.engine.common.util.CollectionUtil;
import com.alibaba.smart.framework.engine.instance.impl.DefaultExecutionInstance;
import com.alibaba.smart.framework.engine.model.assembly.BaseElement;
import com.alibaba.smart.framework.engine.model.assembly.ProcessDefinition;
import com.alibaba.smart.framework.engine.model.assembly.ProcessDefinitionSource;
import com.alibaba.smart.framework.engine.model.instance.DeploymentInstance;
import com.alibaba.smart.framework.engine.model.instance.ExecutionInstance;
import com.alibaba.smart.framework.engine.model.instance.InstanceStatus;
import com.alibaba.smart.framework.engine.model.instance.ProcessInstance;
import com.alibaba.smart.framework.engine.service.command.DeploymentCommandService;
import com.alibaba.smart.framework.engine.service.command.ExecutionCommandService;
import com.alibaba.smart.framework.engine.service.command.ProcessCommandService;
import com.alibaba.smart.framework.engine.service.command.RepositoryCommandService;
import com.alibaba.smart.framework.engine.service.param.command.CreateDeploymentCommand;
import com.alibaba.smart.framework.engine.service.param.command.UpdateDeploymentCommand;
import com.alibaba.smart.framework.engine.service.param.query.DeploymentInstanceQueryParam;
import com.alibaba.smart.framework.engine.service.query.DeploymentQueryService;
import com.alibaba.smart.framework.engine.service.query.ExecutionQueryService;
import com.alibaba.smart.framework.engine.service.query.ProcessQueryService;
import com.alibaba.smart.framework.engine.service.query.RepositoryQueryService;
import com.alibaba.smart.framework.engine.util.IOUtil;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SmartTest {

    @Autowired
    private SmartEngine smartEngine;


    @Test
    @DisplayName("读取配置文件实例化流程模板")
    public void testRepositoryCommandService() {

        RepositoryCommandService repositoryCommandService = smartEngine.getRepositoryCommandService();
        ProcessDefinitionSource definitionSource = repositoryCommandService.deploy("smart-engine/traffic_order_predict.bpmn20.xml");
        Assertions.assertEquals(1, definitionSource.getProcessDefinitionList().size());
    }


    @Test
    @DisplayName("查询内存中的流程模板实例")
    public void testRepositoryQueryService() {
        RepositoryCommandService repositoryCommandService = smartEngine.getRepositoryCommandService();
        ProcessDefinitionSource definitionSource = repositoryCommandService.deploy("smart-engine/order.xml.old");

        RepositoryQueryService repositoryQueryService = smartEngine.getRepositoryQueryService();
        ProcessDefinition processDefinition = repositoryQueryService.getCachedProcessDefinition("order", "1.0.0");
        Assertions.assertEquals(definitionSource.getProcessDefinitionList().get(0).getId(), processDefinition.getId());
    }

    @Test
    @DisplayName("发布流程定义")
    public void testDeploymentCommandService() {
        DeploymentCommandService deploymentCommandService = smartEngine.getDeploymentCommandService();
        String content = IOUtil.readResourceFileAsUTF8String("smart-engine/traffic_order_predict.bpmn20.xml");
        CreateDeploymentCommand createDeploymentCommand = new CreateDeploymentCommand();
        createDeploymentCommand.setProcessDefinitionCode("traffic_order_predict");
        createDeploymentCommand.setProcessDefinitionName("traffic_order_predict");
        createDeploymentCommand.setDeploymentUserId("zhifeng");
        createDeploymentCommand.setProcessDefinitionContent(content);
        createDeploymentCommand.setProcessDefinitionDesc("订单预测流程定义");
        createDeploymentCommand.setProcessDefinitionType("order_predict");
        createDeploymentCommand.setDeploymentStatus("new");
        DeploymentInstance instance = deploymentCommandService.createDeployment(createDeploymentCommand);
        instance.getDeploymentStatus();
    }

    @Test
    @DisplayName("修改流程定义")
    public void updateDeploymentCommandService() {
        DeploymentCommandService deploymentCommandService = smartEngine.getDeploymentCommandService();
        String content = IOUtil.readResourceFileAsUTF8String("smart-engine/hotel_order_predict.bpmn20.xml");
        UpdateDeploymentCommand updateDeploymentCommand = new UpdateDeploymentCommand();
        updateDeploymentCommand.setDeployInstanceId("430868255293308928");
        updateDeploymentCommand.setProcessDefinitionContent(content);
        DeploymentInstance deploymentInstance = deploymentCommandService.updateDeployment(updateDeploymentCommand);
    }

    @Test
    @DisplayName("作废流程定义")
    public void invalidDeploymentCommandService() {
        DeploymentCommandService deploymentCommandService = smartEngine.getDeploymentCommandService();
        deploymentCommandService.deleteDeploymentInstanceLogically("428781921787445248");
    }

    @Test
    @DisplayName("查询流程定义内容")
    public void testDeploymentQueryService() {
        DeploymentQueryService deploymentQueryService = smartEngine.getDeploymentQueryService();
        DeploymentInstance deploymentInstance = deploymentQueryService.findById("430868255293308928");
        RepositoryCommandService repositoryCommandService = smartEngine.getRepositoryCommandService();
        ProcessDefinitionSource definitionSource = repositoryCommandService.deployWithUTF8Content(deploymentInstance.getProcessDefinitionContent());
        System.out.println(definitionSource);
    }


    @Test
    @DisplayName("根据流程id查询流程定义内容")
    public void findProcessDefinition(){
            //获取流程定义中的节点
            DeploymentQueryService deploymentQueryService = smartEngine.getDeploymentQueryService();
            DeploymentInstanceQueryParam param = new DeploymentInstanceQueryParam();
            param.setProcessDefinitionType("order_predict");
            param.setProcessDefinitionCode("hotel_order_predict");
            param.setLogicStatus("valid");
            List<DeploymentInstance> list = deploymentQueryService.findList(param);
            if (CollectionUtil.isEmpty(list)){
                return;
            }
            RepositoryCommandService repositoryCommandService = smartEngine.getRepositoryCommandService();
            ProcessDefinitionSource definitionSource = repositoryCommandService.deployWithUTF8Content(list.get(0).getProcessDefinitionContent());
            if (definitionSource == null || CollectionUtil.isEmpty(definitionSource.getProcessDefinitionList())){
                return;
            }
            ProcessDefinition processDefinition = definitionSource.getProcessDefinitionList().get(0);
    }


    @Test
    @DisplayName("流程实例管理")
    public void testProcessCommandService() {
        ProcessCommandService processCommandService = smartEngine.getProcessCommandService();
        processCommandService.startWith("430868255293308928");
        //processCommandService.start("hotel_order_predict","1.0.0");
    }



    @Test
    @DisplayName("关闭流程实例管理")
    public void testAbortProcessCommandService() {
        Map<String, Object> request = new HashMap<>();
        ProcessCommandService processCommandService = smartEngine.getProcessCommandService();
        processCommandService.abort("423609546661953536");
    }

    @Test
    @DisplayName("test")
    public void test() {
        try {
            throw new RuntimeException("test");
        }finally {
            System.out.println("finally");
        }
    }

    @Test
    @DisplayName("流程实例查询")
    public void testProcessQueryService() {
        ProcessQueryService processQueryService = smartEngine.getProcessQueryService();
        ProcessInstance processInstance = processQueryService.findById("423695572730380288");
        Assertions.assertEquals(InstanceStatus.running, processInstance.getStatus());
    }

    @Test
    @DisplayName("唤起人工审核节点")
    public void testExecutionCommandService() {
        // 针对userTask进行唤醒
        ExecutionCommandService executionCommandService = smartEngine.getExecutionCommandService();
        // 流程查询器
        ProcessQueryService processQueryService = smartEngine.getProcessQueryService();
        // 执行记录查询器
        ExecutionQueryService executionQueryService = smartEngine.getExecutionQueryService();
        ProcessInstance processInstance = processQueryService.findById("297181418952327168");
        // 获取当前激活的节点
        List<ExecutionInstance> activeExecutionInstances = executionQueryService.findActiveExecutionList(processInstance.getInstanceId());
        Map<String, Object> request = new HashMap<>();
        request.put("approve", "agree");
        for (ExecutionInstance activeExecutionInstance : activeExecutionInstances) {
            if (activeExecutionInstance.getProcessDefinitionActivityId().equals("Activity_0sovwe9")) {
                executionCommandService.signal(activeExecutionInstance.getInstanceId(), request);
            }
        }
    }

    @Test
    @DisplayName("人工任务节点")
    public void testUserTaskService() {
        // 针对userTask进行唤醒
        ExecutionCommandService executionCommandService = smartEngine.getExecutionCommandService();
        // 流程查询器
        ProcessQueryService processQueryService = smartEngine.getProcessQueryService();
        // 执行记录查询器
        ExecutionQueryService executionQueryService = smartEngine.getExecutionQueryService();
        ProcessInstance processInstance = processQueryService.findById("431950224315908096");
        // 获取当前激活的节点
        List<ExecutionInstance> activeExecutionInstances = executionQueryService.findActiveExecutionList(processInstance.getInstanceId());
        ExecutionInstance executionInstance = activeExecutionInstances.get(0);
        ProcessInstance signal = executionCommandService.signal(executionInstance.getInstanceId());
        List<ExecutionInstance> all = executionQueryService.findAll("431950224315908096");
        Date completeTime = signal.getCompleteTime();
        System.out.println(signal);
    }

    @Test
    @DisplayName("查询所有节点")
    public void getAll() {
        List<ExecutionInstance> allActivities = findAllActivities(431950224315908096L);
        System.out.println(allActivities);
    }

    /**
     * 获取所有的流程节点，含未触达的
     * @param processInstanceId
     * @return
     */
    public List<ExecutionInstance> findAllActivities(Long processInstanceId){
        //查询流程实例
        ProcessInstance processInstance = this.findProcessInstance(processInstanceId);

        //获取流程定义中的节点
        ProcessDefinition processDefinition = this.findProcessDefinition(processInstance);
        List<BaseElement> baseElementList = processDefinition.getBaseElementList();
        List<ExecutionInstance> definedActivityInfos = baseElementList.stream().filter(b -> b instanceof UserTask)
            .map(s -> {
                UserTask userTask = (UserTask)s;
                DefaultExecutionInstance executionInstance = new DefaultExecutionInstance();
                executionInstance.setProcessInstanceId(String.valueOf(processInstanceId));
                executionInstance.setProcessDefinitionActivityId(userTask.getId());
                executionInstance.setActive(false);
                return executionInstance;
            })
            .collect(Collectors.toList());


        //获取已执行的人工流程节点
        List<ExecutionInstance> instances = this.findActivities(processInstanceId);
        if (CollectionUtil.isEmpty(instances)){
            return definedActivityInfos;
        }

        //去重，保证原有顺序
        List<String> keys = instances.stream().map(ExecutionInstance::getProcessDefinitionActivityId).collect(
            Collectors.toList());
        List<ExecutionInstance> definitionActivities = definedActivityInfos.stream().filter(
            d -> !keys.contains(d.getProcessDefinitionActivityId())).collect(Collectors.toList());
        instances.addAll(definitionActivities);
        return instances;

    }

    /**
     * 获取流程实例
     * @param processInstanceId
     * @return
     */
    public ProcessInstance findProcessInstance(Long processInstanceId){
        ProcessQueryService processQueryService = smartEngine.getProcessQueryService();
        return processQueryService.findById(String.valueOf(processInstanceId));
    }

    /**
     * 获取已执行的人工流程节点
     * @param processInstanceId
     * @return
     */
    public List<ExecutionInstance> findActivities(Long processInstanceId){
        ExecutionQueryService executionQueryService = smartEngine.getExecutionQueryService();
        return executionQueryService.findAll(String.valueOf(processInstanceId));
    }

    /**
     * 获取流程定义
     * @param processInstance
     * @return
     */
    public ProcessDefinition findProcessDefinition(ProcessInstance processInstance){
        //获取流程定义中的节点
        DeploymentQueryService deploymentQueryService = smartEngine.getDeploymentQueryService();
        DeploymentInstanceQueryParam param = new DeploymentInstanceQueryParam();
        param.setProcessDefinitionType(processInstance.getProcessDefinitionType());
        param.setProcessDefinitionCode(processInstance.getProcessDefinitionId());
        param.setLogicStatus("valid");
        List<DeploymentInstance> list = deploymentQueryService.findList(param);
        if (CollectionUtil.isEmpty(list)){
            return null;
        }
        RepositoryCommandService repositoryCommandService = smartEngine.getRepositoryCommandService();
        ProcessDefinitionSource definitionSource = repositoryCommandService.deployWithUTF8Content(list.get(0).getProcessDefinitionContent());
        if (definitionSource == null || CollectionUtil.isEmpty(definitionSource.getProcessDefinitionList())){
            return null;
        }
        return definitionSource.getProcessDefinitionList().get(0);
    }
}
