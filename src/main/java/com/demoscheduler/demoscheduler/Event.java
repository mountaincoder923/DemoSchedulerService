package com.demoscheduler.demoscheduler;

import java.time.LocalTime;

public class Event {
    private final LocalTime startTime;
    private final LocalTime endTime;
    private boolean booked;

    private String client;
    private String description;
    private String advisor;

    public Event(LocalTime startTime, LocalTime endTime, String client, String description, String advisor) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.client = client;
        this.description = description;
        this.advisor = advisor;
        this.booked = !client.isEmpty(); // auto-mark booked if client provided
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