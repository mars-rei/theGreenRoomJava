package androidev.thegreenroom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// to help with displaying data from api
import com.bumptech.glide.Glide;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventsFragment extends Fragment {
    // api key for ticketmaster
    private static final String API_KEY = "1JKSNmElJAGHQbSQj7sAVdzraSVzfnN6";

    private LinearLayout eventsContainer;
    private TextView myEventsTab, eventsForYouTab;
    private ScrollView scrollView;

    private FirebaseFirestore firestore;
    private DataStoreManager dataStoreManager;
    private String currentUserId;

    private List<ScheduleEvent> myScheduledEvents = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        // initialise firestore
        firestore = FirebaseFirestore.getInstance();
        dataStoreManager = new DataStoreManager(requireContext());

        // tabs on this fragment
        myEventsTab = view.findViewById(R.id.my_events);
        eventsForYouTab = view.findViewById(R.id.events_for_you);

        eventsContainer = view.findViewById(R.id.events_container);
        scrollView = view.findViewById(R.id.scrollView);

        myEventsTab.setOnClickListener(v -> showMyEvents());
        eventsForYouTab.setOnClickListener(v -> showEventsForYou());

        // load user id from datastore
        loadUserId();

        return view;
    }

    private void loadUserId() {
        new Thread(() -> {
            currentUserId = dataStoreManager.getUserIdBlocking();
            requireActivity().runOnUiThread(() -> {
                // default is events created by the user - to improve, add events saved from events for you section
                showMyEvents();
            });
        }).start();
    }

    private void loadWestMidlandsEvents() {
        // show events for you tab is active
        myEventsTab.setTypeface(null, android.graphics.Typeface.NORMAL);
        eventsForYouTab.setTypeface(null, android.graphics.Typeface.BOLD);

        // show buffer text when loading
        eventsContainer.removeAllViews();
        displayMessage("Loading events in West Midlands...");

        // call api
        TicketmasterApi api = RetrofitInstance.getApiInterface();
        Call<TicketmasterResponse> call = api.searchEvents(
                API_KEY, // use api key mentioned above
                "Birmingham", // for a location in west midlands
                "GB", // country code for uk
                "music", // get music events
                20 // get max of 20 results
        );

        call.enqueue(new Callback<TicketmasterResponse>() {
            @Override
            public void onResponse(Call<TicketmasterResponse> call, Response<TicketmasterResponse> response) {
                eventsContainer.removeAllViews();

                if (response.isSuccessful() && response.body() != null) {
                    List<TicketmasterEvent> events = response.body().getEvents();

                    if (events != null && !events.isEmpty()) {
                        // if events are found, display events
                        displayEvents(events);
                    } else {
                        // if no events are found, show message
                        displayMessage("No events found in West Midlands");
                    }
                } else {
                    // in case api fails
                    displayMessage("API Error: " + response.code());
                }
            }

            // if api fails
            @Override
            public void onFailure(Call<TicketmasterResponse> call, Throwable t) {
                eventsContainer.removeAllViews();
                displayMessage("Error: " + t.getMessage());
            }
        });
    }

    private void displayEvents(List<TicketmasterEvent> events) {
        // for every event found from api pull, add an event to the events for you display
        for (TicketmasterEvent event : events) {
            addEventCard(event);
        }

        // start at top of page
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_UP));
    }

    // add event cards to scroll view for events for you tab
    private void addEventCard(TicketmasterEvent event) {
        View card = LayoutInflater.from(getContext())
                .inflate(R.layout.event_card_template, eventsContainer, false);

        TextView eventName = card.findViewById(R.id.event_name);
        TextView venueName = card.findViewById(R.id.venue_name);
        TextView eventDateTime = card.findViewById(R.id.event_date_time);
        ImageView eventPhoto = card.findViewById(R.id.eventPhoto);

        // set event name, venue and date and time
        eventName.setText(event.getName());
        venueName.setText(event.getVenue());
        eventDateTime.setText(event.getDateTime());

        // load image for event
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(event.getImageUrl())
                    .placeholder(R.drawable.profile_placeholder)
                    .into(eventPhoto);
        }

        // listener for event card to get and display more details
        card.setOnClickListener(v -> {
            EventDetailsFragment detailFragment = EventDetailsFragment.newInstance(event);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, detailFragment)
                    .addToBackStack("event_detail")  // go back to events for you tab in this fragment
                    .commit();
        });

        eventsContainer.addView(card);
    }


    private void loadMyEvents() {
        // show my events tab is active
        myEventsTab.setTypeface(null, android.graphics.Typeface.BOLD);
        eventsForYouTab.setTypeface(null, android.graphics.Typeface.NORMAL);

        // show buffer text when loading
        eventsContainer.removeAllViews();

        displayMessage("Loading your events...");

        // get events from firestore
        firestore.collection("users")
                .document(currentUserId)
                .collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventsContainer.removeAllViews();

                    List<DocumentSnapshot> events = queryDocumentSnapshots.getDocuments();

                    if (events.isEmpty()) {
                        displayMessage("You have no events yet");
                    } else {
                        myScheduledEvents.clear();
                        for (DocumentSnapshot document : events) {
                            ScheduleEvent scheduleEvent = document.toObject(ScheduleEvent.class);
                            if (scheduleEvent != null) {
                                myScheduledEvents.add(scheduleEvent);
                            }
                        }
                        displayMyEvents(myScheduledEvents);
                    }

                    scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_UP));
                });
    }

    private void displayMyEvents(List<ScheduleEvent> events) {
        eventsContainer.removeAllViews();

        if (events.isEmpty()) {
            displayMessage("You have no events yet");
            return;
        }

        for (ScheduleEvent event : events) {
            addScheduleCard(event);
        }

        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_UP));
    }

    // add schedule cards to my events section
    private void addScheduleCard(ScheduleEvent event) {
        View card = LayoutInflater.from(getContext())
                .inflate(R.layout.schedule_card_template, eventsContainer, false);

        TextView title = card.findViewById(R.id.event_name);
        TextView venue = card.findViewById(R.id.venue_name);
        TextView dateTime = card.findViewById(R.id.event_date_time);

        title.setText(event.getTitle());
        venue.setText(event.getVenue());

        String formattedDateTime = formatDateTime(event.getDate(), event.getTime());
        dateTime.setText(formattedDateTime);

        // listener for event card to get and display more details
        card.setOnClickListener(v -> {
            MyEventDetailsFragment detailFragment = MyEventDetailsFragment.newInstance(event);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.flFragment, detailFragment)
                    .addToBackStack("event_detail")
                    .commit();
        });

        eventsContainer.addView(card);
    }

    // format for card
    private String formatDateTime(String dateStr, String timeStr) {
        String[] dateParts = dateStr.split("-");
        if (dateParts.length == 3) {
            int year = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]);
            int day = Integer.parseInt(dateParts[2]);
            LocalDate date = LocalDate.of(year, month, day);

            // hh:mm format
            String[] timeParts = timeStr.split(":");
            if (timeParts.length == 2) {
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);
                LocalTime time = LocalTime.of(hour, minute);

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM, yyyy");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

                return date.format(dateFormatter) + " @ " + time.format(timeFormatter);
            }
        }
        return dateStr + " @ " + timeStr;
    }

    // show text if any errors occur / loading ux
    private void displayMessage(String message) {
        TextView textView = new TextView(getContext());
        textView.setText(message);
        textView.setTextColor(0xFFEDEDE9);
        textView.setTextSize(16);
        textView.setPadding(20, 20, 0, 0);
        eventsContainer.addView(textView);
    }

    private void showMyEvents() {
        myEventsTab.setTypeface(null, android.graphics.Typeface.BOLD);
        eventsForYouTab.setTypeface(null, android.graphics.Typeface.NORMAL);
        loadMyEvents();
    }

    private void showEventsForYou() {
        myEventsTab.setTypeface(null, android.graphics.Typeface.NORMAL);
        eventsForYouTab.setTypeface(null, android.graphics.Typeface.BOLD);
        loadWestMidlandsEvents();
    }
}