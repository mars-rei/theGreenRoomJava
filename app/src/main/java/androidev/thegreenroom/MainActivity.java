package androidev.thegreenroom;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// for onboarding dialog overlay
import androidx.appcompat.app.AlertDialog;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private DataStoreManager dataStoreManager;

    private Fragment firstFragment, secondFragment, thirdFragment;
    private boolean isOnboardingComplete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialising DataStoreManager
        dataStoreManager = new DataStoreManager(this);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        firstFragment = new EventsFragment();
        secondFragment = new FeedFragment();
        thirdFragment = new ProfileFragment();

        setCurrentFragment(secondFragment);

        // set feed as default after stage 1 of onboarding
        bottomNavigationView.setSelectedItemId(R.id.feed);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // if user has not completed profile customisation, show dialog
            if (!isOnboardingComplete && id == R.id.feed) {
                showOnboardingDialog();
                return true;
            }

            if (id == R.id.events) {
                setCurrentFragment(firstFragment);
            } else if (id == R.id.feed) {
                setCurrentFragment(secondFragment);
            } else if (id == R.id.profile) {
                setCurrentFragment(thirdFragment);
            }
            return true;
        });

        // check if the dialog has to be shown
        checkOnboardingStatus();
    }

    private void checkOnboardingStatus() {
        new Thread(() -> {
            boolean onboardingComplete = dataStoreManager.isOnboardingCompletedBlocking();

            runOnUiThread(() -> {
                if (!onboardingComplete) {
                    isOnboardingComplete = false;
                    thirdFragment = new ProfileEditFragment();

                    // show dialogue if not complete
                    setCurrentFragment(secondFragment);
                    bottomNavigationView.setSelectedItemId(R.id.feed);
                    showOnboardingDialog();
                } else {
                    isOnboardingComplete = true;
                    thirdFragment = new ProfileFragment();

                    setCurrentFragment(secondFragment);
                    bottomNavigationView.setSelectedItemId(R.id.feed);
                }
            });
        }).start();
    }

    private void showOnboardingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Welcome!")
                .setMessage("Customise your profile to start using The Green Room.")
                .setPositiveButton("Go to profile", (dialog, id) -> {
                    // redirect to the 4th fragment (profile)
                    bottomNavigationView.setSelectedItemId(R.id.profile);
                    setCurrentFragment(thirdFragment);
                })
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, fragment)
                .commit();
    }

    // to ensure the right fragment of profile is shown
    public void switchToReadableProfile() {
        thirdFragment = new ProfileFragment();
        isOnboardingComplete = true;

        if (bottomNavigationView.getSelectedItemId() == R.id.profile) {
            setCurrentFragment(thirdFragment);
        }
    }
}