package comp5216.sydney.edu.au.learn.fragment;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import comp5216.sydney.edu.au.learn.Common.Message;
import comp5216.sydney.edu.au.learn.R;
import comp5216.sydney.edu.au.learn.util.FireBaseUtil;
import comp5216.sydney.edu.au.learn.util.NetworkUtils;
import comp5216.sydney.edu.au.learn.viewAdapter.ChatAdapter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MyBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private ImageButton expandButton;
    private boolean isExpanded = false;
    private final int fixHeight = 440;

    private EditText myEditText;
    private ImageButton senToGptBtn;
    private String userQuestion;
    private String userId;
    private String pdfName;
    private String pdfContentSummary;

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;


    public static MyBottomSheetDialogFragment newInstance() {
        return new MyBottomSheetDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.bottom_sheet_layout, container, false);

        expandButton = view.findViewById(R.id.expandButton);

        senToGptBtn = view.findViewById(R.id.senToGptBtn);
        senToGptBtn.setOnClickListener(this::askQuestionBaseOnSummary);

        myEditText = view.findViewById(R.id.myEditText);


        expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameLayout bottomSheet = getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);
                if (bottomSheet!=null){
                    ViewGroup.LayoutParams params = bottomSheet.getLayoutParams();
                    if (!isExpanded) {

                        params.height = convertDpToPx(fixHeight + 340);
                        isExpanded = true;
                    }else {
                        // 恢复到初始高度
                        params.height =convertDpToPx(fixHeight);
                        isExpanded = false;
                    }

                    // 应用新的高度
                    bottomSheet.setLayoutParams(params);

                }


            }
        });


        // Ensure the dialog does not dim the background
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }


        // 获取传递的参数
        Bundle args = getArguments();
        if (args != null) {
            userId = args.getString("userId");
            pdfName = args.getString("pdfName");
            pdfContentSummary = args.getString("pdfContentSummary");
        }

        // get chat history
        getChatHistory();

        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        // 创建并设置RecyclerView的LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        chatRecyclerView.setLayoutManager(layoutManager);

        // 创建并设置RecyclerView的Adapter
        chatAdapter = new ChatAdapter(getContext(),new ArrayList<Message>());
        chatRecyclerView.setAdapter(chatAdapter);

        // get offer data
        return view;
    }

    private void getChatHistory() {

        FireBaseUtil.retrievePdfQaAndPopulateChat(userId, pdfName, new FireBaseUtil.FirestoreRetrieveCallback() {
            @Override
            public void onSuccess(List<Message> chatData) {

                 chatAdapter.setMessages(chatData);
                 chatAdapter.notifyDataSetChanged();

                for (Message msg : chatData) {
                    Log.d("CHAT_DATA", msg.getContent() + " | Type: " + msg.getType().toString());
                }
            }

            @Override
            public void onFailure(Exception e) {
                // 处理错误或通知用户
                Log.e("CHAT_DATA", "Failed to retrieve data.", e);
            }
        });

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);


        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
                FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);

                if (bottomSheet != null) {
                    BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                    behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);

                    // 设置固定高度
                    int desiredHeight = convertDpToPx(fixHeight);

                    bottomSheet.getLayoutParams().height = desiredHeight;

                    bottomSheet.requestLayout();
                }

            }
        });
        return dialog;
    }

    private int convertDpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }


    private void askQuestionBaseOnSummary(View view) {
        if (myEditText.getText()!=null){

            userQuestion = myEditText.getText().toString();


            // set send message
            Message newSentMessage = new Message(userQuestion, Message.MessageType.SENT);
            chatAdapter.addMessage(newSentMessage);
            chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1); // 滚动到最新的消息


            // set received message
            Message PreviewMessage = new Message("I'm editing the answer, please wait...", Message.MessageType.PREVIEW);
            chatAdapter.addMessage(PreviewMessage);
            chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1); // 滚动到最新的消息



            StringBuilder requestText = new StringBuilder();

            requestText.append("Following is the content of my university lecture material : [ ");


            requestText.append(pdfContentSummary);

            requestText.append("]. ");

            requestText.append(" Using all this information as your knowledge source, I want you to answer the following question: ");

            requestText.append(userQuestion);
//            requestText.append(". You must always return in html format.");

            myEditText.setText("");

            String requestBody = "{\"messages\": [{\"role\": \"system\", \"content\": \"You are a helpful assistant.\"}, {\"role\": \"system\", \"content\": \"[]\"}, {\"role\": \"user\", \"content\": \"" + requestText + "\"}], \"model\": \"gpt-3.5-turbo\"}";
            NetworkUtils.postJsonRequest(requestBody, new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    handleResponseAnswer(response);
                }

                @Override
                public void onFailure(Call call, IOException e) {

                    handleFailure(e);
                }
            });


        }
    }


    private void handleResponseAnswer(Response response) throws IOException {

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
                        // run this in the main tread

                        // delete preview message
                        chatAdapter.deletePreviewMessage();

                        // set received message
                        Message newSentMessage = new Message(content, Message.MessageType.RECEIVED);
                        chatAdapter.addMessage(newSentMessage);
                        chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1); // 滚动到最新的消息


                        // Get the original file name
                        if (pdfName !=null){
                            saveQuestionAnswerToFirebase(pdfName, userQuestion, content);
                        }

                    }
                });
            }
        } else {

        }
    }


    private void saveQuestionAnswerToFirebase(String pdfFileName, String userQuestion, String gptAnswer){

        FireBaseUtil.addQuestionAndAnswerToPdf(userId, pdfFileName, userQuestion, gptAnswer,
                new FireBaseUtil.FirestoreUploadCallback() {
                    @Override
                    public void onSuccess() {
                        // Handle successful addition of questions and answers to Firestore
//                        Snackbar.make(rootView, "Question and Answer has been saved", Snackbar.LENGTH_LONG)
//                                .setAction("Action", null).show();
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        // Handle failure to add questions and answers to Firestore
                        Log.d("system error", String.valueOf(exception));

                    }
                }
        );

    }


    private void handleFailure(IOException e) {
        Log.e(TAG, "Exception: " + e);
    }
}

