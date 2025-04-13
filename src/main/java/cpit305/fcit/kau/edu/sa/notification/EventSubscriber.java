package cpit305.fcit.kau.edu.sa.notification;

public interface EventSubscriber {
    /**
     * Called when a new booking event is published
     * @param event The booking event
     */
    void onEvent(BookingEvent event);

    /**
     * Gets the name of this subscriber
     * @return The subscriber name
     */
    String getName();
}