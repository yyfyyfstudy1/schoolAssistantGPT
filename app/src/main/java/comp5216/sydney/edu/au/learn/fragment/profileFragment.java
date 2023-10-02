package comp5216.sydney.edu.au.learn.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import comp5216.sydney.edu.au.learn.R;

public class profileFragment extends Fragment {

    private TextView nameTextView;
    private TextView emailTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        nameTextView = view.findViewById(R.id.profileName);
        emailTextView = view.findViewById(R.id.profileEmail);

        if(user != null) {
            String userName = user.getDisplayName();
            String userEmail = user.getEmail();

            if(userName != null)
                nameTextView.setText(userName);
            if(userEmail != null)
                emailTextView.setText(userEmail);
        }

    }
}
