package comp5216.sydney.edu.au.learn;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import comp5216.sydney.edu.au.learn.Common.HeaderFragment;
import comp5216.sydney.edu.au.learn.fragment.EmailFragment;
import comp5216.sydney.edu.au.learn.fragment.HomeFragment;
import comp5216.sydney.edu.au.learn.fragment.LectureFragment;
import comp5216.sydney.edu.au.learn.fragment.profileFragment;

public class MainActivity extends AppCompatActivity {
    private HeaderFragment headerFragment;
    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        headerFragment = new HeaderFragment();
        // fragment添加到Activity中
        getSupportFragmentManager().beginTransaction().add(R.id.fl_header, headerFragment, "header").commitAllowingStateLoss();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    loadFragment(new HomeFragment());
                    return true;
                case R.id.navigation_email:
                    loadFragment(new EmailFragment());
                    return true;
                case R.id.navigation_lecture:
                    loadFragment(new LectureFragment());
                    return true;
                case R.id.navigation_profile:
                    loadFragment(new profileFragment());
                    return true;
            }
            return false;
        });

        // 默认加载一个Fragment
        loadFragment(new HomeFragment());
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_container, fragment);
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();
    }

}