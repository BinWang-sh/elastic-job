package com.example.elasticjob.service;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.example.elasticjob.listener.ElJobListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ElasticJobService {

    @Resource
    private ZookeeperRegistryCenter zookeeperRegistryCenter;

    public void addTaskJob(final String jobName,final ElasticJob elJob,
                           final String cron,final int shardCount,final String shardItem) {
        // 配置过程
        JobCoreConfiguration jobCoreConfiguration = JobCoreConfiguration.newBuilder(
                jobName, cron, shardCount)
                .shardingItemParameters(shardItem).build();
        JobTypeConfiguration jobTypeConfiguration;
        if(elJob instanceof DataflowJob) {
            //streamingProcess设为true只有在 fetchData 方法的返回值为 null 或集合容量为空时，才停止抓取
            //streamingProcess false 任务只会在每次作业执行过程中执行一次 fetchData 和 processData 方法
            jobTypeConfiguration = new DataflowJobConfiguration(jobCoreConfiguration,
                    elJob.getClass().getCanonicalName(), false);
        } else {
            jobTypeConfiguration = new SimpleJobConfiguration(jobCoreConfiguration,
                    elJob.getClass().getCanonicalName());
        }
        LiteJobConfiguration liteJobConfiguration = LiteJobConfiguration.newBuilder(
                jobTypeConfiguration).overwrite(true).build();
        ElJobListener taskJobListener = new ElJobListener();
        // 加载执行
        SpringJobScheduler jobScheduler = new SpringJobScheduler(
                elJob, zookeeperRegistryCenter,
                liteJobConfiguration, taskJobListener);
        jobScheduler.init();
    }

}