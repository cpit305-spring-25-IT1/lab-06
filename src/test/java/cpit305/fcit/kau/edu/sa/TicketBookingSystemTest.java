package cpit305.fcit.kau.edu.sa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;


import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class TicketBookingSystemTest {

    private TicketBookingSystem bookingSystem;

    @BeforeEach
    void setUp() {
        bookingSystem = new TicketBookingSystem();
    }

    @Test
    void testBookSingleSeat() {
        int seatNumber = bookingSystem.bookSeat("Khalid Ahmed");
        assertTrue(seatNumber > 0, "Should book a valid seat");
        assertTrue(bookingSystem.isSeatBooked(seatNumber), "Seat should be marked as booked");
        assertEquals(1, bookingSystem.getBookedSeatsCount(), "Should have 1 booked seat");
    }

    @Test
    void testBookAllSeats() {
        // Book all seats
        for (int i = 1; i <= bookingSystem.getTotalSeatsCapacity(); i++) {
            int seatNumber = bookingSystem.bookSeat("Passenger-" + i);
            assertEquals(i, seatNumber, "Should book seat " + i);
        }

        // Try to book one more seat
        int seatNumber = bookingSystem.bookSeat("Extra Passenger");
        assertEquals(-1, seatNumber, "Should return -1 when all seats are booked");
        assertEquals(bookingSystem.getTotalSeatsCapacity(), bookingSystem.getBookedSeatsCount(),
                "Should have the number of booked seats equal to the total number of seats");
    }

    @Test
    void testResetBookingSystem() {
        // Book some seats
        bookingSystem.bookSeat("Ali Hamed");
        bookingSystem.bookSeat("Fatima Abdullah");

        // clear all booked seats
        bookingSystem.reset();

        // Check if all seats are available
        assertEquals(0, bookingSystem.getBookedSeatsCount(), "Should have 0 booked seats after reset");

        // Book a seat again
        int seatNumber = bookingSystem.bookSeat("Ahmed Salman");
        assertEquals(1, seatNumber, "Should book the first seat after reset");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testConcurrentBookings() throws InterruptedException {
        int numThreads = 50;
        Thread[] threads = new Thread[numThreads];

        // Track seat numbers to check for duplicates
        ConcurrentHashMap<Integer, AtomicInteger> seatBookingCount = new ConcurrentHashMap<>();

        AtomicBoolean startSignal = new AtomicBoolean(false);

        // Create threads
        for (int i = 0; i < numThreads; i++) {
            final String passengerName = "Passenger-" + i;
            threads[i] = new Thread(() -> {
                // Wait until start signal
                while (!startSignal.get()) {
                    Thread.yield();
                }

                int seatNumber = bookingSystem.bookSeat(passengerName);
                if (seatNumber > 0) {
                    seatBookingCount.computeIfAbsent(seatNumber, k -> new AtomicInteger(0))
                            .incrementAndGet();
                }
            });
            threads[i].start();
        }

        // Give threads time to initialize
        Thread.sleep(100);

        // Signal threads to start booking
        startSignal.set(true);

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Verify results
        // Check if any seat was booked more than once (a race condition)
        boolean duplicateBookingsFound = false;
        for (Integer seat : seatBookingCount.keySet()) {
            int count = seatBookingCount.get(seat).get();
            if (count > 1) {
                duplicateBookingsFound = true;
                System.out.println("Race condition detected: Seat " + seat + " was booked " + count + " times!");
            }
        }

        assertFalse(duplicateBookingsFound, "Race condition detected. No seat should be booked more than once");

        // Verify the number of successfully booked seats
        assertEquals(Math.min(numThreads, bookingSystem.getTotalSeatsCapacity()),
                bookingSystem.getBookedSeatsCount(),
                "Number of booked seats should match the minimum of threads and capacity");

        // Verify all booked seats are actually marked as booked
        for (Integer seat : seatBookingCount.keySet()) {
            assertTrue(bookingSystem.isSeatBooked(seat),
                    "Seat " + seat + " should be marked as booked");
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testConcurrentBookingsWithHighContention() throws InterruptedException {
        // Create more threads than available seats to increase contention
        int numThreads = 200;
        Thread[] threads = new Thread[numThreads];

        // Reset system to ensure only 10 seats are available
        bookingSystem.reset();

        // Track successful bookings
        ConcurrentHashMap<Integer, AtomicInteger> seatBookingCount = new ConcurrentHashMap<>();
        AtomicInteger successfulBookings = new AtomicInteger(0);
        AtomicBoolean startSignal = new AtomicBoolean(false);

        // Create and start threads
        for (int i = 0; i < numThreads; i++) {
            final String passengerName = "HighContention-Passenger-" + i;
            threads[i] = new Thread(() -> {
                while (!startSignal.get()) {
                    Thread.yield();
                }

                int seatNumber = bookingSystem.bookSeat(passengerName);
                if (seatNumber > 0) {
                    successfulBookings.incrementAndGet();
                    seatBookingCount.computeIfAbsent(seatNumber, k -> new AtomicInteger(0))
                            .incrementAndGet();
                }
            });
            threads[i].start();
        }

        // Give threads time to initialize
        Thread.sleep(100);

        // Start all threads simultaneously
        startSignal.set(true);

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Verify results
        // Check for race conditions (duplicate bookings)
        boolean duplicateBookingsFound = false;
        for (Integer seat : seatBookingCount.keySet()) {
            int count = seatBookingCount.get(seat).get();
            if (count > 1) {
                duplicateBookingsFound = true;
                System.out.println("High contention race condition: Seat " + seat +
                        " was booked " + count + " times!");
            }
        }

        assertFalse(duplicateBookingsFound, "No seat should be booked more than once under high contention");

        // Verify we didn't book more seats than available
        assertTrue(successfulBookings.get() <= bookingSystem.getTotalSeatsCapacity(),
                "Should not book more seats than available");

        // Verify all booked seats are actually marked as booked
        for (Integer seat : seatBookingCount.keySet()) {
            assertTrue(bookingSystem.isSeatBooked(seat),
                    "Seat " + seat + " should be marked as booked under high contention");
        }

        // Verify the exact number of booked seats matches the system's count
        assertEquals(successfulBookings.get(), bookingSystem.getBookedSeatsCount(),
                "Number of successful bookings should match system's count");
    }

}