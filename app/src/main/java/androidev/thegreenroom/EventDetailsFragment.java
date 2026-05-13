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

    // creates fragment with its necessary data (for event details of event that was clicked on)
    public static EventDetailsFragment newInstance(TicketmasterEvent event) {
        EventDetailsFragment fragment = new EventDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, event);
        fragment.setArguments(args);
        return fragment;
    }

    // uses data for fragment
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            event = (TicketmasterEvent) getArguments().getSerializable(ARG_EVENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_details, container, false);

        eventImage = view.findViewById(R.id.detailEventImage);
        eventName = view.findViewById(R.id.detailEventName);
        venueName = view.findViewById(R.id.detailVenueName);
        dateTime = view.findViewById(R.id.detailDateTime);
        btnTickets = view.findViewById(R.id.btnTickets);
        btnDirections = view.findViewById(R.id.btnDirections);
        btnBack = view.findViewById(R.id.btnBack);

        // display event data
        displayEventData();


        // click listeners for going back, navigating to venue, and going to tickets site
        // get tickets for event
        btnTickets.setOnClickListener(v -> {
            if (event.getUrl() != null && !event.getUrl().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(event.getUrl()));
                startActivity(intent);
            }
        });

        // get directions to event venue
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