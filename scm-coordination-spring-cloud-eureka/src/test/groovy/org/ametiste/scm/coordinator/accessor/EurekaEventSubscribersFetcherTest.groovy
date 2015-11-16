package org.ametiste.scm.coordinator.accessor

import com.netflix.appinfo.InstanceInfo
import com.netflix.discovery.DiscoveryClient
import com.netflix.discovery.shared.Application
import com.netflix.discovery.shared.Applications
import spock.lang.Specification

class EurekaEventSubscribersFetcherTest extends Specification {

    List<URI> subscriberHosts = [
            URI.create("http://some-service.org:8085/event-receiver"),
            URI.create("http://localhost:8082/event-receiver")
    ]

    InstanceInfo invalidSubscriber = InstanceInfo.Builder.newBuilder().setAppName("invalid")
            .setHostName("invalid.org").add("eventSubscriber", "true").build();
    InstanceInfo disableSubscriber = InstanceInfo.Builder.newBuilder().setAppName("disabled")
            .setHostName("disable.com").add("eventSubscriber", "false").build();
    InstanceInfo other_application = InstanceInfo.Builder.newBuilder().setAppName("other")
            .setHostName("other.gov").build();

    List<InstanceInfo> validSubscribers = [
            InstanceInfo.Builder.newBuilder().setAppName("discovery").setHostName(subscriberHosts[0].getHost())
                    .setPort(subscriberHosts[0].getPort())
                    .add("eventSubscriber", "true").add("eventReceiverUrlPath", subscriberHosts[0].getPath())
                    .build(),
            InstanceInfo.Builder.newBuilder().setAppName("log").setHostName(subscriberHosts[1].getHost())
                    .setPort(subscriberHosts[1].getPort())
                    .add("eventSubscriber", "true").add("eventReceiverUrlPath", subscriberHosts[1].getPath())
                    .build()
    ]


    EventSubscribersFetcher fetcher;
    DiscoveryClient client;

    def setup() {
        client = Mock(DiscoveryClient);
        fetcher = new EurekaEventSubscribersFetcher(client);
    }

    def "arguments validation on creation"() {
        when: "try create fetcher with not initialized discovery client"
        new EurekaEventSubscribersFetcher(null)

        then: "expect IllegalArgumentException thrown"
        thrown(IllegalArgumentException.class)
    }

    def "fetcher should return empty list when no registered subscribers"() {
        given: "applications list without subscribers"
        Application app1 = new Application("discoveryApps");
        app1.addInstance(invalidSubscriber);
        app1.addInstance(disableSubscriber);

        Application app2 = new Application("logApps");
        app2.addInstance(other_application);
        app2.addInstance(disableSubscriber);

        Applications apps = new Applications([app1, app2])

        when: "fetch subscribers"
        def subscribers = fetcher.fetchSubscribers()

        then: "discovery client return applications"
        1 * client.getApplications() >> apps

        and: "fetcher return empty list"
        subscribers.isEmpty()
    }

    def "fetcher should return collection of subscriber event receiver endpoint URIs"() {
        given: "applications list with valid subscribers"
        Application app1 = new Application("discoveryApps");
        app1.addInstance(invalidSubscriber);
        app1.addInstance(validSubscribers[0]);

        Application app2 = new Application("logApps");
        app2.addInstance(other_application);
        app2.addInstance(validSubscribers[1]);

        Applications apps = new Applications([app1, app2])

        when: "fetch subscribers"
        def subscribers = fetcher.fetchSubscribers()

        then: "discovery client return applications"
        1 * client.getApplications() >> apps

        and: "fetcher returns list with subscribers URIs"
        subscribers.size() == 2
        subscribers.contains(subscriberHosts[0])
        subscribers.contains(subscriberHosts[1])
    }

    def "fetcher should filter list of instances with regex pattern"() {
        given: "applications list with valid subscribers"
        Application app1 = new Application("discovery");
        app1.addInstance(invalidSubscriber);
        app1.addInstance(validSubscribers[0]);

        Application app2 = new Application("log");
        app2.addInstance(other_application);
        app2.addInstance(validSubscribers[1]);

        Applications apps = new Applications([app1, app2])

        when: "fetch subscribers"
        def subscribers = fetcher.fetchSubscribers("^log[a-z0-9]*");

        then: "discovery client return applications"
        1 * client.getApplications() >> apps

        and: "fetcher returns list with subscribers URIs"
        subscribers.size() == 1
        subscribers.contains(subscriberHosts[1])
    }
}
