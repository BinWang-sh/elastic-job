package com.example.elasticjob.config;

import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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