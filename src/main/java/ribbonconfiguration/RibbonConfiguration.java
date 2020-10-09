package ribbonconfiguration;

import com.netflix.loadbalancer.IRule;
import com.yasin.contentcenter.configuration.NacosTargetVersionSameClusterWeightRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Yasin Zhang
 */
@Configuration
public class RibbonConfiguration {

    @Bean
    public IRule ribbonRule() {
        return new NacosTargetVersionSameClusterWeightRule();
    }

}
