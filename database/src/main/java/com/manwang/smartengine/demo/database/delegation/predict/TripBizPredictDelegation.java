package com.manwang.smartengine.demo.database.delegation.predict;

import com.alibaba.smart.framework.engine.context.ExecutionContext;
import com.alibaba.smart.framework.engine.delegation.JavaDelegation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author: zhifeng
 * @Description:
 * @Date: 2023/6/13 21
 */
@Component
@Slf4j
public class TripBizPredictDelegation implements JavaDelegation {
    @Override
    public void execute(ExecutionContext executionContext) {
        log.info("TripBizPredictDelegation:出行业务预测");
    }
}
