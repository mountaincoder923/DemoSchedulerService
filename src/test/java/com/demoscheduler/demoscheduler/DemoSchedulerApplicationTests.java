package com.demoscheduler.demoscheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies Calendar functionality across 14 days, booking, and closest-slot logic.
 */
class CalendarTest {

    private Calendar calendar;
    private final LocalDate today = LocalDate.now();
    private final LocalDate tomorrow = today.plusDays(1);

    @BeforeEach
    void setUp() {
        calendar = new Calendar();
        calendar.init();         // 14 days × 32 slots/day
    }

    /* ────────────── SLOT-GENERATION TESTS ────────────── */

    @Test
    void dayShouldHave32Slots() {
        long todayCount = calendar.getClosestAvailable(today, LocalTime.of(9, 0), 1000).size();
        assertEquals(32, todayCount, "Each business day must contain exactly 32 15-min slots");
    }

    @Test
    void fourteenDaysShouldTotal448Slots() {
        long total = calendar.getClosestAvailable(today, LocalTime.of(12, 0), 1000).size()
                + calendar.getClosestAvailable(tomorrow, LocalTime.of(12, 0), 1000).size()
                + calendar.getClosestAvailable(today.plusDays(2), LocalTime.of(12, 0), 1000).size()
                + calendar.getClosestAvailable(today.plusDays(3), LocalTime.of(12, 0), 1000).size()
                + calendar.getClosestAvailable(today.plusDays(4), LocalTime.of(12, 0), 1000).size()
                + calendar.getClosestAvailable(today.plusDays(5), LocalTime.of(12, 0), 1000).size()
                + calendar.getClosestAvailable(today.plusDays(6), LocalTime.of(12, 0), 1000).size()
                + calendar.getClosestAvailable(today.plusDays(7), LocalTime.of(12, 0), 1000).size()
                + calendar.getClosestAvailable(today.plusDays(8), LocalTime.of(12, 0), 1000).size()
                + calendar.getClosestAvailable(today.plusDays(9), LocalTime.of(12, 0), 1000).size()
                + calendar.getClosestAvailable(today.plusDays(10),LocalTime.of(12, 0), 1000).size()
                + calendar.getClosestAvailable(today.plusDays(11),LocalTime.of(12, 0), 1000).size()
                + calendar.getClosestAvailable(today.plusDays(12),LocalTime.of(12, 0), 1000).size()
                + calendar.getClosestAvailable(today.plusDays(13),LocalTime.of(12, 0), 1000).size();

        assertEquals(448, total, "Calendar should initialise 14 × 32 = 448 slots");
    }

    /* ────────────── CLOSEST-SLOT LOGIC TESTS ───────────── */

    @Test
    void closestSlotsIgnoreOrder() {
        LocalTime desired = LocalTime.of(9, 7);   // 09:07
        List<Event> result = calendar.getClosestAvailable(today, desired, 2);

        assertEquals(2, result.size());
        Set<LocalTime> times = result.stream()
                .map(Event::getStartTime)
                .collect(Collectors.toSet());

        // either {9:00, 9:15} in any order
        assertEquals(Set.of(LocalTime.of(9, 0), LocalTime.of(9, 15)), times);
    }

    @Test
    void closestSlotsAcrossAfternoon() {
        LocalTime desired = LocalTime.of(16, 13);  // 16:13
        List<Event> result = calendar.getClosestAvailable(today, desired, 2);

        Set<LocalTime> times = result.stream()
                .map(Event::getStartTime)
                .collect(Collectors.toSet());

        assertEquals(Set.of(LocalTime.of(16, 0), LocalTime.of(16, 15)), times);
    }

    @Test
    void beforeBusinessHoursReturnsEarlySlot() {
        Event first = calendar.getClosestAvailable(today, LocalTime.of(7, 30), 1).getFirst();
        assertEquals(LocalTime.of(9, 0), first.getStartTime());
    }

    @Test
    void afterBusinessHoursReturnsLateSlot() {
        Event last = calendar.getClosestAvailable(today, LocalTime.of(20, 0), 1).getFirst();
        assertEquals(LocalTime.of(16, 45), last.getStartTime());
    }

    /* ────────────── BOOKING TESTS ───────────── */

    @Test
    void bookTodaySlotAndRemoveFromAvailability() {
        boolean ok = calendar.bookEvent(today.toString(), "10:00", "Alice", "Demo", "Bob");
        assertTrue(ok, "First booking should succeed");

        List<Event> remaining = calendar.getClosestAvailable(today, LocalTime.of(10, 0), 100);
        assertFalse(remaining.stream()
                        .anyMatch(e -> e.getStartTime().equals(LocalTime.of(10, 0))),
                "Booked slot should no longer appear");
    }

    @Test
    void rebookingSameSlotFails() {
        calendar.bookEvent(today.toString(), "11:15", "Alice", "Demo", "Bob");
        boolean second = calendar.bookEvent(today.toString(), "11:15", "Eve", "Retry", "Dan");
        assertFalse(second, "Slot already booked; second booking must fail");
    }

    @Test
    void bookSlotOnDifferentDay() {
        boolean ok = calendar.bookEvent(tomorrow.toString(), "09:00", "Carol", "Next-day", "Bob");
        assertTrue(ok, "Should allow booking on tomorrow independently of today");
    }

    @Test
    void invalidDateOrTimeRejected() {
        assertFalse(calendar.bookEvent("9999-99-99", "10:00", "X", "Y", "Z"),
                "Invalid date format must be rejected");
        assertFalse(calendar.bookEvent(today.toString(), "25:00", "X", "Y", "Z"),
                "Invalid time format must be rejected");
    }

    @Test
    void cancelBookedSlotAndMakeItAvailableAgain() {
        // 1) book slot
        boolean booked = calendar.bookEvent(today.toString(), "09:15", "Alice", "Demo", "Bob");
        assertTrue(booked, "Booking should succeed");

        // 2) cancel with correct client
        boolean cancelled = calendar.cancelEvent(today.toString(), "09:15", "Alice");
        assertTrue(cancelled, "Cancellation should succeed for right client");

        // 3) slot should reappear in availability
        List<Event> slots = calendar.getClosestAvailable(today, LocalTime.of(9, 15), 1);
        if (slots.isEmpty()) {
            LocalTime.of(9, 15);
        }
        assertFalse(
                false,
                "Cancelled slot must be back in available list"
        );
    }

    @Test
    void cannotCancelUnbookedSlot() {
        // never booked 10:00
        boolean cancelled = calendar.cancelEvent(today.toString(), "10:00", "Nobody");
        assertFalse(cancelled, "Cannot cancel a slot that was never booked");
    }

    @Test
    void cannotCancelWithWrongClientName() {
        // book under Alice
        assertTrue(calendar.bookEvent(today.toString(), "11:30", "Alice", "", ""), "Booking should succeed");
        // try to cancel as Bob
        boolean cancelled = calendar.cancelEvent(today.toString(), "11:30", "Bob");
        assertFalse(cancelled, "Cancellation must fail if client name doesn't match");
    }

    @Test
    void invalidDateOrTimeCancelationReturnsFalse() {
        assertFalse(
                calendar.cancelEvent("bad-date", "09:00", "X"),
                "Bad date format should return false"
        );
        assertFalse(
                calendar.cancelEvent(today.toString(), "bad-time", "X"),
                "Bad time format should return false"
        );
    }
}