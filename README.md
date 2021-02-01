# elastic-job

## 概述
Elastic-Job是一个分布式调度解决方案，由两个相互独立的子项目Elastic-Job-Lite和Elastic-Job-Cloud组成。Elastic-Job-Lite定位为轻量级无中心化解决方案，使用jar包的形式提供分布式任务的协调服务
任务类型分为：SimpleJob、DataflowJob、ScriptJob

### 功能特点
* 分布式调度
* 协调弹性扩容缩容
* 失效转移
* 错过执行
* 作业重触发作业分片一致性，保证同一分片在分布式环境中仅一个执行实例


### 在运行之前需要搭建zookeeper。如下是我实验使用的docker-compose.yml
```
version: '3.1'

services:
  zoo1:
    image: zookeeper:3.5.8
    restart: always
    hostname: zoo1
    ports:
      - 2181:2181
    environment:
      ZOO_MY_ID: 1
      ZOO_SERVERS: server.1=0.0.0.0:2888:3888;2181 server.2=zoo2:2888:3888;2181 server.3=zoo3:2888:3888;2181

  zoo2:
    image: zookeeper:3.5.8
    restart: always
    hostname: zoo2
    ports:
      - 2182:2181
    environment:
      ZOO_MY_ID: 2
      ZOO_SERVERS: server.1=zoo1:2888:3888;2181 server.2=0.0.0.0:2888:3888;2181 server.3=zoo3:2888:3888;2181

  zoo3:
    image: zookeeper:3.5.8
    restart: always
    hostname: zoo3
    ports:
      - 2183:2181
    environment:
      ZOO_MY_ID: 3
      ZOO_SERVERS: server.1=zoo1:2888:3888;2181 server.2=zoo2:2888:3888;2181 server.3=0.0.0.0:2888:3888;2181
```      
执行docker-compose up，启动
执行docker ps可以看到如下内容
```
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                                                  NAMES
9cbec7f0012f        zookeeper:3.5.8     "/docker-entrypoint.…"   2 days ago          Up 2 days           2888/tcp, 3888/tcp, 0.0.0.0:2181->2181/tcp, 8080/tcp   zookeeper_zoo1_1
25ed26983a39        zookeeper:3.5.8     "/docker-entrypoint.…"   2 days ago          Up 2 days           2888/tcp, 3888/tcp, 8080/tcp, 0.0.0.0:2183->2181/tcp   zookeeper_zoo3_1
e5002064ade5        zookeeper:3.5.8     "/docker-entrypoint.…"   2 days ago          Up 2 days           2888/tcp, 3888/tcp, 8080/tcp, 0.0.0.0:2182->2181/tcp   zookeeper_zoo2_1
```

### application.yml中定义zookeeper配置
```
zookeeper:
  address: 192.168.6.132:2181
  namespace: elastic-job
  connectionTimeout: 10000
  sessionTimeout: 10000
  maxRetries: 3
```

### 自定义一个注解
```
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ElasticSimpleJob {

    @AliasFor("cron")
    public abstract String value() default "";

    @AliasFor("value")
    public abstract String cron() default "";

    public abstract String jobName() default "";

    public abstract int shardingTotalCount() default 1;

    public abstract String shardingItemParameter() default "";

    public abstract String jobParameter() default "";

    public abstract String description() default "";

    public abstract boolean disabled() default false;

    public abstract boolean overwrite() default false;

    public abstract boolean failover() default true;

    public abstract boolean monitorExecution() default true;
}
```

### 实例化注册中心
```
@Configuration
@ConditionalOnExpression("'${zookeeper.address}'.length() > 0")
public class RegistryCenterConfig {

    /**
     * 加载注册中心到spring容器
     * @return
     */
    @Bean(initMethod = "init")
    public ZookeeperRegistryCenter registryCenter(@Value("${zookeeper.address}") final String serverLists,
                                                  @Value("${zookeeper.namespace}") final String namespace,
                                                  @Value("${zookeeper.connectionTimeout}") final int connectionTimeout,
                                                  @Value("${zookeeper.sessionTimeout}") final int sessionTimeout,
                                                  @Value("${zookeeper.maxRetries}") final int maxRetries) {
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration(serverLists, namespace);
        zookeeperConfiguration.setConnectionTimeoutMilliseconds(connectionTimeout);
        zookeeperConfiguration.setSessionTimeoutMilliseconds(sessionTimeout);
        zookeeperConfiguration.setMaxRetries(maxRetries);

        return new ZookeeperRegistryCenter(zookeeperConfiguration);

    }
}
```

### 自定义任务1
```
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
```

### 自定义任务2
```
@Component
@ElasticSimpleJob(jobName = "DynamicElasticJob002", shardingTotalCount = 3, shardingItemParameter = "0=java,1=C,2=goLang", cron = "0/1 * * * * ?", description = "动态添加任务002")
public class ShowInfoSimpleJob implements SimpleJob {

    private static final Logger LOG = LoggerFactory.getLogger(ShowInfoSimpleJob.class.getName());

    @Override
    public void execute(ShardingContext shardingContext) {
        LOG.info("**Current Thread: "+Thread.currentThread().getId());
        LOG.info("**Sharding Total Count："+shardingContext.getShardingTotalCount());
        LOG.info("**Current Shard："+shardingContext.getShardingItem());
        LOG.info("**Shard Parameter："+shardingContext.getShardingParameter());
        LOG.info("**Task Parameter："+shardingContext.getJobParameter());
        LOG.info("**Executing: " + shardingContext.getJobName());
    }
}
```

### 定义任务执行Listener
```
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
```

### 动态执行添加的任务
```
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
```

> #### LiteJobConfiguration的overwrite()要设置为true，否则所有任务都会按照第一个任务的设置执行
```
                LiteJobConfiguration liteJobConfiguration = LiteJobConfiguration
                        .newBuilder(simpleJobConfiguration).overwrite(true)
                        .monitorExecution(monitorExecution).build();
```


## 数据流作业
用于处理数据流，需实现 DataflowJob 接口。 DataflowJob接口有两个方法，分别用于抓取 (fetchData) 和处理 (processData) 数据
配置 streaming.process 开启或关闭流式处理。

流式处理开启时，任务只有在 fetchData 方法的返回值为 null 或集合容量为空时，才停止抓取
流式处理关闭时，任务只会在每次作业执行过程中执行一次 fetchData 和 processData 方法，随即完成本次作业。

```
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
```

对外提供添加任务的API
```
@RestController
public class ElasticJobController {

    @Resource
    private ElasticJobService taskJobService;

    @RequestMapping("/addJob")
    public String addJob(@RequestParam("cron") String cron,
                         @RequestParam("jobName") String jobName,
                         @RequestParam("jobType") Integer jobType,
                         @RequestParam("shardCount") Integer shardCount,
                         @RequestParam("shardItem") String shardItem) {
        ElasticJob elJob;

        if(jobType == 0) {
            elJob = new ShowInfoSimpleJob();
        } else if(jobType == 1) {
            elJob = new MyDataFlowJob();
        } else {
            return "JobType error!";
        }

        taskJobService.addTaskJob(jobName, elJob, cron, shardCount, shardItem);
        return "success";
    }
}
```

添加任务service
```
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
```


## elastic-job
去官网下载ElasticJob-Lite-UI二进制包，解压。
在conf目录下是配置目录
在bin目录下执行./start.sh
浏览器访问地址 http://localhost:8088/

在register center添加zookeeper地址，点击连接后，在server dimension中可以看到运行的任务
可以对任务进行停止操作







