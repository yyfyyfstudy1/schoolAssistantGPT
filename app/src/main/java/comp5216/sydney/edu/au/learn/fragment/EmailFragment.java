package comp5216.sydney.edu.au.learn.fragment;

import static android.content.ContentValues.TAG;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;


import java.util.ArrayList;

import comp5216.sydney.edu.au.learn.R;
import comp5216.sydney.edu.au.learn.viewAdapter.HistoryListAdapter;
import comp5216.sydney.edu.au.learn.viewAdapter.PreferenceListAdapter;
import comp5216.sydney.edu.au.learn.util.FireBaseUtil;
public class EmailFragment extends Fragment {

    private RecyclerView expandableRecyclerView1;
    private RecyclerView expandableRecyclerView2;
    private  PreferenceListAdapter preferenceListAdapter;
    private HistoryListAdapter historyListAdapter;
    private MaterialButton composeEmailBtn;
    private EditText thoughtEditText;

    private gptResponseFragment gptResponseFragment;

    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_email, container, false);
        Bundle arguments = getArguments();
        if (arguments != null) {
            userId = arguments.getString("userId");
            Log.d(TAG, "onCreateView: "+ userId);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        composeEmailBtn = view.findViewById(R.id.composeEmailBtn);
        thoughtEditText = view.findViewById(R.id.thoughtEditText);

        // set preference recycler view
        setPreferenceView(view);

        // set history recycler view
        setHistoryView(view, userId);


        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroup);
        MaterialCardView cardView1 = view.findViewById(R.id.cardView1);
        MaterialCardView cardView2 = view.findViewById(R.id.cardView2);
        final MaterialButton emailButton = view.findViewById(R.id.emailButton);
        final MaterialButton messageButton = view.findViewById(R.id.messageButton);

        toggleGroup.check(R.id.emailButton);

        cardView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expandableRecyclerView1.getVisibility() == View.GONE) {
                    expandableView(expandableRecyclerView1);
                    collapseView(expandableRecyclerView2);
                } else {
                    collapseView(expandableRecyclerView1);
                }
            }
        });

        cardView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expandableRecyclerView2.getVisibility() == View.GONE) {
                    expandableView(expandableRecyclerView2);
                    collapseView(expandableRecyclerView1);
                } else {
                    collapseView(expandableRecyclerView2);
                }
            }
        });

        toggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (checkedId == R.id.emailButton) {


                } else if (checkedId == R.id.messageButton) {
                    // switch to message model
                }
            }
        });

        // click with call gpt API
        composeEmailBtn.setOnClickListener(this::composeEmailClick);

    }

    private void setPreferenceView(View view){
        // create and set recycler view layout manager
        expandableRecyclerView1 = view.findViewById(R.id.expandableLayout1);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        expandableRecyclerView1.setLayoutManager(layoutManager);

        ArrayList<String> preferenceList = new ArrayList<>();
        preferenceList.add("to ask tutor...");
        preferenceList.add("to mention the team member.....");
        preferenceList.add("to ask tutor...");
        preferenceList.add("to ask tutor...");
        preferenceList.add("to mention the team member.....");
        preferenceList.add("to ask tutor...");
        preferenceList.add("to ask tutor...");
        preferenceList.add("to mention the team member.....");
        preferenceList.add("to ask tutor...");
        preferenceList.add("to ask tutor...");
        preferenceList.add("to mention the team member.....");
        preferenceList.add("to ask tutor...");


        // create and set the adapter
        preferenceListAdapter = new PreferenceListAdapter(getContext(),preferenceList);
        expandableRecyclerView1.setAdapter(preferenceListAdapter);

    }


    private void setHistoryView(View view, String userId){

        // create and set recycler view layout manager
        expandableRecyclerView2 = view.findViewById(R.id.expandableLayout2);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        expandableRecyclerView2.setLayoutManager(layoutManager);

        // create and set the adapter
        historyListAdapter = new HistoryListAdapter(getContext(),FireBaseUtil.getUserEmailHistory(userId), clickListener);
        expandableRecyclerView2.setAdapter(historyListAdapter);

    }

    private void composeEmailClick(View view){
        gptResponseFragment gptResponseFragment = new gptResponseFragment();
        Bundle args = new Bundle();
        args.putString("userEditContent", thoughtEditText.getText().toString());
        args.putString("userId", userId);
        gptResponseFragment.setArguments(args);
        // change fragment
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_container, gptResponseFragment);
        // Add FragmentA to the back stack so the user can return to it
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();


    }

    private void expandableView(final RecyclerView layout) {
        layout.setVisibility(View.VISIBLE);
    }

    private void collapseView(final RecyclerView layout) {
        layout.setVisibility(View.GONE);
    }

    HistoryListAdapter.OnItemClickListener clickListener = new HistoryListAdapter.OnItemClickListener() {

        @Override
        public void onClick(int pos, String emailContent) {
            // prepare parameter
            Bundle args = new Bundle();
            args.putString("emailHistoryContent", emailContent);
            args.putString("userId", userId);
            if (gptResponseFragment == null){
                gptResponseFragment = new gptResponseFragment();
            }
            gptResponseFragment.setArguments(args);

            // execute Fragment change
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, gptResponseFragment);
            transaction.addToBackStack(null); // Add FragmentA to the back stack so the user can return to it
            transaction.commitAllowingStateLoss();
        }
    };


}
