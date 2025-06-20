package com.demoscheduler.demoscheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalendarTest {

    private Calendar calendar;

    @BeforeEach
    void setUp() {
        calendar = new Calendar();
        calendar.init();
    }

    @Test
    void testInitSlotsCount() {
        // There are 32 slots from 9:00 to 17:00 in 15-minute increments
        List<Event> all = calendar.getClosestAvailable(LocalTime.of(9, 0), 100);
        assertEquals(32, all.size(), "There should be 32 slots from 9 AM to 5 PM in 15-min increments");
    }

    @Test
    void testGetClosestAvailableOrdering() {
        LocalTime desired = LocalTime.of(9, 7);
        List<Event> two = calendar.getClosestAvailable(desired, 2);

        assertEquals(2, two.size(), "Should return exactly 2 slots");
        assertEquals(LocalTime.of(9, 0), two.get(0).getStartTime(), "First slot should be 9:00");
        assertEquals(LocalTime.of(9, 15), two.get(1).getStartTime(), "Second slot should be 9:15");
    }

    @Test
    void testDesiredBeforeStart() {
        Event first = calendar.getClosestAvailable(LocalTime.of(8, 0), 1).get(0);
        assertEquals(LocalTime.of(9, 0), first.getStartTime());
    }

    @Test
    void testDesiredAfterEnd() {
        Event last = calendar.getClosestAvailable(LocalTime.of(18, 0), 1).get(0);
        assertEquals(LocalTime.of(16, 45), last.getStartTime());
    }

    @Test
    void testBookEventSuccessAndIdempotence() {
        // Find a slot at 10:00
        List<Event> slotList = calendar.getClosestAvailable(LocalTime.of(10, 0), 1);
        assertFalse(slotList.isEmpty(), "There should be at least one slot at 10:00");
        String startTimeStr = slotList.get(0).getStartTime().toString();

        // First booking should succeed
        boolean first = calendar.bookEvent(startTimeStr, "Alice", "Consultation", "Bob");
        assertTrue(first, "Booking an unbooked slot must return true");

        // Slot should no longer appear in available slots
        List<Event> remaining = calendar.getClosestAvailable(LocalTime.of(10, 0), 100);
        assertFalse(
                remaining.stream().anyMatch(e -> e.getStartTime().toString().equals(startTimeStr)),
                "Booked slot must be removed from available slots"
        );

        // Second booking attempt should fail
        boolean second = calendar.bookEvent(startTimeStr, "Alice", "Consultation", "Bob");
        assertFalse(second, "Re-booking the same slot must return false");
    }

    @Test
    void testBookInvalidStartTime() {
        assertFalse(calendar.bookEvent("22:00", "X", "Y", "Z"),
                "Booking a non-existent time must return false");
        assertFalse(calendar.bookEvent("invalid-time", "X", "Y", "Z"),
                "Booking with an invalid time format must return false");
    }
}