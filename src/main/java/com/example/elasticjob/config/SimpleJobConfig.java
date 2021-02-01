package com.example.elasticjob.config;

import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.example.elasticjob.job.ShowInfoSimpleJob;
import com.example.elasticjob.listener.SimpleJobListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class SimpleJobConfig {
    @Autowired
    private ZookeeperRegistryCenter registryCenter;

    /**
     * 执行定时任务
     *
     * @return
     */
    //@Bean
    public SimpleJob simpleJob() {
        return new ShowInfoSimpleJob();
    }

    /**
     * @param simpleJob
     * @return
     */
    //@Bean(initMethod = "init")
    public JobScheduler simpleJobScheduler(final SimpleJob simpleJob,
                                           @Value("${simpleJob.cron}") final String cron,
                                           @Value("${simpleJob.shardingTotalCount}") final int shardingTotalCount,
                                           @Value("${simpleJob.shardingItemParameters}") final String shardingItemParameters,
                                           @Value("${simpleJob.jobParameter}") final String jobParameter,
                                           @Value("${simpleJob.failover}") final boolean failover,
                                           @Value("${simpleJob.monitorExecution}") final boolean monitorExecution,
                                           @Value("${simpleJob.monitorPort}") final int monitorPort,
                                           @Value("${simpleJob.maxTimeDiffSeconds}") final int maxTimeDiffSeconds,
                                           @Value("${simpleJob.jobShardingStrategyClass}") final String jobShardingStrategyClass) {

        JobCoreConfiguration jobCoreConfiguration = JobCoreConfiguration
                .newBuilder(simpleJob.getClass().getName(), cron, shardingTotalCount)
                .misfire(true)
                .failover(failover)
                .jobParameter(jobParameter)
                .shardingItemParameters(shardingItemParameters)
                .build();

        SimpleJobConfiguration simpleJobConfiguration = new SimpleJobConfiguration(jobCoreConfiguration, simpleJob.getClass().getCanonicalName());

        LiteJobConfiguration liteJobConfiguration = LiteJobConfiguration.newBuilder(simpleJobConfiguration)
                .jobShardingStrategyClass(jobShardingStrategyClass)
                .monitorExecution(monitorExecution)
                .monitorPort(monitorPort)
                .maxTimeDiffSeconds(maxTimeDiffSeconds)
                .overwrite(true)
                .build();

        return new SpringJobScheduler(simpleJob,
                registryCenter,
                liteJobConfiguration,
                new SimpleJobListener());

    }
}