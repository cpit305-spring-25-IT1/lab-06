package cpit305.fcit.kau.edu.sa.notification;

public class AnalyticsSubscriber implements EventSubscriber {

    @Override
    public void onEvent(BookingEvent event) {
        System.out.println("\n📋 Booking Notification:");
        System.out.println("🎫 Seat " + event.getSeatNumber() + " booked for " + event.getPassengerName());
        System.out.println("🕒 Time: " + event.getBookingTime());
    }

    @Override
    public String getName() {
        return "Display Subscriber";
    }
}