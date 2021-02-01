package com.example.elasticjob.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.example.elasticjob.abs.ElasticSimpleJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class MyDataFlowJob implements DataflowJob<String> {

    private static final Logger LOG = LoggerFactory.getLogger(MyDataFlowJob.class.getName());

    @Override
    public List<String> fetchData(ShardingContext shardingContext) {
        LOG.info("@@@@@@Current Thread: "+Thread.currentThread().getId());
        LOG.info("@@@@@@Sharding Total Count："+shardingContext.getShardingTotalCount());
        LOG.info("@@@@@@Current Shard："+shardingContext.getShardingItem());
        LOG.info("@@@@@@Shard Parameter："+shardingContext.getShardingParameter());
        LOG.info("@@@@@@Task Parameter："+shardingContext.getJobParameter());
        LOG.info("@@@@@@Executing: " + shardingContext.getJobName());
        LOG.info("@@@@@@Start fetching data");
        return Arrays.asList("Tom", "Jerry", "Mike");
    }

    @Override
    public void processData(ShardingContext shardingContext, List<String> data) {
        for (String val : data) {
            LOG.info("@@@@@@@Processing data:" + val);
        }
        LOG.info("   ");
    }
}
