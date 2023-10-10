package comp5216.sydney.edu.au.learn.fragment;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.os.Bundle;
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

import comp5216.sydney.edu.au.learn.R;
import comp5216.sydney.edu.au.learn.util.FireBaseUtil;
import comp5216.sydney.edu.au.learn.util.NetworkUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TimeTableResponseFragment extends Fragment {
    private View rootView;
    private String userId;

    private WebView gptResponseWebView;
    private ImageButton senToGptBtn;
    private String gptResponse;
    private EditText myEditText;
    private ProgressBar progressBar;
    private TextView loadingText;
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
        // bind the component
        gptResponseWebView = view.findViewById(R.id.gptResponseWebView);
        senToGptBtn = view.findViewById(R.id.senToGptBtn);
        myEditText = view.findViewById(R.id.myEditText);
        progressBar = view.findViewById(R.id.progressBar);
        loadingText = view.findViewById(R.id.loadingText);

        WebSettings webSettings = gptResponseWebView.getSettings();
        webSettings.setJavaScriptEnabled(false);

        senToGptBtn.setOnClickListener(this::getGptResponseBasedOnTimeTable);

    }

    private void getGptResponseBasedOnTimeTable(View view){

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

    private void callGptForResponse(String formattedData){

        String userInput = myEditText.getText().toString();

        showLoading(true);  // show loading bar
        StringBuilder requestText = new StringBuilder();
        requestText.append("This is my timetable [ ");
        requestText.append(formattedData);
        requestText.append("]. Now please answer my following question based on this timetable. strictly return an HTML style reply (needs to have recognizable html tags), Do not reply with anything other than html style content");
        requestText.append(userInput);

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
        String responseBody = response.body().string();

        if (response.code() == 200) {


            JSONObject jsonObject = JSONObject.parseObject(responseBody);
            JSONObject firstChoice = jsonObject.getJSONArray("choices").getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            String content = message.getString("content");

            // 替换转义字符
            String unescapedHtml = content.replace("\\n", "\n").replace("\\\"", "\"");

            gptResponse = unescapedHtml;

            Log.e(TAG, "response: " + unescapedHtml);
            // use getActivity() to get Activity
            if(getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // run this in the main tread
                        Log.e(TAG, "Load timetable response content！！！！！！！！！！！！！！！！: " );
                        gptResponseWebView.loadData(unescapedHtml, "text/html", "UTF-8");

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
