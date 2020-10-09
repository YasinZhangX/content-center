package com.yasin.contentcenter.configuration;

import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Configuration;
import ribbonconfiguration.RibbonConfiguration;

/**
 * 通过代码配置Ribbon
 * @author Yasin Zhang
 */

@Configuration
//以下是单个服务配置
//@RibbonClient(name = "user-center", configuration = RibbonConfiguration.class)

//以下是全局配置
@RibbonClients(defaultConfiguration = RibbonConfiguration.class)
public class UserCenterRibbonConfiguration {

}
