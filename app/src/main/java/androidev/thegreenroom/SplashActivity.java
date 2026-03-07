package androidev.thegreenroom;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;

import android.os.Bundle;
public class SplashActivity extends AppCompatActivity { private long ms=0;
    private static long splashTime = 5000;
    @Override
    protected void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); setContentView(R.layout.splash);
        Thread mythread = new Thread() { public void run(){
            try {
                while (ms < splashTime) { ms = ms+100; sleep(100);
                }
            } catch (Exception e) {} finally {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class); startActivity(intent);
                finish();
            }
        }
        };
        mythread.start();
    }
}