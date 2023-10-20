package comp5216.sydney.edu.au.learn.fragment;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

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

    ArrayList<String> preferenceList;

    private String userPreferenceChoose;

    private boolean isMessage = false;

    private ScrollView scrollView;

    private ImageButton audioButton;


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
        scrollView = view.findViewById(R.id.ScrollView);
        audioButton = view.findViewById(R.id.audioButton);

        // set preference recycler view
        setPreferenceView(view);

        // set history recycler view
        setHistoryView(view, userId);

        MaterialCardView cardView1 = view.findViewById(R.id.cardView1);
        MaterialCardView cardView2 = view.findViewById(R.id.cardView2);

        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroup);
        final MaterialButton emailButton = view.findViewById(R.id.emailButton);
        final MaterialButton messageButton = view.findViewById(R.id.messageButton);

        toggleGroup.check(R.id.emailButton);

        toggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (isChecked) {
                    switch (checkedId) {
                        case R.id.emailButton:
                            isMessage = false;
                            break;
                        case R.id.messageButton:
                            isMessage = true;
                            break;
                    }
                }
            }
        });


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
        audioButton.setOnClickListener(this::convertSpeechToText);
    }


    private void setPreferenceView(View view){
        // create and set recycler view layout manager
        expandableRecyclerView1 = view.findViewById(R.id.expandableLayout1);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        expandableRecyclerView1.setLayoutManager(gridLayoutManager);

        preferenceList = new ArrayList<>();
        preferenceList.add("🤗 " + "Friendly");
        preferenceList.add("😡 " + "Brutal");
        preferenceList.add("🏆 " + "Confident");
        preferenceList.add("🤩 " + "Joyful");
        preferenceList.add("🥳 " + "Exciting");
        preferenceList.add("🥸 " + "Information");


        // create and set the adapter
        preferenceListAdapter = new PreferenceListAdapter(getContext(),preferenceList, preferenceClickListener);
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
        args.putBoolean("isMessage", isMessage);
        if (userPreferenceChoose !=null){
            args.putString("userPreference", userPreferenceChoose);
        }
        gptResponseFragment.setArguments(args);
        // change fragment
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_container, gptResponseFragment);
        // Add FragmentA to the back stack so the user can return to it
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();


    }

    private void expandableView(final RecyclerView layout) {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
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

    PreferenceListAdapter.OnItemClickListener preferenceClickListener = new PreferenceListAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position) {
            String selectedItem = preferenceList.get(position);
            String filteredString = removeEmoji(selectedItem);

            // add preference


            Log.d("yyf", filteredString);
            userPreferenceChoose = filteredString;


        }
    };


    public static String removeEmoji(String source) {
        if (source == null) {
            return null;
        }

        // 这是一个用于匹配大部分emoji的正则表达式
        String regex = "[\\uD800-\\uDFFF]";
        return source.replaceAll(regex, "");
    }



    private void convertSpeechToText(View view) {
        Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (speechIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(speechIntent, 10);
        } else {

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 10 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            // 输出或显示转换的文字
            thoughtEditText.setText(result.get(0));
        }
    }




}
