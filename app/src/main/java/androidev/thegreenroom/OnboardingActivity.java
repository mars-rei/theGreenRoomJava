package androidev.thegreenroom;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import android.widget.Toast;

public class OnboardingActivity extends AppCompatActivity {

    private Button nextButton;
    private LinearLayout btnVenue, btnMusician, btnGiggoer;

    private String selectedUserType = null;
    private DataStoreManager dataStoreManager;

    /**
     * On Create
     * Initialises DataStore
     * Sets on click listeners for each button and option
     * If an option is selected, the selectedUserType is set
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        // initialising DataStoreManager
        dataStoreManager = new DataStoreManager(this);

        btnVenue = findViewById(R.id.btn_venue);
        btnMusician = findViewById(R.id.btn_musician);
        btnGiggoer = findViewById(R.id.btn_giggoer);
        nextButton = findViewById(R.id.btn_next);

        btnVenue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectOption(btnVenue);
                selectedUserType = "venue";
            }
        });

        btnMusician.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectOption(btnMusician);
                selectedUserType = "musician";
            }
        });

        btnGiggoer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectOption(btnGiggoer);
                selectedUserType = "gig-goer";
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedUserType != null) {
                    saveToDataStore();
                } else {
                    Toast.makeText(OnboardingActivity.this,
                            "You have not selected a user type", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Select Option
     * Choice state management
     * Ensures the selected option is the option highlighted in the UI
     */
    private void selectOption(LinearLayout selectedBtn) {
        btnVenue.setSelected(false);
        btnMusician.setSelected(false);
        btnGiggoer.setSelected(false);

        selectedBtn.setSelected(true);
    }

    /**
     * Save To DataStore
     * Sets userType in Datastore
     * Flags onboarding as incomplete for the first time
     * Redirects user to the main activity (the main app)
     */
    // save to DataStore
    private void saveToDataStore() {
        new Thread(() -> {
            dataStoreManager.setUserType(selectedUserType);
            dataStoreManager.setOnboardingComplete(false);

            runOnUiThread(() -> {
                Intent intent = new Intent(OnboardingActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }).start();
    }
}