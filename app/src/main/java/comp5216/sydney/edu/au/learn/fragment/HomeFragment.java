package comp5216.sydney.edu.au.learn.fragment;

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

        if (username != null) {
            greetingTextView.setText(username);
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
}
