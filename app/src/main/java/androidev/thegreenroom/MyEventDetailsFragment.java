package androidev.thegreenroom;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MyEventDetailsFragment extends Fragment {

    private TextView eventNameText;
    private TextView venueText;
    private TextView dateTimeText;
    private Button btnDirections;
    private ImageView btnBack;

    private ScheduleEvent scheduleEvent;

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

    private void displayEventData() {
        if (scheduleEvent != null) {
            eventNameText.setText(scheduleEvent.getTitle());
            venueText.setText(scheduleEvent.getVenue());

            // format for display
            String formattedDateTime = formatDateTime(scheduleEvent.getDate(), scheduleEvent.getTime());
            dateTimeText.setText(formattedDateTime);
        }
    }

    private String formatDateTime(String dateStr, String timeStr) {
        // YYYY-MM-DD
        String[] dateParts = dateStr.split("-");
        if (dateParts.length == 3) {
            int year = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]);
            int day = Integer.parseInt(dateParts[2]);
            LocalDate date = LocalDate.of(year, month, day);

            // HH:MM
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
}