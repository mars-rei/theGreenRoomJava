package androidev.thegreenroom;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;

import android.os.Bundle;
public class SplashActivity extends AppCompatActivity { private long ms=0;
    private static long splashTime = 5000;

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
                        ms = ms+100;
                        sleep(100);
                    }
                } catch (Exception e) {

                } finally {
                    boolean onboardingComplete = dataStoreManager.isOnboardingCompletedBlocking();

                    runOnUiThread(() -> {
                        if (onboardingComplete) {
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        } else {
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