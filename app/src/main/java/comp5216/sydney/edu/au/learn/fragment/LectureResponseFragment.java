package comp5216.sydney.edu.au.learn.fragment;

import static android.content.ContentValues.TAG;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.snackbar.Snackbar;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import comp5216.sydney.edu.au.learn.R;
import comp5216.sydney.edu.au.learn.util.FireBaseUtil;
import comp5216.sydney.edu.au.learn.util.NetworkUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LectureResponseFragment extends Fragment {



    private String userEditContent;
    private String emailHistoryContent;
    private String userId;

    private ExecutorService executorService;
    private List<String> results = Collections.synchronizedList(new ArrayList<String>());
    private CountDownLatch latch;

    private View rootView;
    private String pdfFilePath;

    private Boolean isFromHistory = false;
    // this is from history
    private String pdfName;
    private Button GetHistoryBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_lecresponse, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        executorService = Executors.newFixedThreadPool(3);

//        // bind the component
//        gptResponseWebView = view.findViewById(R.id.gptResponseWebView);
        GetHistoryBtn = view.findViewById(R.id.GetHistoryBtn);
        GetHistoryBtn.setText("The pdf content is being analyzed. . .");
        GetHistoryBtn.setEnabled(false);
        // get parameter
        Bundle args = getArguments();
        if (args != null) {
            userId = args.getString("userId");
            // if the pdf is from the history
            if (args.getString("pdfName") != null){
                isFromHistory = true;
                pdfName = args.getString("pdfName");
                // get the pdf url by pdfName
                FireBaseUtil.getLocalPdfByUserIdAndFileName(getContext(), userId, pdfName, new FireBaseUtil.FirebaseDownloadCallback() {

                    @Override
                    public void onSuccess(File localFile) {
                        initPdfView(localFile.getAbsolutePath());
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        Log.d("pdf Load faild", String.valueOf(exception));
                    }
                });


            }else {

                pdfFilePath = args.getString("filePath");

                initPdfView(pdfFilePath);

                // upload pdf file to firebase
                FireBaseUtil.uploadFileToFirebase(userId, pdfFilePath, firebaseUploadCallback, firestoreUploadCallback);
            }

        }

        GetHistoryBtn.setOnClickListener(this::showChat);
    }

    private void showChat(View view) {

        // 展开bottom view
        MyBottomSheetDialogFragment bottomSheet = MyBottomSheetDialogFragment.newInstance();
        Bundle args = new Bundle();


        StringBuilder contextText = new StringBuilder();

        for (String sk1: results){

            // append all the summary point of PDF
            // Remove newlines and special characters
            String cleanText = sk1.replaceAll("[^a-zA-Z0-9\\s]|[\r\n]+", " ");
            contextText.append(cleanText);
        }

        // from general
        if (pdfFilePath !=null){
            String fileName = new File(pdfFilePath).getName();
            String baseName = FilenameUtils.getBaseName(fileName);
            args.putString("pdfName", baseName);
        }

        // from history
        if (pdfName !=null){
            args.putString("pdfName", pdfName);
        }

        args.putString("userId", userId);
        args.putString("pdfContentSummary", contextText.toString());
        bottomSheet.setArguments(args);
        bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());

    }




    private void initPdfView(String filePath) {
        PDFView pdfView = getView().findViewById(R.id.pdfViewPager);
        File pdfFile = new File(filePath);
        pdfView.fromFile(pdfFile)
                .swipeHorizontal(false)  // 设置纵向滚动
                .spacing(30)
                .load();

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
        GetHistoryBtn.setEnabled(true);
        GetHistoryBtn.setText("Ask question based on your pdf !");
        GetHistoryBtn.setBackgroundResource(R.drawable.custom_buttonsuccessful_background);
        if (isFromHistory){
        }else {
//            callGptForQuestion();
        }

    }

//    //  call gpt again, to get the question based on the analyse result
//    private void callGptForQuestion(){
//        StringBuilder requestText = new StringBuilder();
//        requestText.append("The following content is the summary of PDF [");
//
//        for (String ss : results){
//            // Remove newlines and special characters
//            String cleanText = ss.replaceAll("[^a-zA-Z0-9\\s]|[\r\n]+", " ");
//            requestText.append(cleanText);
//        }
//
//        requestText.append("]. now, give me three question based on the content, " +
//                "In order to preserve the formatting of the content, strictly reply the three question with jsonArray style (for example: ['question1', 'question2'] )." +
//                "i only need the question, don`t response any other content like answer of the question.");
//        String requestBody = "{\"messages\": [{\"role\": \"system\", \"content\": \"You are a helpful assistant.\"}, {\"role\": \"system\", \"content\": \"[clear context]\"}, {\"role\": \"user\", \"content\": \"" + requestText + "\"}], \"model\": \"gpt-3.5-turbo\"}";
//
//
//        NetworkUtils.postJsonRequest(requestBody, new Callback() {
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                handleResponseQuestion(response);
//
//            }
//
//            @Override
//            public void onFailure(Call call, IOException e) {
//                handleFailure(e);
//            }
//        });
//    }
//    /*
//    * handle the response of gpt
//    * */
//    private void handleResponseQuestion(Response response) throws IOException {
//        showLoading(false);
//        String responseBody = response.body().string();
//
//        if (response.code() == 200) {
//
//            JSONObject jsonObject = JSONObject.parseObject(responseBody);
//            JSONObject firstChoice = jsonObject.getJSONArray("choices").getJSONObject(0);
//            JSONObject message = firstChoice.getJSONObject("message");
//            String content = message.getString("content");
//
//            // Replace escape characters
//            String jsonString = content.replace("\\n", "\n").replace("\\\"", "\"");
//            Log.e(TAG, "response: " + jsonString);
//
//            // Use fastjson to parse this JSON array
//            JSONArray jsonArray = JSONArray.parseArray(jsonString);
//
//            // create a list to store the question
//            List<String> questionsList = new ArrayList<>();
//
//            // Loop through the JSONArray and add each string to the List
//            for (int i = 0; i < jsonArray.size(); i++) {
//                questionsList.add(jsonArray.getString(i));
//            }
//
//
//            StringBuilder finalQuestionHtml = new StringBuilder();
//            finalQuestionHtml.append("<h3>example question </h3>");
//            for (String vv : questionsList){
//                finalQuestionHtml.append("<span style=\"color:gray\">").append(vv).append("</span>").append("<br><br>");
//            }
//
//            // use getActivity() to get Activity
//            if(getActivity() != null) {
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        // run this in the main tread
//                        gptResponseWebView.loadData(finalQuestionHtml.toString(), "text/html", "UTF-8");
//
//                    }
//                });
//            }
//        } else {
//
//        }
//    }

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



    FireBaseUtil.FirebaseUploadCallback firebaseUploadCallback = new FireBaseUtil.FirebaseUploadCallback() {
        @Override
        public void onSuccess(String fileName) {
        }

        @Override
        public void onFailure(Exception exception) {
        }
    };

    FireBaseUtil.FirestoreUploadCallback firestoreUploadCallback = new FireBaseUtil.FirestoreUploadCallback() {
        @Override
        public void onSuccess() {
        }

        @Override
        public void onFailure(Exception exception) {
            Snackbar.make(rootView, "PDF uploaded Invalid", Snackbar.ANIMATION_MODE_FADE)
                    .setAction("Action", null).show();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        // close tread pool
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }


}
