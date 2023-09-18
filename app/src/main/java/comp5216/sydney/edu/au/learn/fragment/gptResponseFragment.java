package comp5216.sydney.edu.au.learn.fragment;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

import comp5216.sydney.edu.au.learn.R;
import comp5216.sydney.edu.au.learn.util.NetworkUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class gptResponseFragment extends Fragment {
    private WebView gptResponseWebView;
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
        gptResponseWebView = view.findViewById(R.id.gptResponseWebView);
        WebSettings webSettings = gptResponseWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        if (args != null) {
            String userEditContent = args.getString("userEditContent");

            // call the chatGpt api
            callGptForResponse(userEditContent);



        }
    }

    private void callGptForResponse(String inputText){
        StringBuilder requestText = new StringBuilder();
        requestText.append("Convert the following request into a well-formatted email. In order to preserve the formatting of the email, strictly return an HTML style reply (needs to have recognizable html tags), I only need the content of the email.");
        requestText.append(inputText);
        String requestBody = "{\"messages\": [{\"role\": \"system\", \"content\": \"You are a helpful assistant.\"}, {\"role\": \"system\", \"content\": \"[clear context]\"}, {\"role\": \"user\", \"content\": \"" + requestText + "\"}], \"model\": \"gpt-3.5-turbo\"}";
        NetworkUtils.postJsonRequest(requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handleResponse(response);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                handleFailure(e);
            }
        });

//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("<html><body><h1>");
//        stringBuilder.append(inputText);
//        stringBuilder.append("</h1></body></html>");

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

            Log.e(TAG, "response: " + unescapedHtml);
            // use getActivity() to get Activity
            if(getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // run this in the main tread
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
}
