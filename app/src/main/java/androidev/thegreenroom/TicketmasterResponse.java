package androidev.thegreenroom;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TicketmasterResponse {
    // contains the events and data from ticketmaster api
    @SerializedName("_embedded")
    private Embedded embedded;

    // for pagination - will tell if there are items or not
    @SerializedName("page")
    private Page page;


    public List<TicketmasterEvent> getEvents() {
        if (embedded != null && embedded.events != null) {
            return embedded.events;
        }
        return null;
    }

    public int getTotalCount() {
        if (page != null) {
            return page.totalElements;
        }
        return 0;
    }


    public static class Embedded {
        @SerializedName("events")
        private List<TicketmasterEvent> events;
    }

    public static class Page {
        @SerializedName("totalElements")
        private int totalElements;
    }
}