package com.yasin.contentcenter;

import com.yasin.contentcenter.dao.content.ShareMapper;
import com.yasin.contentcenter.domain.entity.content.Share;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * @author Yasin Zhang
 */
@RestController("/")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TestController {
    private final ShareMapper shareMapper;
    private final DiscoveryClient discoveryClient;

    @GetMapping("test")
    public List<Share> testInsert() {
        Share share = new Share();
        share.setCreateTime(new Date());
        share.setUpdateTime(new Date());
        share.setTitle("xxx");
        share.setCover("xxx");
        share.setAuthor("xxx");
        share.setBuyCount(1);
        shareMapper.insertSelective(share);

        return shareMapper.selectAll();
    }

    /**
     * 测试服务发现
     * @return 返回用户中心所有实例
     */
    @GetMapping("testDiscovery")
    public List<ServiceInstance> getInstances() {
        return discoveryClient.getInstances("user-center");
    }
}
