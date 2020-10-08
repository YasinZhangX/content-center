package com.yasin.contentcenter.service.content;

import com.yasin.contentcenter.dao.content.ShareMapper;
import com.yasin.contentcenter.domain.dto.content.ShareDTO;
import com.yasin.contentcenter.domain.dto.user.UserDTO;
import com.yasin.contentcenter.domain.entity.content.Share;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author Yasin Zhang
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ShareService {

    private final ShareMapper shareMapper;
    private final RestTemplate restTemplate;

    /**
     * 获取分享详情
     */
    public ShareDTO findById(Integer id) {
        // 获取分享详情
        Share share = shareMapper.selectByPrimaryKey(id);

        // 发布人ID
        Integer userId = share.getUserId();

        UserDTO userDTO = restTemplate.getForObject("http://user-center/users/{userId}", UserDTO.class, userId);

        // 消息装配
        ShareDTO shareDTO = new ShareDTO();
        BeanUtils.copyProperties(share, shareDTO);
        shareDTO.setWxNickName(userDTO.getWxNickname());

        return shareDTO;
    }



}
