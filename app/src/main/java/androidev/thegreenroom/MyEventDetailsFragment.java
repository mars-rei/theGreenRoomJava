package androidev.thegreenroom;

import android.content.Intent;
import android.net.Uri;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import androidx.fragment.app.Fragment;
import android.os.Bundle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MyEventDetailsFragment extends Fragment {

    private TextView eventNameText;
    private TextView venueText;
    private TextView dateTimeText;
    private Button btnDirections;
    private ImageView btnBack;

    private ScheduleEvent scheduleEvent;

    /**
     * My Event Details Fragment
     * Creates an My Event Details Fragment with its necessary data
     * @return created fragment
     */
    public static MyEventDetailsFragment newInstance(ScheduleEvent event) {
        MyEventDetailsFragment fragment = new MyEventDetailsFragment();
        Bundle args = new Bundle();
        args.putString("event_id", event.getId());
        args.putString("event_title", event.getTitle());
        args.putString("event_venue", event.getVenue());
        args.putString("event_date", event.getDate());
        args.putString("event_time", event.getTime());
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * On Create View
     * Converts the fragment my event details XML file to View objects
     * Calls displayEventData
     * Initialises click listeners
     * @return view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_event_details, container, false);

        eventNameText = view.findViewById(R.id.event_name);
        venueText = view.findViewById(R.id.venue);
        dateTimeText = view.findViewById(R.id.date_time);
        btnDirections = view.findViewById(R.id.btn_directions);
        btnBack = view.findViewById(R.id.btn_back);

        // get event data from fragment
        if (getArguments() != null) {
            String id = getArguments().getString("event_id");
            String title = getArguments().getString("event_title");
            String venue = getArguments().getString("event_venue");
            String date = getArguments().getString("event_date");
            String time = getArguments().getString("event_time");
            scheduleEvent = new ScheduleEvent(id, "", title, date, time, venue);
        }

        // display event data
        displayEventData();

        // get directions to event venue
        btnDirections.setOnClickListener(v -> {
            if (scheduleEvent != null) {
                String venueAddress = scheduleEvent.getVenue();
                if (venueAddress != null && !venueAddress.isEmpty() && !venueAddress.equals("Venue TBD")) {
                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(venueAddress));
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            }
        });

        // go back
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }

    /**
     * Display Event Data
     * Loads event text values into view
     */
    private void displayEventData() {
        if (scheduleEvent != null) {
            eventNameText.setText(scheduleEvent.getTitle());
            venueText.setText(scheduleEvent.getVenue());

            // format for display
            String formattedDateTime = formatDateTime(scheduleEvent.getDate(), scheduleEvent.getTime());
            dateTimeText.setText(formattedDateTime);
        }
    }

    /**
     * Format Date Time
     * Loads event text values and image into view
     * @param dateString (String)
     * @param timeString (String)
     */
    private String formatDateTime(String dateString, String timeString) {
        LocalDate date = LocalDate.parse(dateString);
        LocalTime time = LocalTime.parse(timeString);
        LocalDateTime dateTime = LocalDateTime.of(date, time);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM, yyyy @ h:mm a");
        return dateTime.format(formatter);
    }
}