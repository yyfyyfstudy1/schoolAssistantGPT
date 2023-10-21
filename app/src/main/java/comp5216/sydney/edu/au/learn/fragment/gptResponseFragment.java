package comp5216.sydney.edu.au.learn.fragment;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.Jsoup;

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

    private ImageButton audioButton;

    private ShakeDetector shakeDetector;

    private boolean canShake = false;

    private boolean isFixAnswer = false;

    private String fixText;

    private MaterialButton copyBtn;

    private MaterialButton shareBtn;

    private View rootView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_gptresponse, container, false);
        return rootView;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toastUtil.topSnackBar(view, "Shake to generate a new message! ");

        // get parameter
        Bundle args = getArguments();

        // bind the component
        gptResponseWebView = view.findViewById(R.id.gptResponseWebView);
        senToGptBtn = view.findViewById(R.id.senToGptBtn);
        myEditText = view.findViewById(R.id.myEditText);
        progressBar = view.findViewById(R.id.progressBar);
        loadingText = view.findViewById(R.id.loadingText);
        audioButton = view.findViewById(R.id.audioButton);
        copyBtn = view.findViewById(R.id.copyBtn);
        shareBtn = view.findViewById(R.id.shareBtn);
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
                canShake = true;

            }


        }

        senToGptBtn.setOnClickListener(this::fixContentWithGpt);
        audioButton.setOnClickListener(this::convertSpeechToText);
        copyBtn.setOnClickListener(this::copyText);
        shareBtn.setOnClickListener(this::shareEmail);

        shakeDetector = new ShakeDetector(getContext(), new ShakeListener() {
            @Override
            public void onShake() {

                if (canShake){
                    if (fixText == null){
                        callGptForResponse(userEditContent);
                    }else {
                        fixContentWithGpt(view);
                    }

                }

            }
        });

    }

    private void shareEmail(View view) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));  // only email apps should handle this
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"recipient@example.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Email Subject");
        emailIntent.putExtra(Intent.EXTRA_TEXT, gptResponseWebView.getText().toString());

        try {
            startActivity(emailIntent);
        } catch (ActivityNotFoundException e) {
            Snackbar.make(rootView, "No email client available", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

    }


    // copy Email to clipboard
    private void copyText(View view) {
        String textToCopy = gptResponseWebView.getText().toString();
        copyToClipboard(getContext(), textToCopy);
        // Handle success
        Snackbar.make(rootView, "Copy Email successful", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void copyToClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Email", text);
        clipboard.setPrimaryClip(clip);
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

    private void setEmailHistoryContent(String emailHistoryContent) {
        Markwon markwon = Markwon.create(Objects.requireNonNull(getContext()));
        markwon.setMarkdown(gptResponseWebView, emailHistoryContent);
//        gptResponseWebView.loadData(emailHistoryContent, "text/html", "UTF-8");
    }


    private void fixContentWithGpt(View view){
        // clear the show
        gptResponseWebView.setText("");
        canShake = false;
        showLoading(true);  // show loading bar

        if (!myEditText.getText().toString().equals("")){
            fixText = myEditText.getText().toString();
        }

        StringBuilder requestText = new StringBuilder();

        requestText.append("my original email is: [ ");

        // convert the html content to text
        String plainText = Jsoup.parse(gptResponse).text();
        requestText.append(plainText);

        requestText.append("]. ");

        requestText.append("Help me fix the email content according to my following requirement." +
                "In order to preserve the formatting of the email, strictly return an Markdown style reply. Don't say other things, I only need the fixed content of the email. ");

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

        canShake = false;

        gptResponseWebView.setText("");

        StringBuilder requestText = new StringBuilder();

        if (isMessage){
            requestText.append("Please help me edit a message based on the following content.strictly return an Markdown style reply");
        }else {
            requestText.append("Convert the following request into a well-formatted email. In order to preserve the formatting of the email, strictly return an Markdown style reply, Don't say other things,  I only need the content of the email.");
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

        canShake = true;
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
