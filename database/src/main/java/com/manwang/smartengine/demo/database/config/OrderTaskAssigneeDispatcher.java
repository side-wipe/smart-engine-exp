package com.manwang.smartengine.demo.database.config;

import com.alibaba.smart.framework.engine.common.util.CollectionUtil;
import com.alibaba.smart.framework.engine.configuration.TaskAssigneeDispatcher;
import com.alibaba.smart.framework.engine.constant.AssigneeTypeConstant;
import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.model.assembly.Activity;
import com.alibaba.smart.framework.engine.model.assembly.ExtensionElementContainer;
import com.alibaba.smart.framework.engine.model.assembly.ExtensionElements;
import com.alibaba.smart.framework.engine.model.assembly.ProcessDefinition;
import com.alibaba.smart.framework.engine.model.instance.ProcessInstance;
import com.alibaba.smart.framework.engine.model.instance.TaskAssigneeCandidateInstance;
import com.alibaba.smart.framework.engine.smart.Properties;
import com.alibaba.smart.framework.engine.smart.PropertiesElementMarker;
import com.alibaba.smart.framework.engine.smart.Property;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class OrderTaskAssigneeDispatcher implements TaskAssigneeDispatcher {


    @Override
    public List<TaskAssigneeCandidateInstance> getTaskAssigneeCandidateInstance(Activity activity,
        ExecutionContext executionContext) {
        List<TaskAssigneeCandidateInstance> assigneeLists = getTaskAssignees(activity);
        ProcessDefinition processDefinition = executionContext.getProcessDefinition();
        String processDefinitionId = processDefinition.getId();
        ProcessInstance processInstance = executionContext.getProcessInstance();
        String processDefinitionType = processInstance.getProcessDefinitionType();

        Map<String, String> extentionProperties = this.findExtentionProperties(activity);
        Map<String, String> extentionProperties1 = this.findExtentionProperties(
            executionContext.getProcessDefinition());
        sendMessage(activity.getId());
        return assigneeLists;
    }

    /**
     * 获取流程定义扩展属性
     * @param extensionElementContainer
     * @return
     */
    public Map<String,String> findExtentionProperties(ExtensionElementContainer extensionElementContainer){
        List<PropertiesElementMarker> processPropertiesElements = Optional.ofNullable(extensionElementContainer)
            .map(ExtensionElementContainer::getExtensionElements)
            .map(ExtensionElements::getExtensionList)
            .map(extensionElements -> extensionElements.get(0))
            .map(Properties.class::cast)
            .map(Properties::getExtensionList)
            .orElse(null);
        if (CollectionUtil.isEmpty(processPropertiesElements)){
            return null;
        }
        return processPropertiesElements.stream().map(Property.class::cast).filter(p -> StringUtils.isNotBlank(p.getValue())).collect(
            Collectors.toMap(Property::getName, Property::getValue));

    }

    /**
     * 对外发送一个需要人工处理的消息
     *
     * @param id
     */
    private void sendMessage(String id) {
        log.info("{}任务等待人工处理", id);
    }

    /**
     * 根据活动返回对应的审核人
     *
     * @param activity
     * @return
     */
    private List<TaskAssigneeCandidateInstance> getTaskAssignees(Activity activity) {
        List<TaskAssigneeCandidateInstance> taskAssigneeCandidateInstanceList = new ArrayList<TaskAssigneeCandidateInstance>();
        TaskAssigneeCandidateInstance taskAssigneeCandidateInstance = new TaskAssigneeCandidateInstance();
        taskAssigneeCandidateInstance.setAssigneeId(activity.getProperties().get("assignee"));
        taskAssigneeCandidateInstance.setAssigneeType(AssigneeTypeConstant.USER);
        taskAssigneeCandidateInstanceList.add(taskAssigneeCandidateInstance);
        return taskAssigneeCandidateInstanceList;
    }


}
