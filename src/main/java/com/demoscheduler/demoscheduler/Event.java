package com.demoscheduler.demoscheduler;

import java.time.LocalDate;
import java.time.LocalTime;

public class Event {
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final LocalDate date;
    private boolean booked;
    private String client;
    private String description;
    private String advisor;

    public Event(LocalDate date, LocalTime startTime, LocalTime endTime,
                 String client, String description, String advisor) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.client = client == null ? "" : client;
        this.description = description == null ? "" : description;
        this.advisor = advisor == null ? "" : advisor;
        this.booked = !this.client.isEmpty(); // safe
    }

    public LocalDate getDate() {         // NEW
        return date;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public boolean isBooked() {
        return booked;
    }

    public void book() {
        this.booked = true;
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