package com.yasin.contentcenter.configuration;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.core.Balancer;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Yasin Zhang
 */
@Slf4j
public class NacosTargetVersionSameClusterWeightRule extends AbstractLoadBalancerRule {
    @Autowired
    private NacosServiceManager nacosServiceManager;

    @Autowired
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {

    }

    @Override
    public Server choose(Object key) {
        try {
            // 目标元数据版本
            String targetVersion = nacosDiscoveryProperties.getMetadata().get("target-version");

            // 获取配置文件中的集群名称
            String clusterName = nacosDiscoveryProperties.getClusterName();

            BaseLoadBalancer loadBalancer = (BaseLoadBalancer) this.getLoadBalancer();
            // 微服务名称
            String serviceName = loadBalancer.getName();

            // 服务发现组件
            NamingService namingService = nacosServiceManager.getNamingService(nacosDiscoveryProperties.getNacosProperties());

            // step 1. 找到指定服务的所有实例
            List<Instance> instances = namingService.selectInstances(serviceName, true);

            // step 2. 过滤出元数据匹配的实例
            List<Instance> metadataMatchInstance = instances;
            if (StringUtils.isNotBlank(targetVersion)) {
                metadataMatchInstance = instances.stream()
                    .filter(instance -> Objects.equals(instance.getMetadata().get("version"), targetVersion))
                    .collect(Collectors.toList());
                if (CollectionUtils.isEmpty(metadataMatchInstance)) {
                    log.warn("未找到元数据匹配的实例， 当前目标元数据 target-version = {}", targetVersion);
                    return null;
                }
            }

            // step 3. 过滤出相同集群下的所有实例B
            List<Instance> sameClusterInstances = metadataMatchInstance;
            if (StringUtils.isNotBlank(clusterName)) {
                sameClusterInstances = metadataMatchInstance.stream()
                    .filter(instance -> Objects.equals(instance.getClusterName(), clusterName))
                    .collect(Collectors.toList());

                // 发生跨集群调用
                if (CollectionUtils.isEmpty(sameClusterInstances)) {
                    sameClusterInstances = metadataMatchInstance;
                    log.warn("发生跨集群调用, name = {}, clusterName = {}, instance = {}", serviceName, clusterName, sameClusterInstances);
                }
            }

            // step 4. 基于权重的负载均衡算法，返回一个实例
            Instance instance = ExtendBalancer.getHostByRandomWeightEx(sameClusterInstances);
            log.info("选择实例 port = {}, instance = {}", instance.getPort(), instance);

            return new NacosServer(instance);
        } catch (NacosException e) {
            log.error("异常出现", e);
            return null;
        }
    }

    /**
     * 根据权重随机选择实例
     */
    static class ExtendBalancer extends Balancer {
        public static Instance getHostByRandomWeightEx(List<Instance> host) {
            return getHostByRandomWeight(host);
        }
    }
}
