package androidev.thegreenroom;

import java.time.LocalDate;
import java.time.LocalTime;

public class ScheduleEvent {
    private String id;
    private String userId;
    private String title;
    private String date;
    private String time;
    private String venue;

    public ScheduleEvent() {}
    public ScheduleEvent(String id, String userId, String title,
                         String date, String time, String venue) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.date = date;
        this.time = time;
        this.venue = venue;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getVenue() { return venue; }

    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setTitle(String title) { this.title = title; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setVenue(String venue) { this.venue = venue; }
}