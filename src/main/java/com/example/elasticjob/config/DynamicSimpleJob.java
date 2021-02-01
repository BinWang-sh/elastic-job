package com.example.elasticjob.config;

import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.example.elasticjob.abs.ElasticSimpleJob;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;

@Configuration
public class DynamicSimpleJob {

    @Resource
    private ZookeeperRegistryCenter registryCenter;

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void initElasticJob() {

        Map<String, SimpleJob> map = applicationContext.getBeansOfType(SimpleJob.class);

        for (Map.Entry<String, SimpleJob> entry : map.entrySet()) {
            SimpleJob simpleJob = entry.getValue();
            if (AopUtils.isAopProxy(simpleJob)) {
                try {
                    simpleJob = (SimpleJob) ((Advised) simpleJob).getTargetSource().getTarget();
                } catch (Exception e) {
                    throw new RuntimeException("==>代理类转换异常!", e);
                }
            }

            ElasticSimpleJob elasticSimpleJobAnnotation = simpleJob.getClass().getAnnotation(ElasticSimpleJob.class);

            if (null != elasticSimpleJobAnnotation) {
                String cron = StringUtils.defaultIfBlank(elasticSimpleJobAnnotation.cron(),
                        elasticSimpleJobAnnotation.value());
                String jobName = StringUtils.isBlank(elasticSimpleJobAnnotation.jobName()) ? simpleJob
                        .getClass().getName() : elasticSimpleJobAnnotation.jobName();
                boolean overwrite = elasticSimpleJobAnnotation.overwrite() ? true : false;
                boolean monitorExecution = elasticSimpleJobAnnotation.monitorExecution() ? true : false;

                boolean failover = elasticSimpleJobAnnotation.failover();
                int shardingTotalCount = elasticSimpleJobAnnotation.shardingTotalCount();
                String jobParameter = elasticSimpleJobAnnotation.jobParameter();
                String shardingItemParameters = elasticSimpleJobAnnotation.shardingItemParameter();

                JobCoreConfiguration jobCoreConfiguration = JobCoreConfiguration
                        .newBuilder(simpleJob.getClass().getName(), cron, shardingTotalCount)
                        .misfire(true)
                        .failover(failover)
                        .jobParameter(jobParameter)
                        .shardingItemParameters(shardingItemParameters)
                        .build();

                //SimpleJob任务配置
                SimpleJobConfiguration simpleJobConfiguration = new SimpleJobConfiguration(
                        jobCoreConfiguration,
                        simpleJob.getClass().getCanonicalName());

                LiteJobConfiguration liteJobConfiguration = LiteJobConfiguration
                        .newBuilder(simpleJobConfiguration).overwrite(true)
                        .monitorExecution(monitorExecution).build();

                SpringJobScheduler jobScheduler = new SpringJobScheduler(simpleJob, registryCenter,
                        liteJobConfiguration);
                jobScheduler.init();

            }
        }
    }
}
