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
@Slf4j
@Component
public class BiPlatformConfirmDelegation implements JavaDelegation {
    @Override
    public void execute(ExecutionContext executionContext) {
        log.info("BiPlatformConfirmDelegation:Bi平台确认");
    }
}
