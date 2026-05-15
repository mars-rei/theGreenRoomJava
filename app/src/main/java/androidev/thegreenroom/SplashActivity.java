package androidev.thegreenroom;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;

import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {
    private long ms = 0;
    private static long splashTime = 5000; // 5 seconds wait

    // to direct to the right page depending on if onboarding has been completed or not
    private DataStoreManager dataStoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); setContentView(R.layout.splash);

        // initialising DataStoreManager
        dataStoreManager = new DataStoreManager(this);

        Thread mythread = new Thread() {
            public void run(){
                try {
                    while (ms < splashTime) {
                        ms = ms + 100;
                        sleep(100);
                    }
                } catch (Exception e) {

                } finally {
                    // check if onboarding has been completed
                    boolean onboardingComplete = dataStoreManager.isOnboardingCompletedBlocking();

                    runOnUiThread(() -> {
                        if (onboardingComplete) {
                            // if complete, go to main activity
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        } else {
                            // if not complete, start onboarding
                            startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
                        }
                        finish();
                    });
                }
            }
        };
        mythread.start();
    }
}