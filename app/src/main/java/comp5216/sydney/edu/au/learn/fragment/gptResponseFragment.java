package comp5216.sydney.edu.au.learn.fragment;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSONObject;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Objects;

import comp5216.sydney.edu.au.learn.R;
import comp5216.sydney.edu.au.learn.util.FireBaseUtil;
import comp5216.sydney.edu.au.learn.util.NetworkUtils;
import comp5216.sydney.edu.au.learn.util.toastUtil;
import io.noties.markwon.Markwon;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class gptResponseFragment extends Fragment {
    private TextView gptResponseWebView;
    private ImageButton senToGptBtn;

    private String userEditContent;
    private String emailHistoryContent;
    private String userId;
    private String gptResponse;
    private EditText myEditText;

    private ProgressBar progressBar;
    private TextView loadingText;

    private String userPreferenceChoose;

    private boolean isMessage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gptresponse, container, false);
        return view;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // get parameter
        Bundle args = getArguments();

        // bind the component
        gptResponseWebView = view.findViewById(R.id.gptResponseWebView);
        senToGptBtn = view.findViewById(R.id.senToGptBtn);
        myEditText = view.findViewById(R.id.myEditText);
        progressBar = view.findViewById(R.id.progressBar);
        loadingText = view.findViewById(R.id.loadingText);
        // before get the gpt first response, the button can`t click
        senToGptBtn.setEnabled(false);


        if (args != null) {
            userEditContent = args.getString("userEditContent");
            userId = args.getString("userId");
            emailHistoryContent = args.getString("emailHistoryContent");
            isMessage = args.getBoolean("isMessage");

            if (args.getString("userPreference") !=null){
                userPreferenceChoose = args.getString("userPreference");
            }


            if (emailHistoryContent == null){
                // call the chatGpt api
                callGptForResponse(userEditContent);
            }else {
                gptResponse = emailHistoryContent;
                showLoading(false);
                senToGptBtn.setEnabled(true);
                setEmailHistoryContent(emailHistoryContent);


            }


        }

        senToGptBtn.setOnClickListener(this::fixContentWithGpt);
    }

    private void setEmailHistoryContent(String emailHistoryContent) {
        Markwon markwon = Markwon.create(Objects.requireNonNull(getContext()));
        markwon.setMarkdown(gptResponseWebView, emailHistoryContent);
//        gptResponseWebView.loadData(emailHistoryContent, "text/html", "UTF-8");
    }


    private void fixContentWithGpt(View view){
        showLoading(true);  // show loading bar
        String fixText = myEditText.getText().toString();
        StringBuilder requestText = new StringBuilder();

        requestText.append("my original email is: [ ");

        // convert the html content to text
        String plainText = Jsoup.parse(gptResponse).text();
        requestText.append(plainText);

        requestText.append("]. ");

        requestText.append("Help me fix the email content according to my under requirement." +
                "In order to preserve the formatting of the email, strictly return an Markdown style reply. Don`t say other things, I only need the fixed content of the email. ");

        requestText.append(fixText);

        myEditText.setText("");

        String requestBody = "{\"messages\": [{\"role\": \"system\", \"content\": \"You are a helpful assistant.\"}, {\"role\": \"system\", \"content\": \"[]\"}, {\"role\": \"user\", \"content\": \"" + requestText + "\"}], \"model\": \"gpt-3.5-turbo\"}";
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

    private void callGptForResponse(String inputText){
        showLoading(true);  // show loading bar
        StringBuilder requestText = new StringBuilder();

        if (isMessage){
            requestText.append("Please help me edit a message based on the following content.strictly return an Markdown style reply");
        }else {
            requestText.append("Convert the following request into a well-formatted email. In order to preserve the formatting of the email, strictly return an Markdown style reply, Don`t say other things,  I only need the content of the email.");
        }

        if (userPreferenceChoose!=null){
            requestText.append("Compose with the following style/tone: " + userPreferenceChoose);
        }

        requestText.append(inputText);
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

            gptResponse = content;

            Log.e(TAG, "response: " + content);
            // use getActivity() to get Activity
            if(getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        senToGptBtn.setEnabled(true);
                        // run this in the main tread
                        Log.e(TAG, "Load email content！！！！！！！！！！！！！！！！: " );


//                        gptResponseWebView.loadData(gptResponse, "text/html", "UTF-8");

                        Markwon markwon = Markwon.create(Objects.requireNonNull(getContext()));
                        markwon.setMarkdown(gptResponseWebView, gptResponse);


                        FireBaseUtil.insertEmailHistory(userId, gptResponse, new FireBaseUtil.EmailHistoryInsertionCallback() {
                            @Override
                            public void onInsertionCompleted(boolean success) {
                                if (success) {
                                    // insert successful
                                } else {
                                    // insert failed
                                }
                            }
                        });


                    }
                });
            }
        } else {

        }
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
    private void handleFailure(IOException e) {
        Log.e(TAG, "Exception: " + e);
    }
}
