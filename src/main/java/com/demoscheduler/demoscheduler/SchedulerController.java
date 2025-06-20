package com.demoscheduler.demoscheduler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.PostConstruct;

import java.time.LocalTime;
import java.util.List;

/**
 * REST controller exposing the Calendar service for scheduling.
 */
@RestController
@RequestMapping("/api/scheduler")
public class SchedulerController {

    private final Calendar salesCalendar = new Calendar();

    /**
     * Initialize calendar slots at startup.
     */
    @PostConstruct
    public void init() {
        salesCalendar.init();
        salesCalendar.showCalendar(); // Optional: for visual verification
    }

    /**
     * Get the closest available slots to a desired time.
     * @param desired ISO-8601 time string (e.g. "09:30")
     * @param count number of slots to return (default 5)
     * @return list of available Event slots
     */
    @GetMapping("/slots")
    public List<Event> getClosestSlots(
            @RequestParam("desired") String desired,
            @RequestParam(value = "count", defaultValue = "5") int count
    ) {
        LocalTime desiredTime = LocalTime.parse(desired);
        return salesCalendar.getClosestAvailable(desiredTime, count);
    }

    /**
     * Book a specific slot by start time (e.g., "10:15").
     * @param request contains startTime and booking details
     * @return 200 OK if booked, 409 Conflict if slot unavailable or invalid
     */
    @PostMapping("/book")
    public ResponseEntity<String> bookSlot(@RequestBody BookingRequest request) {
        boolean success = salesCalendar.bookEvent(
                request.getStartTime(),
                request.getClient(),
                request.getDescription(),
                request.getAdvisor()
        );
        if (success) {
            salesCalendar.showCalendar();
            return ResponseEntity.ok("Booked successfully");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Failed to book: slot not found or already booked");
        }
    }

    /**
     * DTO for booking requests.
     */
    public static class BookingRequest {
        private String startTime; // e.g., "10:15"
        private String client;
        private String description;
        private String advisor;

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getClient() {
            return client;
        }

        public void setClient(String client) {
            this.client = client;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getAdvisor() {
            return advisor;
        }

        public void setAdvisor(String advisor) {
            this.advisor = advisor;
        }
    }
}