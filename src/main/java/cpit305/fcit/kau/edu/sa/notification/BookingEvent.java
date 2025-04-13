package cpit305.fcit.kau.edu.sa.notification;

import java.time.LocalDateTime;

public class BookingEvent {
    private final String passengerName;
    private final int seatNumber;
    private final LocalDateTime bookingTime;

    public BookingEvent(String passengerName, int seatNumber) {
        this.passengerName = passengerName;
        this.seatNumber = seatNumber;
        this.bookingTime = LocalDateTime.now();
    }

    public String getPassengerName() {
        return passengerName;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public String getBookingTime() {
        return bookingTime.toString();
    }

    @Override
    public String toString() {
        return "Booking Details:\n" +
                "\tpassengerName:" + getPassengerName() + "\n" +
                "\tseatNumber:" + getSeatNumber() + "\n" +
                "\tbooking time:" + getBookingTime() + "\n";

    }
}