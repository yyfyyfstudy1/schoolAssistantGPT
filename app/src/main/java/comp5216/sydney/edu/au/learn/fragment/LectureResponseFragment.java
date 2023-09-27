package comp5216.sydney.edu.au.learn.fragment;

import static android.content.ContentValues.TAG;

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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import comp5216.sydney.edu.au.learn.R;
import comp5216.sydney.edu.au.learn.util.NetworkUtils;
import es.voghdev.pdfviewpager.library.PDFViewPager;
import es.voghdev.pdfviewpager.library.adapter.PDFPagerAdapter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LectureResponseFragment extends Fragment {

    PDFViewPager pdfViewPager;
    PDFPagerAdapter pdfPagerAdapter;

    private WebView gptResponseWebView;
    private ImageButton senToGptBtn;

    private String userEditContent;
    private String emailHistoryContent;
    private String userId;
    private EditText myEditText;

    private ProgressBar progressBar;
    private TextView loadingText;

    private ExecutorService executorService;
    private List<String> results = Collections.synchronizedList(new ArrayList<String>());
    private CountDownLatch latch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lecresponse, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 创建一个线程池，它可以同时运行三个线程
        executorService = Executors.newFixedThreadPool(3);

        // bind the component
        gptResponseWebView = view.findViewById(R.id.gptResponseWebView);
        senToGptBtn = view.findViewById(R.id.senToGptBtn);
        myEditText = view.findViewById(R.id.myEditText);

        progressBar = view.findViewById(R.id.progressBar);
        loadingText = view.findViewById(R.id.loadingText);

        // before get the gpt first response, the button can`t click
        senToGptBtn.setEnabled(false);

        WebSettings webSettings = gptResponseWebView.getSettings();
        webSettings.setJavaScriptEnabled(false);

        // get parameter
        Bundle args = getArguments();
        if (args != null) {

          String pdfFilePath = args.getString("filePath");

            initPdfView(pdfFilePath);
        }

        senToGptBtn.setOnClickListener(this::askQuestionBaseOnSummary);

    }

    private void askQuestionBaseOnSummary(View view) {
        if (myEditText.getText()!=null){

            showLoading(true);  // show loading bar
            String userQuestion = myEditText.getText().toString();
            StringBuilder requestText = new StringBuilder();

            requestText.append("Based on the content to answer my question: [ ");

            for (String sk1: results){

                // append all the summary point of PDF
                // Remove newlines and special characters
                String cleanText = sk1.replaceAll("[^a-zA-Z0-9\\s]|[\r\n]+", " ");
                requestText.append(cleanText);
            }

            requestText.append("]. ");

            requestText.append(" my question is:");

            requestText.append(userQuestion);

            myEditText.setText("");

            String requestBody = "{\"messages\": [{\"role\": \"system\", \"content\": \"You are a helpful assistant.\"}, {\"role\": \"system\", \"content\": \"[]\"}, {\"role\": \"user\", \"content\": \"" + requestText + "\"}], \"model\": \"gpt-3.5-turbo\"}";
            NetworkUtils.postJsonRequest(requestBody, new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    showLoading(false);  // hidden loading bar
                    handleResponseAnswer(response);
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    showLoading(false);  // hidden loading bar
                    handleFailure(e);
                }
            });


        }
    }

    private void handleResponseAnswer(Response response) throws IOException {
        showLoading(false);
        String responseBody = response.body().string();

        if (response.code() == 200) {

            JSONObject jsonObject = JSONObject.parseObject(responseBody);
            JSONObject firstChoice = jsonObject.getJSONArray("choices").getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            String content = message.getString("content");

            StringBuilder finalAnswerHtml = new StringBuilder();
            finalAnswerHtml.append("<h3>GPT ANSWER </h3>");
            finalAnswerHtml.append(content);
            // use getActivity() to get Activity
            if(getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        senToGptBtn.setEnabled(true);
                        // run this in the main tread
                        gptResponseWebView.loadData(finalAnswerHtml.toString(), "text/html", "UTF-8");

                    }
                });
            }
        } else {

        }
    }


    private void initPdfView(String filePath) {
        showLoading(true);
        // 获取 PDFViewPager 实例
        pdfViewPager = getView().findViewById(R.id.pdfViewPager);

        // 创建 PDFPagerAdapter 实例
        pdfPagerAdapter = new PDFPagerAdapter(getContext(), filePath);

        // 将 PDFPagerAdapter 设置为 PDFViewPager 的适配器
        pdfViewPager.setAdapter(pdfPagerAdapter);


        try {
            ArrayList<String> textParts = pdfToText(filePath);

            // textParts contains three parts of the text
            multiTreadCallGpt(textParts);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void multiTreadCallGpt(ArrayList<String> textParts) {

        // Set the countdown latch count to 3 because there are three requests
        latch = new CountDownLatch(textParts.size());

        for (String textPart: textParts){
            executorService.submit(new RequestRunnable(textPart));
        }

        // waiting for all request finished
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    latch.await();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handleResults();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private class RequestRunnable implements Runnable {
        private String inputText;

        public RequestRunnable(String inputText) {
            this.inputText = inputText;
        }

        @Override
        public void run() {
            callGptForResponse(inputText);
        }
    }

    private void callGptForResponse(String inputText){
        StringBuilder requestText = new StringBuilder();
        requestText.append("The following content is part of the pdf file. You need to summarize its main knowledge points. Since it is the content of pdf translation, the text is relatively fragmented. Please combine some of the content. ");
        requestText.append(inputText);
        String requestBody = "{\"messages\": [{\"role\": \"system\", \"content\": \"You are a helpful assistant.\"}, {\"role\": \"system\", \"content\": \"[clear context]\"}, {\"role\": \"user\", \"content\": \"" + requestText + "\"}], \"model\": \"gpt-3.5-turbo\"}";


        NetworkUtils.postJsonRequest(requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                latch.countDown();  // Decrement the count of the countdown latch
                handleResponsePart(response);

            }

            @Override
            public void onFailure(Call call, IOException e) {
                latch.countDown();  // Decrement the count of the countdown latch
                handleFailure(e);
            }
        });

    }

    // this is a part of the pdf response
    private void handleResponsePart(Response response) throws IOException {
        String responseBody = response.body().string();
        Log.e(TAG, "response is ok ？ : " + responseBody);
        if (response.code() == 200) {

            JSONObject jsonObject = JSONObject.parseObject(responseBody);
            JSONObject firstChoice = jsonObject.getJSONArray("choices").getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            String content = message.getString("content");

            Log.e(TAG, "response: " + content);

            // add the result to the list
            results.add(content);

        } else {

        }
    }


    private void handleResults() {
        // 在这里处理 results 列表中的结果
       // showLoading(false);
        callGptForQuestion();
        System.out.println("逆天：" + results);
    }

    //  call gpt again, to get the question based on the analyse result
    private void callGptForQuestion(){
        StringBuilder requestText = new StringBuilder();
        requestText.append("The following content is the summary of PDF [");

        for (String ss : results){
            // Remove newlines and special characters
            String cleanText = ss.replaceAll("[^a-zA-Z0-9\\s]|[\r\n]+", " ");
            requestText.append(cleanText);
        }

        requestText.append("]. now, give me three question based on the content, " +
                "In order to preserve the formatting of the content, strictly reply the three question with jsonArray style (for example: ['question1', 'question2'] )." +
                "i only need the question, don`t response any other content like answer of the question.");
        String requestBody = "{\"messages\": [{\"role\": \"system\", \"content\": \"You are a helpful assistant.\"}, {\"role\": \"system\", \"content\": \"[clear context]\"}, {\"role\": \"user\", \"content\": \"" + requestText + "\"}], \"model\": \"gpt-3.5-turbo\"}";


        NetworkUtils.postJsonRequest(requestBody, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handleResponseQuestion(response);

            }

            @Override
            public void onFailure(Call call, IOException e) {
                handleFailure(e);
            }
        });
    }
    /*
    * handle the response of gpt
    * */
    private void handleResponseQuestion(Response response) throws IOException {
        showLoading(false);
        String responseBody = response.body().string();

        if (response.code() == 200) {

            JSONObject jsonObject = JSONObject.parseObject(responseBody);
            JSONObject firstChoice = jsonObject.getJSONArray("choices").getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            String content = message.getString("content");

            // Replace escape characters
            String jsonString = content.replace("\\n", "\n").replace("\\\"", "\"");
            Log.e(TAG, "response: " + jsonString);

            // Use fastjson to parse this JSON array
            JSONArray jsonArray = JSONArray.parseArray(jsonString);

            // create a list to store the question
            List<String> questionsList = new ArrayList<>();

            // Loop through the JSONArray and add each string to the List
            for (int i = 0; i < jsonArray.size(); i++) {
                questionsList.add(jsonArray.getString(i));
            }


            StringBuilder finalQuestionHtml = new StringBuilder();
            finalQuestionHtml.append("<h3>example question </h3>");
            for (String vv : questionsList){
                finalQuestionHtml.append("<span style=\"color:gray\">").append(vv).append("</span>").append("<br><br>");
            }

            // use getActivity() to get Activity
            if(getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        senToGptBtn.setEnabled(true);
                        // run this in the main tread
                        gptResponseWebView.loadData(finalQuestionHtml.toString(), "text/html", "UTF-8");

                    }
                });
            }
        } else {

        }
    }

    private void handleFailure(IOException e) {
        Log.e(TAG, "Exception: " + e);
    }


    public  ArrayList<String> pdfToText(String pdfFilePath) throws IOException {
        ArrayList<String> textParts = new ArrayList<>();
        PdfReader reader = new PdfReader(pdfFilePath);
        PdfDocument pdfDoc = new PdfDocument(reader);

        StringBuilder text = new StringBuilder();
        int numberOfPages = pdfDoc.getNumberOfPages();
        for (int i = 1; i <= numberOfPages; i++) {
            text.append(PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i)));
        }

        pdfDoc.close();
        // Remove special characters
        // Remove newlines and special characters
        String cleanText = text.toString().replaceAll("[^a-zA-Z0-9\\s]|[\r\n]+", " ");
        // divide the pdf file into four part
        int partLength = cleanText.length() / 3;
        for (int i = 0; i < 3; i++) {
            int start = i * partLength;
            int end = i == 2 ? cleanText.length() : start + partLength;  // Making sure the last part includes any leftover characters
            textParts.add(cleanText.substring(start, end));
        }

        return textParts;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        // release PDFPagerAdapter resource
        if (pdfPagerAdapter != null) {
            pdfPagerAdapter.close();
        }
        // close tread pool
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }


}
