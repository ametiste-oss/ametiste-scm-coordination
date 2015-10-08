package org.ametiste.scm.coordinator.accessor;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.shared.Application;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.Validate.isTrue;

/**
 * {@code EurekaEventSubscribersFetcher} implements {@code EventSubscribersFetcher} interface and provide functionality
 * for taking SCM event subscribers from Netflix Eureka Server.
 * <p>
 * Fetcher use Netflix {@code DiscoveryClient} to communicate with Eureka Server.
 * <p>
 * Fetcher get all registered application from server and filter only those who has specified boolean field with value
 * {@code true} and event receiver url path in metadata section.
 * This criteria allow us don't bother in naming of application and constructing event receiver endpoint url.
 */
public class EurekaEventSubscribersFetcher implements EventSubscribersFetcher {

    public static final String SUBSCRIBER_METADATA_FIELD = "eventSubscriber";
    public static final String EVENT_RECEIVER_URL_PATH_FIELD = "eventReceiverUrlPath";

    private final DiscoveryClient discoveryClient;

    /**
     * Create instance of {@code EurekaEventSubscribersFetcher}.
     * @param discoveryClient Netflix {@code DiscoveryClient} instance to communicate with Eureka Server.
     */
    public EurekaEventSubscribersFetcher(DiscoveryClient discoveryClient) {
        isTrue(discoveryClient != null, "'discoveryClient' must be initialized");

        this.discoveryClient = discoveryClient;
    }

    @Override
    public Collection<URI> fetchSubscribers() throws FetchSubscribersException {
        return registeredEventSubscribersStream()
                .map(this::createReceiverEndpointUrl)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Fetch subscribers from coordinator that application name match to specified regexp pattern.
     * Application name transforms to lower case before comparing.
     * @param pattern string that contains regular expression for filtering.
     * @return collection of URIs for messaging with subscribers. If subscribers absent returns empty collection.
     */
    @Override
    public Collection<URI> fetchSubscribers(String pattern) throws FetchSubscribersException {
        return registeredEventSubscribersStream()
                .filter(i -> Pattern.matches(pattern, i.getAppName().toLowerCase()))
                .map(this::createReceiverEndpointUrl)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * @return {@code InstanceInfo} stream of valid registered event subscribers.
     */
    private Stream<InstanceInfo> registeredEventSubscribersStream() {
        return discoveryClient.getApplications().getRegisteredApplications().stream()
                .map(Application::getInstances)
                .flatMap(List::stream)
                .filter(i -> i.getMetadata().containsKey(SUBSCRIBER_METADATA_FIELD))
                .filter(i -> Boolean.valueOf(i.getMetadata().get(SUBSCRIBER_METADATA_FIELD)));
    }

    private URI createReceiverEndpointUrl(InstanceInfo info) {
        if (info.getHostName() != null && info.getMetadata().containsKey(EVENT_RECEIVER_URL_PATH_FIELD)) {
            return URI.create(String.format("http://%s:%d%s", info.getHostName(), info.getPort(),
                    info.getMetadata().get(EVENT_RECEIVER_URL_PATH_FIELD)));
        }
        return null;
    }
}
