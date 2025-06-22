package com.demoscheduler.demoscheduler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.PostConstruct;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * REST controller exposing the Calendar service for scheduling (now date-aware).
 */
@RestController
@RequestMapping("/api/scheduler")
public class SchedulerController {

    private final Calendar salesCalendar = new Calendar();

    /** Initialise 14-day calendar at startup. */
    @PostConstruct
    public void init() {
        salesCalendar.init();
        salesCalendar.showCalendar();   // optional console dump
    }

    /* ─────────────────────────────  /slots  ───────────────────────────── */

    /** Find the N closest available slots for a given date + time. */
    @PostMapping("/slots")
    public List<Event> getClosestSlots(@RequestBody SlotSearchRequest req) {

        // default to today if the client omits "date"
        LocalDate desiredDate = (req.getDate() == null || req.getDate().isBlank())
                ? LocalDate.now()
                : LocalDate.parse(req.getDate());

        LocalTime desiredTime = LocalTime.parse(req.getDesired());
        int count = req.getCount() > 0 ? req.getCount() : 5;

        return salesCalendar.getClosestAvailable(desiredDate, desiredTime, count);
    }

    /* ─────────────────────────────  /book  ────────────────────────────── */

    /** Book a specific slot identified by date + startTime. */
    @PostMapping("/book")
    public ResponseEntity<String> bookSlot(@RequestBody BookingRequest req) {

        // default to today if "date" omitted
        String dateStr = (req.getDate() == null || req.getDate().isBlank())
                ? LocalDate.now().toString()
                : req.getDate();

        boolean ok = salesCalendar.bookEvent(
                dateStr,
                req.getStartTime(),
                req.getClient(),
                req.getDescription(),
                req.getAdvisor()
        );

        if (ok) {
            salesCalendar.showCalendar();   // visual feedback
            return ResponseEntity.ok("Booked successfully");
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Failed to book: slot not found or already booked");
    }

    @PostMapping("/cancel")
    public ResponseEntity<String> cancelSlot(@RequestBody CancelRequest req) {
        // default to today if omitted
        String dateStr = (req.getDate() == null || req.getDate().isBlank())
                ? LocalDate.now().toString()
                : req.getDate();

        String timeStr = req.getStartTime();
        String client  = req.getClient();

        if (timeStr == null || timeStr.isBlank() || client == null || client.isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Both startTime and client are required");
        }

        boolean cancelled = salesCalendar.cancelEvent(dateStr, timeStr, client);
        if (cancelled) {
            salesCalendar.showCalendar();
            return ResponseEntity.ok("Cancellation successful");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Cancellation failed: slot not found, not booked, or client mismatch");
        }
    }

    /* ───────────────────────────── DTOs ──────────────────────────────── */

    /** JSON body for POST /slots */
    public static class SlotSearchRequest {
        private String date;     // yyyy-MM-dd, optional (defaults to today)
        private String desired;  // HH:mm   (required)
        private int    count = 5;

        public String getDate()        { return date; }
        public void   setDate(String d){ this.date = d; }

        public String getDesired()     { return desired; }
        public void   setDesired(String t){ this.desired = t; }

        public int    getCount()       { return count; }
        public void   setCount(int c)  { this.count = c; }
    }


    /** JSON body for POST /book */
    public static class BookingRequest {
        private String date;      // yyyy-MM-dd, optional
        private String startTime; // HH:mm, required
        private String client;
        private String description;
        private String advisor;

        public String getDate() { return date; }
        public void   setDate(String d) { this.date = d; }

        public String getStartTime() { return startTime; }
        public void   setStartTime(String t) { this.startTime = t; }

        public String getClient() { return client; }
        public void   setClient(String c) { this.client = c; }

        public String getDescription() { return description; }
        public void   setDescription(String d) { this.description = d; }

        public String getAdvisor() { return advisor; }
        public void   setAdvisor(String a) { this.advisor = a; }
    }

    public static class CancelRequest {
        private String date;       // yyyy-MM-dd, optional
        private String startTime;  // HH:mm, required
        private String client;     // required

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String t) { this.startTime = t; }
        public String getClient() { return client; }
        public void setClient(String c) { this.client = c; }
    }
}