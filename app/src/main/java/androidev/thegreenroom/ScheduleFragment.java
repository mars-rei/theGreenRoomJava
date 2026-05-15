package androidev.thegreenroom;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import androidx.fragment.app.Fragment;
import android.os.Bundle;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.UUID;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

public class ScheduleFragment extends Fragment {

    private ImageView btnBack;
    private EditText editTitle;
    private EditText editDate;
    private EditText editTime;
    private EditText editVenue;
    private Button btnSave;

    private LocalDate selectedDate;
    private LocalTime selectedTime;

    // firestore resources
    private FirebaseFirestore firestore;

    private DataStoreManager dataStoreManager;
    private String userId;

    /**
     * On Create
     * Initialises Firestore and DataStore
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialising
        firestore = FirebaseFirestore.getInstance();
        dataStoreManager = new DataStoreManager(requireContext());
    }

    /**
     * On Create View
     * Converts fragment add schedule XML file into View objects
     * Gets user id
     * calls setupClickListeners
     * @return add schedule view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_schedule, container, false);

        btnBack = view.findViewById(R.id.btn_back);
        editTitle = view.findViewById(R.id.edit_title);
        editDate = view.findViewById(R.id.edit_date);
        editTime = view.findViewById(R.id.edit_time);
        editVenue = view.findViewById(R.id.edit_venue);
        btnSave = view.findViewById(R.id.btn_save);

        // get userId
        new Thread(() -> {
            userId = dataStoreManager.getUserIdBlocking();
        }).start();

        setupClickListeners();

        return view;
    }

    /**
     * Setup Click Listeners
     * Sets up click listeners for back and save buttons
     * Sets up click listeners for selecting time and date
     * Upon save, saveToFirestore is called with title, selectedDate, selectedTime, and venue as parameters
     */
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // initialising time picker
        editTime.setOnClickListener(v -> {
            // set default to current time
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                    (timePicker, hourOfDay, minuteOfHour) -> {
                        String formattedTime = String.format("%02d:%02d", hourOfDay, minuteOfHour);
                        editTime.setText(formattedTime);

                        selectedTime = LocalTime.of(hourOfDay, minuteOfHour);
                    },
                    hour, minute, false);  // for AM and PM
            timePickerDialog.show();
        });

        // initialising date picker
        editDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (datePicker, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                        editDate.setText(formattedDate);

                        selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay);
                    },
                    year, month, day);
            datePickerDialog.show();
        });

        // save schedule event to profile
        btnSave.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            String venue = editVenue.getText().toString().trim();
            String dateString = editDate.getText().toString().trim();
            String timeString = editTime.getText().toString().trim();

            if (title.isEmpty()) {
                editTitle.setError("Title is required");
                return;
            }

            if (dateString.isEmpty()) {
                editDate.setError("Date is required");
                return;
            }

            if (timeString.isEmpty()) {
                editTime.setError("Time is required");
                return;
            }

            if (venue.isEmpty()) {
                editVenue.setError("Venue is required");
                return;
            }

            // parsing in case user types in
            if (selectedTime == null) {
                selectedTime = parseTime(timeString);
                if (selectedTime == null) {
                    editTime.setError("Invalid time format");
                    return;
                }
            }

            saveToFirestore(title, selectedDate, selectedTime, venue);
        });
    }

    /**
     * Parse Time
     * Parses any user-typed  time into hh:mm format
     */
    private LocalTime parseTime(String timeString) {
        return LocalTime.parse(timeString);
    }

    /**
     * Save To Firestore
     * Gives event a unique id
     * Converts date and time to strings
     * Creates a new ScheduleEvent object with parameters
     * Stores event in "events" subcollection in "users"
     * Redirects user to profile
     * @param title (String)
     * @param date (LocalData)
     * @param time (LocalTime)
     * @param venue (String)
     */
    private void saveToFirestore(String title, LocalDate date, LocalTime time, String venue) {
        String eventId = UUID.randomUUID().toString();

        // have to convert to strings as firestore doesn't support localdate and localtime types
        String dateString = String.format("%d-%02d-%02d", date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        String timeString = String.format("%02d:%02d", time.getHour(), time.getMinute());

        ScheduleEvent event = new ScheduleEvent(eventId, userId, title, dateString, timeString, venue);

        firestore.collection("users")
                .document(userId)
                .collection("events")
                .document(eventId)
                .set(event)
                .addOnSuccessListener(aVoid -> {
                    requireActivity().getSupportFragmentManager().popBackStack();
                });
    }
}