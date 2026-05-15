package androidev.thegreenroom;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;

import com.bumptech.glide.Glide;

public class EventDetailsFragment extends Fragment {

    private static final String ARG_EVENT = "event_data";
    private TicketmasterEvent event;

    private ImageView eventImage;
    private TextView eventName;
    private TextView venueName;
    private TextView dateTime;
    private Button btnTickets;
    private Button btnDirections;
    private ImageView btnBack;

    /**
     * Event Details Fragment
     * Creates an Event Details Fragment with its necessary data
     * @return created fragment
     */
    public static EventDetailsFragment newInstance(TicketmasterEvent event) {
        EventDetailsFragment fragment = new EventDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, event);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * On Create
     * Uses the data for the fragment
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            event = (TicketmasterEvent) getArguments().getSerializable(ARG_EVENT);
        }
    }

    /**
     * On Create View
     * Converts the fragment event details XML file to View objects
     * Calls displayEventData
     * Initialises click listeners
     * @return view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_details, container, false);

        eventImage = view.findViewById(R.id.event_image);
        eventName = view.findViewById(R.id.event_name);
        venueName = view.findViewById(R.id.venue);
        dateTime = view.findViewById(R.id.date_time);
        btnTickets = view.findViewById(R.id.btn_tickets);
        btnDirections = view.findViewById(R.id.btn_directions);
        btnBack = view.findViewById(R.id.btn_back);

        // display event data
        displayEventData();


        // click listeners for going back, navigating to venue, and going to tickets site
        // redirects users to internet to get tickets for event
        btnTickets.setOnClickListener(v -> {
            if (event.getUrl() != null && !event.getUrl().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(event.getUrl()));
                startActivity(intent);
            }
        });

        // redirects users to internet to get directions to event venue
        btnDirections.setOnClickListener(v -> {
            String venue = event.getVenue();
            if (venue != null && !venue.isEmpty() && !venue.equals("Venue TBD")) {
                // create uri using venue
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(venue));

                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
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
     * Loads event text values and image into view
     */
    private void displayEventData() {
        if (event == null) return;

        // set details
        eventName.setText(event.getName());
        venueName.setText(event.getVenue());
        dateTime.setText(event.getDateTime());

        // load event image
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(event.getImageUrl())
                    .placeholder(R.drawable.profile_placeholder)
                    .into(eventImage);
        }
    }
}