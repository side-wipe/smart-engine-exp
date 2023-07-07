package com.manwang.smartengine.demo.database.delegation.predict;

import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.delegation.JavaDelegation;
import com.alibaba.smart.framework.engine.model.instance.ExecutionInstance;
import com.alibaba.smart.framework.engine.model.instance.ProcessInstance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author: zhifeng
 * @Description:
 * @Date: 2023/6/13 21
 */
@Slf4j
@Component
public class AutoCreateOrderPredcitProcessDelegation implements JavaDelegation {
    @Override
    public void execute(ExecutionContext executionContext) {
        ProcessInstance processInstance = executionContext.getProcessInstance();
        String instanceId = processInstance.getInstanceId();
        log.info("AutoCreateOrderPredcitProcessDelegation:创建订单预测流程，instanceId:{}",instanceId);
        ExecutionInstance executionInstance = executionContext.getExecutionInstance();
        log.info("executionInstance:{}",executionInstance.getActivityInstanceId());
    }
}
