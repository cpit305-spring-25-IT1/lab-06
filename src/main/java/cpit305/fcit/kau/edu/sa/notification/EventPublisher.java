package cpit305.fcit.kau.edu.sa.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;

public class EventPublisher {
    private Queue<BookingEvent> eventQueue;
    private List<EventSubscriber> subscribers;
    private volatile boolean running;
    private Thread publisherThread;

    public EventPublisher() {
        this.eventQueue = new LinkedList<>();
        this.subscribers = new ArrayList<>();
    }

    /**
     * Registers a subscriber to receive booking events
     * @param subscriber The subscriber to register
     */
    public void registerSubscriber(EventSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    /**
     * Unregisters a subscriber
     * @param subscriber The subscriber to unregister
     */
    public void unregisterSubscriber(EventSubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    /**
     * Publishes a booking event to all subscribers
     * @param event The booking event to publish
     */
    public void publishEvent(BookingEvent event) {
        // Add the event to the queue
        eventQueue.add(event);

        // Manually check if queue isn't empty and process one event
        if (!eventQueue.isEmpty()) {
            BookingEvent nextEvent = eventQueue.poll();
            if (nextEvent != null) {
                notifySubscribers(nextEvent);
            }
        }
    }

    /**
     * Notify all subscribers about an event
     * @param event The event to notify subscribers about
     */
    private void notifySubscribers(BookingEvent event) {
        for (EventSubscriber subscriber : subscribers) {
            try {
                subscriber.onEvent(event);
            } catch (Exception e) {
                System.err.println("Error notifying subscriber " + subscriber.getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Starts the publisher thread
     */
    public void start() {
        if (running) {
            return;
        }

        running = true;
        publisherThread = new Thread(() -> {
            while (running) {
                // Periodically check if there are events in the queue
                if (!eventQueue.isEmpty()) {
                    BookingEvent event = eventQueue.poll();
                    if (event != null) {
                        notifySubscribers(event);
                    }
                }

                // Sleep a bit to reduce CPU usage
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        publisherThread.setDaemon(true);
        publisherThread.start();
    }

    /**
     * Stops the publisher thread
     */
    public void stop() {
        running = false;
        if (publisherThread != null) {
            publisherThread.interrupt();
        }
    }

    /**
     * Returns the current size of the event queue
     * @return The number of events in the queue
     */
    public int getQueueSize() {
        return eventQueue.size();
    }
}