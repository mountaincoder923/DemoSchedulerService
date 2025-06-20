package com.demoscheduler.demoscheduler;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Calendar {
    private static final LocalTime START_TIME = LocalTime.of(9, 0);
    private static final LocalTime END_TIME = LocalTime.of(17, 0);
    private static final Duration SLOT_DURATION = Duration.ofMinutes(15);

    // Map of start time to Event
    private final Map<LocalTime, Event> events = new ConcurrentHashMap<>();

    /**
     * Initialize all available slots between START_TIME and END_TIME.
     */
    public void init() {
        LocalTime time = START_TIME;
        while (!time.isAfter(END_TIME.minus(SLOT_DURATION))) {
            LocalTime end = time.plus(SLOT_DURATION);
            Event slot = new Event(time, end, "", "", "");
            events.put(time, slot);
            time = time.plus(SLOT_DURATION);
        }
    }

    /**
     * Get the closest available slots to the desired time.
     *
     * @param desired the user's desired start time
     * @param count   number of slots to return
     * @return list of available Event objects
     */
    public List<Event> getClosestAvailable(LocalTime desired, int count) {
        return events.values().stream()
                .filter(e -> !e.isBooked())
                .sorted((a, b) -> Long.compare(
                        Math.abs(Duration.between(a.getStartTime(), desired).toMinutes()),
                        Math.abs(Duration.between(b.getStartTime(), desired).toMinutes())
                ))
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * Attempts to book a slot based on start time string.
     *
     * @param startTimeStr ISO 8601 time string (e.g., "10:15")
     * @param client       client's name
     * @param description  meeting description
     * @param advisor      advisor's name
     * @return true if booked, false otherwise
     */
    public boolean bookEvent(String startTimeStr, String client, String description, String advisor) {
        LocalTime startTime;
        try {
            startTime = LocalTime.parse(startTimeStr);
        } catch (Exception e) {
            return false;
        }

        Event slot = events.get(startTime);
        if (slot == null || slot.isBooked()) {
            return false;
        }

        slot.setClient(client);
        slot.setDescription(description);
        slot.setAdvisor(advisor);
        slot.book();
        return true;
    }

    /**
     * Prints the full calendar in ASCII table format.
     */
    public void showCalendar() {
        final String RESET = "\u001B[0m";
        final String RED = "\u001B[31m";
        final String GREEN = "\u001B[32m";
        final String BOLD = "\u001B[1m";

        String format = "| %-13s | %-8s | %-10s | %-8s | %-20s |%n";
        String line = "+-----------------+----------+------------+----------+----------------------+";

        System.out.println(line);
        System.out.printf(BOLD + "| Time Slot       | Booked   | Client     | Advisor  | Description          |%n" + RESET);
        System.out.println(line);

        events.values().stream()
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .forEach(event -> {
                    String timeSlot = event.getStartTime() + " - " + event.getEndTime();
                    boolean isBooked = event.isBooked();
                    String status = isBooked ? RED + "Yes" + RESET : GREEN + "No" + RESET;
                    String client = event.getClient().isEmpty() ? "" : event.getClient();
                    String advisor = event.getAdvisor().isEmpty() ? "" : event.getAdvisor();
                    String description = event.getDescription().isEmpty() ? "" : event.getDescription();

                    System.out.printf(format, timeSlot, status, client, advisor, description);
                });

        System.out.println(line);
    }
}