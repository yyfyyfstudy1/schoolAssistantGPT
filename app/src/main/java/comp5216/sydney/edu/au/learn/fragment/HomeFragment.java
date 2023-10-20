package comp5216.sydney.edu.au.learn.fragment;

import static android.content.ContentValues.TAG;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import comp5216.sydney.edu.au.learn.BottomNavigationListener;
import comp5216.sydney.edu.au.learn.MainActivity;
import comp5216.sydney.edu.au.learn.R;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class HomeFragment extends Fragment {

    private TextView greetingTextView;
    private String userId;

    private MaterialCardView profileButton;

    private MaterialCardView slideButton;

    private MaterialCardView generateEmailButton;

    private MaterialCardView calendarButton;

    private Fragment profileFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        greetingTextView = view.findViewById(R.id.greetingUser);

        profileButton = view.findViewById(R.id.profile_button);
        slideButton = view.findViewById(R.id.slide_button);
        generateEmailButton = view.findViewById(R.id.generate_email_button);
        calendarButton = view.findViewById(R.id.calendar_button);

        profileButton.setOnClickListener(this::navigationButton);
        slideButton.setOnClickListener(this::navigationButton);
        generateEmailButton.setOnClickListener(this::navigationButton);
        calendarButton.setOnClickListener(this::navigationButton);

        String username = getUsername();

        Bundle arguments = getArguments();
        if (arguments != null) {
            userId = arguments.getString("userId");
        }

        // Find your MaterialCardViews
        MaterialCardView slideButton = view.findViewById(R.id.slide_button);
        MaterialCardView generateEmailButton = view.findViewById(R.id.generate_email_button);
        MaterialCardView calendarButton = view.findViewById(R.id.calendar_button);

        if (username != null) {
            greetingTextView.setText(username);
            // Set click listeners for the MaterialCardViews
            slideButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Replace the current fragment with the SlideFragment
                    Log.d("MyTag", "Reached here");
                    loadFragment(new LectureFragment(), userId);
                }
            });
            generateEmailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Replace the current fragment with the SlideFragment
                    Log.d("MyTag", "Reached here");
                    loadFragment(new EmailFragment(), userId);
                }
            });
            calendarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Replace the current fragment with the SlideFragment
                    Log.d("MyTag", "Reached here");
                    loadFragment(new TimeTableFragment(), userId);
                }
            });

        }


    }


    private void navigationButton(View view) {

        if (getActivity() instanceof BottomNavigationListener) {
            switch (view.getId()) {
                case R.id.profile_button:
                    profileFragment = new profileFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fl_container, profileFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    break;
                case R.id.slide_button:
                    ((BottomNavigationListener) getActivity()).onBottomNavigationItemSelected(R.id.navigation_lecture);
                    break;
                case R.id.generate_email_button:
                    ((BottomNavigationListener) getActivity()).onBottomNavigationItemSelected(R.id.navigation_email);
                    break;
                case R.id.calendar_button:
                    ((BottomNavigationListener) getActivity()).onBottomNavigationItemSelected(R.id.navigation_timetable);
                    break;
            }

        }
    }


    private String getUsername() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getDisplayName();
        }
        return null;
    }

    private void loadFragment(Fragment fragment, String userId) {
        Bundle bundle = new Bundle();
        bundle.putString("userId", userId);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_container, fragment);
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();
    }



}
