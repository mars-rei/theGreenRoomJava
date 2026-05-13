package androidev.thegreenroom;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// model class of a single ticket master event
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
        if (dates != null && dates.getStart() != null) {
            String date = dates.getStart().getLocalDate();
            String time = dates.getStart().getLocalTime();

            if (date != null && !date.isEmpty()) {
                if (time != null && !time.isEmpty()) {
                    return date + " at " + time;
                }
                return date;
            }
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