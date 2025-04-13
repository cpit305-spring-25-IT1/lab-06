package cpit305.fcit.kau.edu.sa;

import cpit305.fcit.kau.edu.sa.utils.Utils;

import java.util.*;

public class TicketBookingSystem {
    private static final int TOTAL_SEATS = 100;

    // Each seat can only be booked once. Using HashSet will prevent duplicate entries.
    private final Set<Integer> bookedSeats = new HashSet<>();
    /**
     * Books a seat for a passenger. Assign an available seat to a passenger.
     * @param passengerName Name of the passenger
     * @return The seat number that was booked, or -1 if no seats are available
     */
    public int bookSeat(String passengerName) {
        if (bookedSeats.size() >= TOTAL_SEATS) {
            return -1; // No seats available
        }

        // Find the first available seat.
        int seatNumber = -1;
        for (int i = 1; i <= TOTAL_SEATS; i++) {
            if (!bookedSeats.contains(i)) {
                seatNumber = i;
                break;
            }
        }
        // Simulate slow network latency to increase the chance of race condition
//        Utils.simulateNetworkLatency();
        if (seatNumber == -1){
            System.out.println("No seats available. The train is fully booked.");
        } else{
            // add the seat number to the already booked seats
            bookedSeats.add(seatNumber);
            System.out.println("Seat " + seatNumber + " booked for " + passengerName);
        }
        return seatNumber;
    }

    /**
     * Gets the total number of seats currently booked
     * @return The number of booked seats
     */
    public int getBookedSeatsCount() {
        return bookedSeats.size();
    }

    /**
     * Checks if a specific seat is booked
     * @param seatNumber The seat number to check
     * @return true if the seat is booked, false otherwise
     */
    public boolean isSeatBooked(int seatNumber) {
        return bookedSeats.contains(seatNumber);
    }

    public int getTotalSeatsCapacity() {
        return TOTAL_SEATS;
    }

    /**
     * Resets the booking system by clearing all booked seats
     */
    public void reset() {
        bookedSeats.clear();
    }

}