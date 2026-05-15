package androidev.thegreenroom;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import java.util.List;

/**
 * Ticketmaster Event
 * A model class for a single Ticketmaster Event
 */
public class TicketmasterEvent implements java.io.Serializable {

    @SerializedName("name")
    private String name;

    @SerializedName("url")
    private String url;

    @SerializedName("images")
    private List<Image> images;

    @SerializedName("dates")
    private Dates dates;

    @SerializedName("_embedded")
    private Embedded embedded;


    // getter methods
    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getImageUrl() {
        if (images != null && !images.isEmpty()) {
            return images.get(0).getUrl();
        }
        return null;
    }

    // gets time with date
    public String getDateTime() {
        String dateString = dates.getStart().getLocalDate();
        String timeString = dates.getStart().getLocalTime();

        if (dateString != null && !dateString.isEmpty()) {
            LocalDate date = LocalDate.parse(dateString);
            String formattedDate = date.format(DateTimeFormatter.ofPattern("dd MMM, yyyy"));

            if (timeString != null && !timeString.isEmpty()) {
                LocalTime time = LocalTime.parse(timeString);
                String formattedTime = time.format(DateTimeFormatter.ofPattern("h:mm a"));
                return formattedDate + " @ " + formattedTime;
            }
            return formattedDate;
        }
        return "Date & time TBC";
    }

    public String getVenue() {
        if (embedded != null && embedded.getVenues() != null && !embedded.getVenues().isEmpty()) {
            return embedded.getVenues().get(0).getName();
        }
        return "Venue TBC";
    }


    // inner classes in nested object json to be parsed by gson
    public static class Image {
        @SerializedName("url")
        private String url;
        public String getUrl() {
            return url;
        }
    }

    public static class Dates {
        @SerializedName("start")
        private Start start;
        public Start getStart() {
            return start;
        }
    }

    public static class Start {
        @SerializedName("localDate")
        private String localDate;

        @SerializedName("localTime")
        private String localTime;

        public String getLocalDate() {
            return localDate;
        }

        public String getLocalTime() {
            return localTime;
        }
    }

    public static class Embedded {
        @SerializedName("venues")
        private List<Venue> venues;
        public List<Venue> getVenues() {
            return venues;
        }
    }

    public static class Venue {
        @SerializedName("name")
        private String name;
        public String getName() {
            return name;
        }
    }
}