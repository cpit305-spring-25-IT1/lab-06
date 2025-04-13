package cpit305.fcit.kau.edu.sa;

import cpit305.fcit.kau.edu.sa.notification.BookingEvent;
import cpit305.fcit.kau.edu.sa.notification.EventPublisher;
import cpit305.fcit.kau.edu.sa.notification.EventSubscriber;
import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class BookingNotificationSystemTest {

    @Test
    public void testEventLoss() throws InterruptedException {
        EventPublisher publisher = new EventPublisher();
        CountingSubscriber subscriber = new CountingSubscriber();

        publisher.registerSubscriber(subscriber);
        publisher.start();

        // Rapidly publish 1000 events
        int eventCount = 1000;
        for (int i = 0; i < eventCount; i++) {
            publisher.publishEvent(new BookingEvent("Passenger-" + i, i % 100 + 1));
        }

        // Wait a bit for processing
        Thread.sleep(2000);
        publisher.stop();

        System.out.println("Events published: " + eventCount);
        System.out.println("Events received: " + subscriber.getCount());

        // Check if all events were received
        // This will likely fail with the problematic implementation
        assertEquals(eventCount, subscriber.getCount(),
                "Not all events were processed. Events lost: " + (eventCount - subscriber.getCount()));
    }

    @Test
    public void testConcurrentAccess() throws InterruptedException {
        EventPublisher publisher = new EventPublisher();
        SlowSubscriber slowSubscriber = new SlowSubscriber();

        publisher.registerSubscriber(slowSubscriber);
        publisher.start();

        // Create multiple threads that publish events
        int threadCount = 10;
        int eventsPerThread = 20;
        CountDownLatch latch = new CountDownLatch(threadCount);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < eventsPerThread; j++) {
                        publisher.publishEvent(
                                new BookingEvent("Passenger-" + threadId + "-" + j, j % 100 + 1));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all threads to finish publishing
        latch.await();

        // Wait for events to be processed
        Thread.sleep(3000);

        // Stop the publisher
        publisher.stop();

        // Check the final queue size
        System.out.println("Expected events: " + (threadCount * eventsPerThread));
        System.out.println("Received events: " + slowSubscriber.getCount());
        System.out.println("Remaining in queue: " + publisher.getQueueSize());

        // Ideal case: queue should be empty and all events processed
        // With the problematic implementation, this may not be the case
        assertEquals(0, publisher.getQueueSize(), "Event queue should be empty");
        assertEquals(threadCount * eventsPerThread, slowSubscriber.getCount(),
                "All events should be processed");
    }

    private static class CountingSubscriber implements EventSubscriber {
        private final AtomicInteger count = new AtomicInteger(0);

        @Override
        public void onEvent(BookingEvent event) {
            count.incrementAndGet();
        }

        @Override
        public String getName() {
            return "Counting Subscriber";
        }

        public int getCount() {
            return count.get();
        }
    }

    private static class SlowSubscriber implements EventSubscriber {
        private final AtomicInteger count = new AtomicInteger(0);

        @Override
        public void onEvent(BookingEvent event) {
            // Simulate slow processing
            try {
                Thread.sleep(50);
                count.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public String getName() {
            return "Slow Subscriber";
        }

        public int getCount() {
            return count.get();
        }
    }
}
