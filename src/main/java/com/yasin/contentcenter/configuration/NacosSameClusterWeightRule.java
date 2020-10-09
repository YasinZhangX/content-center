package com.yasin.contentcenter.configuration;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.core.Balancer;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 同一集群优先调用
 * @author Yasin Zhang
 */
@Slf4j
public class NacosSameClusterWeightRule extends AbstractLoadBalancerRule {

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
            // 获取配置文件中的集群名称
            String clusterName = nacosDiscoveryProperties.getClusterName();

            BaseLoadBalancer loadBalancer = (BaseLoadBalancer) this.getLoadBalancer();
            // 微服务名称
            String serviceName = loadBalancer.getName();

            // 服务发现
            NamingService namingService = nacosServiceManager.getNamingService(nacosDiscoveryProperties.getNacosProperties());

            // step 1. 找到指定服务的所有实例A
            List<Instance> instances = namingService.selectInstances(serviceName, true);

            // step 2. 过滤出相同集群下的所有实例B
            List<Instance> sameClusterInstances = instances.stream()
                .filter(instance -> Objects.equals(instance.getClusterName(), clusterName))
                .collect(Collectors.toList());

            // step 3. 如果B为空，就用A
            List<Instance> instancesToBeChosen;
            if (sameClusterInstances.isEmpty()) {
                instancesToBeChosen = instances;
                log.warn("发生跨集群调用, name = {}, clusterName = {}, instance = {}", serviceName, clusterName, instancesToBeChosen);
            } else {
                instancesToBeChosen = sameClusterInstances;
            }

            // step 4. 基于权重的负载均衡算法，返回一个实例
            Instance instance = ExtendBalancer.getHostByRandomWeightEx(instancesToBeChosen);
            log.info("选择实例 port = {}, instance = {}", instance.getPort(), instance);

            return new NacosServer(instance);
        } catch (NacosException e) {
            log.error("异常出现", e);
            return null;
        }
    }

    static class ExtendBalancer extends Balancer {
        public static Instance getHostByRandomWeightEx(List<Instance> host) {
            return getHostByRandomWeight(host);
        }
    }
}
