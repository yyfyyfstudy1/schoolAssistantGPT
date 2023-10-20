package comp5216.sydney.edu.au.learn.fragment;


import static android.content.ContentValues.TAG;

import static comp5216.sydney.edu.au.learn.util.FileUtils.saveBitmapAsPNG;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


import comp5216.sydney.edu.au.learn.Common.lectureHistoryDTO;
import comp5216.sydney.edu.au.learn.R;
import comp5216.sydney.edu.au.learn.util.FileUtils;
import comp5216.sydney.edu.au.learn.util.FireBaseUtil;
import comp5216.sydney.edu.au.learn.util.toastUtil;
import comp5216.sydney.edu.au.learn.viewAdapter.LectureHistoryListAdapter;
import es.voghdev.pdfviewpager.library.PDFViewPager;
import es.voghdev.pdfviewpager.library.adapter.PDFPagerAdapter;


public class LectureFragment extends Fragment {
    private View rootView;
    private static final int REQUEST_PERMISSIONS = 1;
    private static final int PICK_PDF_FILE = 2;
    private String userId;
    PDFPagerAdapter pdfPagerAdapter;

    private RecyclerView lectureHistoryRecyclerView;
    private ArrayList<lectureHistoryDTO> lectureHistoryList;

    private LectureHistoryListAdapter lectureHistoryListAdapter;

    private ProgressBar progressBar;

    private LectureResponseFragment lectureResponseFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_lecture, container, false);

        Bundle arguments = getArguments();
        if (arguments != null) {
            userId = arguments.getString("userId");
            Log.d(TAG, "onCreateView: "+ userId);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSIONS);
        }

        Button uploadButton = view.findViewById(R.id.uploadButton);
        lectureHistoryRecyclerView = view.findViewById(R.id.lectureHistory);

        progressBar = view.findViewById(R.id.progressBar);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        lectureHistoryRecyclerView.setLayoutManager(layoutManager);

        lectureHistoryList = new ArrayList<>();

        // create and set the adapter
        lectureHistoryListAdapter = new LectureHistoryListAdapter(getContext(),lectureHistoryList, clickListener);
        lectureHistoryRecyclerView.setAdapter(lectureHistoryListAdapter);

        showLoading(true);  // show loading bar
        // get lecture history list from firebase
        FireBaseUtil.fetchAllPngFilesFromUserFolder(userId, new FireBaseUtil.FirebaseFilesCallback() {
            @Override
            public void onSuccess(List<lectureHistoryDTO> fileItems) {
                showLoading(false);
                lectureHistoryList.addAll(fileItems);
                lectureHistoryListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception exception) {
                showLoading(false);
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickPdfFile();
            }
        });

    }

    // request permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // granted
                toastUtil.showToast(getContext(),"Permission granted");
            } else {
                // denied
//                toastUtil.showToast(getContext(),"Permission denied");
            }
        }
    }

    private void pickPdfFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        startActivityForResult(intent, PICK_PDF_FILE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF_FILE && resultCode == getActivity().RESULT_OK) {
            Uri selectedPdfUri = data.getData();
            try {
                uploadPdfFile(selectedPdfUri);  // 定义这个方法来处理文件上传
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadPdfFile(Uri fileUri) throws IOException {

        File tempFile = FileUtils.getFileFromUri(getContext(), fileUri);
        String filePath = tempFile.getAbsolutePath();


        // 使用 PDFViewPager 生成缩略图
        // 创建 PDFPagerAdapter 实例
        pdfPagerAdapter = new PDFPagerAdapter(getContext(), filePath);
        PDFViewPager pdfViewPager = new PDFViewPager(getContext(), filePath);

        View pdfView = (View) pdfPagerAdapter.instantiateItem(pdfViewPager, 0);


        int width = 1150;
        int height = 800;

        pdfView.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        pdfView.layout(0, 0, pdfView.getMeasuredWidth(), pdfView.getMeasuredHeight());


        Bitmap thumbnail = getBitmapFromView(pdfView);


        // Get the original file name
        String fileName = new File(filePath).getName();
        String baseName = FilenameUtils.getBaseName(fileName);


        File outputDirectory = Objects.requireNonNull(getActivity()).getExternalFilesDir(null);
        File pngFile = new File(outputDirectory, baseName+".png");

        try {
            saveBitmapAsPNG(thumbnail, pngFile);

            String absolutePath = pngFile.getAbsolutePath();
            Log.d("absPath", absolutePath);


            // upload thumbnail to firebase storage
            FireBaseUtil.uploadPDFThumbnailsToFirebase(userId, absolutePath, new FireBaseUtil.FirebaseUploadCallback() {
                @Override
                public void onSuccess(String downloadUrl) {
                    Log.d("fireStore", "Thumbnail uploaded successfully");
                }

                @Override
                public void onFailure(Exception exception) {
                    Log.d("fireStore", "Failed to upload thumbnail");
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }


        // dump to gpt response pdf page
        dumpToLectureResponse(filePath);



    }



    private void dumpToLectureResponse(String filePath){
        // dump to lecture response fragment
        LectureResponseFragment lectureResponseFragment = new LectureResponseFragment();
        Bundle args = new Bundle();
        // send file path
        args.putString("filePath", filePath);
        args.putString("userId", userId);

        lectureResponseFragment.setArguments(args);
        // change fragment
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_container, lectureResponseFragment);
        // Add FragmentA to the back stack so the user can return to it
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();
    }

    // capture Bitmap from view
    public Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }


    private void showLoading(final boolean show) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        }
    }

    LectureHistoryListAdapter.OnItemClickListener clickListener = new LectureHistoryListAdapter.OnItemClickListener() {

        @Override
        public void onClick(int pos, String pdfName) {
            // prepare parameter
            Bundle args = new Bundle();
            args.putString("pdfName", pdfName);
            args.putString("userId", userId);

            if (lectureResponseFragment == null){
                lectureResponseFragment = new LectureResponseFragment();
            }
            lectureResponseFragment.setArguments(args);

            // execute Fragment change
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fl_container, lectureResponseFragment);
            transaction.addToBackStack(null); // Add FragmentA to the back stack so the user can return to it
            transaction.commitAllowingStateLoss();
        }
    };




}
