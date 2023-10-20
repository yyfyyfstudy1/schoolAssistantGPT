package comp5216.sydney.edu.au.learn.fragment;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import comp5216.sydney.edu.au.learn.R;
import comp5216.sydney.edu.au.learn.Sensor.ShakeDetector;
import comp5216.sydney.edu.au.learn.Sensor.ShakeListener;
import comp5216.sydney.edu.au.learn.util.FireBaseUtil;
import comp5216.sydney.edu.au.learn.util.NetworkUtils;
import comp5216.sydney.edu.au.learn.util.toastUtil;
import io.noties.markwon.Markwon;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TimeTableResponseFragment extends Fragment {
    private View rootView;
    private String userId;

    private TextView gptResponseWebView;
    private ImageButton senToGptBtn;
    private String gptResponse;
    private EditText myEditText;
    private ProgressBar progressBar;
    private TextView loadingText;

    private String initialText;

    private ShakeDetector shakeDetector;

    private boolean canShake = true;

    private ImageButton audioButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_timetableresponse, container, false);

        Bundle arguments = getArguments();
        if (arguments != null) {
            userId = arguments.getString("userId");
        }

        return rootView;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toastUtil.topSnackBar(view, "Shake your phone to get your next week's plan!");
        // bind the component
        initialText = "give me my this week's plan";

        gptResponseWebView = view.findViewById(R.id.gptResponseWebView);
        senToGptBtn = view.findViewById(R.id.senToGptBtn);
        myEditText = view.findViewById(R.id.myEditText);
        progressBar = view.findViewById(R.id.progressBar);
        loadingText = view.findViewById(R.id.loadingText);
        audioButton = view.findViewById(R.id.audioButton);

        senToGptBtn.setOnClickListener(this::getGptResponseBasedOnTimeTable);
        audioButton.setOnClickListener(this::convertSpeechToText);

        shakeDetector = new ShakeDetector(Objects.requireNonNull(getContext()), new ShakeListener() {
            @Override
            public void onShake() {

                if (canShake){

                    getGptResponseBasedOnTimeTable(view);

                }

            }
        });

    }
    @Override
    public void onResume() {
        super.onResume();
        shakeDetector.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        shakeDetector.stop();
    }
    private void getGptResponseBasedOnTimeTable(View view){

        canShake = false;
        // get the summary timetable information from the database
        FireBaseUtil.fetchAndFormatSchedule(userId, new FireBaseUtil.FirebaseFetchCallback() {
            @Override
            public void onSuccess(String formattedData) {
                // Use the formatted data
                System.out.println("！！！！！！！！！！" + formattedData);
                callGptForResponse(formattedData);
            }

            @Override
            public void onFailure(Exception e) {
                // Handle the error
                System.out.println(e);
            }
        });

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
            // display converted text
            myEditText.setText(result.get(0));
        }
    }

    private void callGptForResponse(String formattedData){

        String userInput = myEditText.getText().toString();

        myEditText.setText("");
        gptResponseWebView.setText("");

        showLoading(true);  // show loading bar
        StringBuilder requestText = new StringBuilder();
        requestText.append("This is my timetable [ ");
        requestText.append(formattedData);
        requestText.append("]. Now please answer my following question based on this timetable. strictly return an Markdown style reply, Do not reply with anything other content than the markdown style answer. ");

        if (userInput.equals("")){
            // if the user input is empty, list the user this week`s plan
            requestText.append(initialText);
        }else{
            requestText.append(userInput);
        }


        String requestBody = "{\"messages\": [{\"role\": \"system\", \"content\": \"You are a helpful assistant.\"}, {\"role\": \"system\", \"content\": \"[clear context]\"}, {\"role\": \"user\", \"content\": \"" + requestText + "\"}], \"model\": \"gpt-3.5-turbo\"}";
        NetworkUtils.postJsonRequest(requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                showLoading(false);  // hidden loading bar
                handleResponse(response);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                showLoading(false);  // hidden loading bar
                handleFailure(e);
            }
        });

    }


    private void handleResponse(Response response) throws IOException {

        canShake = true;
        String responseBody = response.body().string();
        Log.e(TAG, "gpt what happened: " + responseBody);
        if (response.code() == 200) {


            JSONObject jsonObject = JSONObject.parseObject(responseBody);
            JSONObject firstChoice = jsonObject.getJSONArray("choices").getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            String content = message.getString("content");


            gptResponse = content;

            Log.e(TAG, "response: " + content);
            // use getActivity() to get Activity
            if(getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // run this in the main tread
                        Log.e(TAG, "Load timetable response content！！！！！！！！！！！！！！！！: " );

                        Markwon markwon = Markwon.create(Objects.requireNonNull(getContext()));
                        markwon.setMarkdown(gptResponseWebView, content);

                    }
                });
            }
        } else {

        }
    }




    private void handleFailure(IOException e) {
        Log.e(TAG, "Exception: " + e);
    }


    private void showLoading(final boolean show) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingText.setVisibility(show ? View.VISIBLE : View.GONE);
                    progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        }
    }
}
