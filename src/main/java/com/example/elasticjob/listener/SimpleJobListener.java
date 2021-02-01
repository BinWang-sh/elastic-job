package com.example.elasticjob.listener;

import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SimpleJobListener implements ElasticJobListener {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleJobListener.class);

    private long beginTime = 0;

    @Override
    public void beforeJobExecuted(ShardingContexts shardingContexts) {
        beginTime = System.currentTimeMillis();
        LOG.info(shardingContexts.getJobName()+"===>开始...");
    }

    @Override
    public void afterJobExecuted(ShardingContexts shardingContexts) {
        long endTime = System.currentTimeMillis();
        LOG.info(shardingContexts.getJobName()+
                "===>结束...[耗时："+(endTime - beginTime)+"]");
    }
}
