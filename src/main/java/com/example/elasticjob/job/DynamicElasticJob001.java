package com.example.elasticjob.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.example.elasticjob.abs.ElasticSimpleJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@ElasticSimpleJob(jobName = "DynamicElasticJob001", shardingTotalCount = 5, shardingItemParameter = "0=apple,1=orange,2=pear,3=strawberry,4=banana", cron = "0/1 * * * * ?", description = "动态添加任务001")
public class DynamicElasticJob001 implements SimpleJob {

    private static final Logger LOG = LoggerFactory.getLogger(ShowInfoSimpleJob.class.getName());

    @Override
    public void execute(ShardingContext shardingContext) {
        try {
            LOG.info("---Current Thread: "+Thread.currentThread().getId());
            LOG.info("---Sharding Total Count："+shardingContext.getShardingTotalCount());
            LOG.info("---Current Shard："+shardingContext.getShardingItem());
            LOG.info("---Shard Parameter："+shardingContext.getShardingParameter());
            LOG.info("---Task Parameter："+shardingContext.getJobParameter());
            LOG.info("---Executing: " + shardingContext.getJobName());
        } catch (Exception e) {
            LOG.error("==>任务发生异常!",e);
        }
    }
}