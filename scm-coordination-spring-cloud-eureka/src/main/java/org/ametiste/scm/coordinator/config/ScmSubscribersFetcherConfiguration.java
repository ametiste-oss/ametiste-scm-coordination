package org.ametiste.scm.coordinator.config;

import org.ametiste.scm.coordinator.accessor.EurekaEventSubscribersFetcher;
import org.ametiste.scm.coordinator.accessor.EventSubscribersFetcher;
import com.netflix.discovery.DiscoveryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration contains ready for use subscriber fetcher bean and all needed dependencies for it.
 * <p>
 * Fetcher autowire {@code DiscoveryClient} bean that was created by Spring Cloud context.
 */
@Configuration
@EnableEurekaClient
@PropertySource("classpath:bootstrap-fetcher.properties")
public class ScmSubscribersFetcherConfiguration {

    @Bean
    @Autowired
    public EventSubscribersFetcher eurekaEventSubscribersFetcher(DiscoveryClient client) {
        return new EurekaEventSubscribersFetcher(client);
    }
}
