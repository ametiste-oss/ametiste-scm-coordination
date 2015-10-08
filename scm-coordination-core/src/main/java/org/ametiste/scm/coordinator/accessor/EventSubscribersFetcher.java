package org.ametiste.scm.coordinator.accessor;

import java.net.URI;
import java.util.Collection;

/**
 * {@code EventSubscribersFetcher} interface provides protocol to communicate with distributed coordinator for taking
 * actual list of subscribers.
 */
public interface EventSubscribersFetcher {

    /**
     * Fetch all actual subscribers from coordinator.
     * @return collection of URIs for messaging with subscribers. If subscribers absent returns empty collection.
     */
    Collection<URI> fetchSubscribers() throws FetchSubscribersException;

    /**
     * Fetch subscribers from coordinator that match to specified pattern.
     * Each implementation define own realization of filtration.
     * @param pattern non empty string with match pattern (suffix, regexp, etc.)
     * @return collection of URIs for messaging with subscribers. If subscribers absent returns empty collection.
     */
    Collection<URI> fetchSubscribers(String pattern) throws FetchSubscribersException;
}