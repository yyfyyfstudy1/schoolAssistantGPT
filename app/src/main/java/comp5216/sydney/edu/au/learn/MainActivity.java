package comp5216.sydney.edu.au.learn;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import comp5216.sydney.edu.au.learn.fragment.HomeFragment;
import comp5216.sydney.edu.au.learn.fragment.EmailFragment;
import comp5216.sydney.edu.au.learn.fragment.LectureFragment;
import comp5216.sydney.edu.au.learn.fragment.profileFragment;

public class MainActivity extends AppCompatActivity {
    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.simple_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);  // disable default title
        TextView toolbar_title = findViewById(R.id.toolbar_title);


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // get SharedPreferences instance
        SharedPreferences sharedPreferences = getSharedPreferences("comp5216", Context.MODE_PRIVATE);

        String userId = sharedPreferences.getString("userId", null);

        if (userId == null) {
            // if userId not exit, return to the login page
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        } else {

            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        toolbar_title.setText("Home");
                        loadFragment(new HomeFragment(), userId);
                        return true;
                    case R.id.navigation_email:
                        toolbar_title.setText("General EMail");
                        loadFragment(new EmailFragment(), userId);
                        return true;
                    case R.id.navigation_lecture:
                        toolbar_title.setText("Smart Lecture");
                        loadFragment(new LectureFragment(), userId);
                        return true;
                    case R.id.navigation_profile:
                        toolbar_title.setText("My Profile");
                        loadFragment(new profileFragment(), userId);
                        return true;
                }
                return false;
            });

            // load a fragment by default
            loadFragment(new HomeFragment(), userId);
        }


    }

    private void loadFragment(Fragment fragment, String userId) {
        Bundle bundle = new Bundle();
        bundle.putString("userId", userId);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_container, fragment);
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();
    }

}