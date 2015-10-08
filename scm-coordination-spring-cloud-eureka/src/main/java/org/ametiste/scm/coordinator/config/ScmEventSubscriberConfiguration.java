package org.ametiste.scm.coordinator.config;

import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration enable Netflix Eureka Client with Spring Cloud wrapper and add metadata
 * (boolean 'eventsSubscriber' field) for selecting subscribers from Eureka Server.
 */
@Configuration
@EnableEurekaClient
@PropertySource("classpath:bootstrap-subscriber.properties")
public class ScmEventSubscriberConfiguration {
}