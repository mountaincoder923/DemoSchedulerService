package com.demoscheduler.demoscheduler;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory calendar that holds 14 days of 15-minute appointment slots,
 * starting with “today”.  Slots are keyed by their exact LocalDateTime.
 */
public class Calendar {

    /* ────────────────  Constants  ──────────────── */

    private static final LocalTime  START_TIME    = LocalTime.of(9, 0);
    private static final LocalTime  END_TIME      = LocalTime.of(17, 0);
    private static final Duration   SLOT_DURATION = Duration.ofMinutes(15);
    private static final int        DAYS_FORWARD  = 14;        // today + 13

    /* ────────────────  Storage  ──────────────── */

    /** Map key = slot start (date + time) */
    private final Map<LocalDateTime, Event> events = new ConcurrentHashMap<>();

    /* ────────────────  Initialise  ──────────────── */

    /** Generates every 15-min slot from 09:00–17:00 for 14 days. */
    public void init() {
        events.clear();
        LocalDate today = LocalDate.now();

        for (int d = 0; d < DAYS_FORWARD; d++) {
            LocalDate date = today.plusDays(d);
            LocalTime t = START_TIME;
            while (!t.isAfter(END_TIME.minus(SLOT_DURATION))) {
                LocalDateTime start = LocalDateTime.of(date, t);
                Event slot = new Event(date, t, t.plus(SLOT_DURATION), "", "", "");
                events.put(start, slot);
                t = t.plus(SLOT_DURATION);
            }
        }
    }

    /* ────────────────  SEARCH  ──────────────── */

    /** Convenience wrapper: “today” search. */
    public List<Event> getClosestAvailable(LocalTime desired, int count) {
        return getClosestAvailable(LocalDate.now(), desired, count);
    }

    /**
     * Return the {@code count} closest *un-booked* slots on the given date,
     * no guarantee of ordering.
     */
    public List<Event> getClosestAvailable(LocalDate desiredDate,
                                           LocalTime desiredTime,
                                           int count) {

        LocalDateTime desired = LocalDateTime.of(desiredDate, desiredTime);

        return events.values().stream()
                .filter(e -> !e.isBooked()
                        && e.getDate().equals(desiredDate))          // ← constrain by day
                .sorted(Comparator.comparingLong(ev ->
                        Math.abs(Duration.between(
                                LocalDateTime.of(ev.getDate(), ev.getStartTime()),
                                desired).toMinutes())))
                .limit(Math.max(count, 1))
                .collect(Collectors.toList());
    }

    /* ────────────────  BOOK  ──────────────── */

    /** Convenience wrapper: book a slot today. */
    public boolean bookEvent(String startTimeStr,
                             String client,
                             String description,
                             String advisor) {
        return bookEvent(LocalDate.now().toString(),
                startTimeStr, client, description, advisor);
    }

    /**
     * Book a specific slot identified by {@code dateStr} (yyyy-MM-dd)
     * and {@code startTimeStr} (HH:mm). Returns {@code true} on success.
     */
    public boolean bookEvent(String dateStr,
                             String startTimeStr,
                             String client,
                             String description,
                             String advisor) {

        if (startTimeStr == null || startTimeStr.isBlank()) return false;

        LocalDate date;
        LocalTime time;
        try {
            date = LocalDate.parse(dateStr);
            time = LocalTime.parse(startTimeStr);
        } catch (Exception e) {
            return false; // bad format
        }

        LocalDateTime key = LocalDateTime.of(date, time);
        Event slot = events.get(key);
        if (slot == null || slot.isBooked()) return false;

        slot.setClient(client == null ? "" : client);
        slot.setDescription(description == null ? "" : description);
        slot.setAdvisor(advisor == null ? "" : advisor);
        slot.book();
        return true;
    }

    public boolean cancelEvent(String dateStr,
                               String startTimeStr,
                               String clientName) {
        LocalDate date;
        LocalTime time;
        try {
            date = LocalDate.parse(dateStr);
            time = LocalTime.parse(startTimeStr);
        } catch (DateTimeParseException ex) {
            return false;      // invalid format
        }

        LocalDateTime key = LocalDateTime.of(date, time);
        Event slot = events.get(key);
        if (slot == null || !slot.isBooked()) {
            return false;      // no such slot or already free
        }
        if (!slot.getClient().equals(clientName)) {
            return false;      // booked under someone else
        }

        // reset the slot
        slot.setClient("");
        slot.setDescription("");
        slot.setAdvisor("");
        // un-book
        // (we don’t have an explicit unbook(), so toggle the flag via reflection or extend Event:)
        // Assuming Event had a setter:
        slot.setBooked(false);
        return true;
    }

    /* ────────────────  DISPLAY  (console helper) ──────────────── */

    public void showCalendar() {
        final String RESET = "\u001B[0m",
                RED   = "\u001B[31m",
                GREEN = "\u001B[32m",
                BOLD  = "\u001B[1m";

        String fmt  = "| %-10s | %-13s | %-8s | %-10s | %-8s | %-20s |%n";
        String line = "+------------+-----------------+----------+------------+----------+----------------------+";

        System.out.println(line);
        System.out.printf(BOLD +
                "| Date       | Time Slot       | Booked   | Client     | Advisor  | Description          |%n"
                + RESET);
        System.out.println(line);

        events.values().stream()
                .sorted(Comparator
                        .comparing(Event::getDate)
                        .thenComparing(Event::getStartTime))
                .forEach(ev -> {
                    String slot   = ev.getStartTime() + " - " + ev.getEndTime();
                    String status = ev.isBooked()
                            ? RED + "Yes" + RESET
                            : GREEN + "No"  + RESET;

                    // Null-safe extraction
                    String client      = ev.getClient()      == null || ev.getClient().isBlank()
                            ? "" : ev.getClient().strip();
                    String advisor     = ev.getAdvisor()     == null || ev.getAdvisor().isBlank()
                            ? "" : ev.getAdvisor().strip();
                    String description = ev.getDescription() == null || ev.getDescription().isBlank()
                            ? "" : ev.getDescription().strip();

                    System.out.printf(fmt,
                            ev.getDate(),
                            slot,
                            status,
                            client,
                            advisor,
                            description
                    );
                });
        System.out.println(line);
    }
}