# SCM Coordination Library

Link to umbrella project: [>> System's Configuration Management <<](https://github.com/ametiste-oss/ametiste-scm)

## Build Status
[![Build Status](https://travis-ci.org/ametiste-oss/ametiste-scm-coordination.svg?branch=master)](https://travis-ci.org/ametiste-oss/ametiste-scm-coordination)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/2f63ae93833b47d5af2dca3c026a81ee)](https://www.codacy.com/app/Ametiste-OSS/ametiste-scm-coordination)
[![codecov.io](https://codecov.io/github/ametiste-oss/ametiste-scm-coordination/coverage.svg?branch=master&precision=2)](https://codecov.io/github/ametiste-oss/ametiste-scm-coordination?branch=master)

## Table Of Content

- [Overview](#overview)
- [Coordination with Netflix Eureka](#coordination-with-netflix-eureka)
  - [Logic](#logic)
    - [Subscriber identification](#subscriber-identification)
    - [Fetching](#fetching)
  - [Usage](#usage)
    - [Subscribing to Event Broadcast](#subscribing-to-event-broadcast)
    - [Fetching Subscribers](#fetching-subscribers) 

## Overview

*SCM Coordination Library* conatins components that organize communication between event publisher and subscriber components.

Number of services that want's receive events can dinamicaly change and to simplify communication SCM include separate 3rd-party component: **Disctributed Coordinator**. Coordinator register subscribers and check their availability, publisher fetch list of subscribers from it. 
It allows reduce connectivity between system components and makes system more more scalable and flexible.

Library provides mechanism for subscribing and interface for fetching subscribers. All details of communication with Coordinator stay behind the scenes.

## Coordination with Netflix Eureka

Library uses *Netflix Eureka Server* as Distributed Coordinator and communicate eith it through Eureka Client. To simplify configuration of client uses [Spring Cloud Netflix Eureka](https://github.com/spring-cloud/spring-cloud-netflix) library.

It imposes restrictions to application use Spring Framework, but in view of its popularity and prevalence it covers wide area of usages. If your application don't use Spring you can provide own communication with Eureka and required [identification logic](#subscriber-identification).

### Logic
#### Subscriber identification
When instance of service register in Eureka it transmit a lot of information: parameters, identifiers, metadata, etc.
For identification of subscribers choosed metadata section. It allows use any Eureka Server without resctictions in naming or else.

To mark application instances as event subscribers in metadata was added fields:

| Name | Type |	Description |
|:-----|:-----|:------------|
|`eventSubscriber`|	Boolean	| Flag that signal that instance is event subscriber and wants to receive events.|
|`eventReceiverUrlPath`| String | Relative path to event receiver endpoint. Use to construct full URL.<br/>:warning: _Important_: path must starts with back slash symbol "/".|

By default, it fields fill with value to enable suscribing. If it's neccessary you can override them.

#### Fetching
If Eureka Client configure with discovery functionality it periodicaly fetch registry from defined server. *Fetcher* get all registered applications and filter instances that have required parameters (see [higher](#subscriber-identification)) and eventSubscriber set to *true*.

After that fetcher construct full URL to instance event receiver endpoint for each subscriber from instance parameters:
```
URL = http://{hostName}:{nonSecurePort}{eventReceiverUrlPath}
```
Host name and port taken from instance info.

### Usage
Library contains module with configurations for Subscriber and Fetcher mechanisms. Configurations assumes usage in Spring Boot applications.
Also it assumes available Eureka Server instance that acts as Distributed Coordinator.

#### Commons
To use Coordination library  you need add to project dependencies *scm-coordination-spring-cloud-eureka.jar* module. 
If you use Gradle add this to build.gradle file:
```groovy
repository {
  jcenter()
}

dependencies {
  compile "org.ametiste.scm:scm-coordination-spring-cloud-eureka:{version}"
}
```

#### Subscribing to Event Broadcast
To subscribe to event broadcasting you need:

* Import *ScmEventSubscriberConfiguration* configuration class to app context.
```java
@Configuration
@EnableAutoConfiguration
@Import(ScmEventSubscriberConfiguration.class)
public class EventSubscriberApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventSubscriberApplication.class, args);
    }
}
```

* Define properties in bootstrap properties file (.yml or .properties):

| Property | Type	| Description |
|:---------|:-----|:------------|
|`spring.application.name`|String|Specify application name in coordinator. Can be used to filtering in fetcher.|
|`eureka.client.serviceUrl.defaultZone`|URL|URL to Eureka Server.<br/>:warning: _Important_: end URL with back slash symbol "/".|

These two properties are required. You can also add other properties to customize eureka client.
```yaml
spring:
  application:
	name: event_log

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```
This is all you need to integrate subscribing mechanism with Eureka.

#### Fetching Subscribers

To integrate subscriber fetcher you need:

* Import *ScmSubscribersFetcherConfiguration* configuration class to app context.
```java
@Configuration
@EnableAutoConfiguration
@Import(ScmSubscribersFetcherConfiguration.class)
public class SubscribersFetcherApplication {
    public static void main(String[] args) {
        SpringApplication.run(SubscribersFetcherApplication.class, args);
    }
}
```

* Define properties in bootstrap properties file (.yml or .properties). Same as in subscribing to event broadcasting.

After that you can autowire EventSubscribersFetcher bean and use as you need.
```java
@RestController
public class Controller {
 
    @Autowired
    private EventSubscribersFetcher eventSubscribersFetcher;
 
    @RequestMapping(method = RequestMethod.GET, value = "/subscribers")
    public Collection<URI> subscribers() {
        return eventSubscribersFetcher.fetchSubscribers();
    }
}
```
