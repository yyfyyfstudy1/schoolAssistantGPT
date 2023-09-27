package comp5216.sydney.edu.au.learn.fragment;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import java.io.File;
import java.io.IOException;


import comp5216.sydney.edu.au.learn.R;
import comp5216.sydney.edu.au.learn.util.FileUtils;
import comp5216.sydney.edu.au.learn.util.toastUtil;


public class LectureFragment extends Fragment {

    private static final int REQUEST_PERMISSIONS = 1;
    private static final int PICK_PDF_FILE = 2;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lecture, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 检查权限
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSIONS);
        }

        Button uploadButton = view.findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickPdfFile();
            }
        });

    }

    // 请求权限的回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限被授予
                toastUtil.showToast(getContext(),"Permission granted");
            } else {
                // 权限被拒绝
                toastUtil.showToast(getContext(),"Permission denied");
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
        // upload pdf file

        File tempFile = FileUtils.getFileFromUri(getContext(), fileUri);
        String filePath = tempFile.getAbsolutePath();

        // dump to lecture response fragment
        LectureResponseFragment lectureResponseFragment = new LectureResponseFragment();
        Bundle args = new Bundle();
         // send file path
        args.putString("filePath", filePath);

        lectureResponseFragment.setArguments(args);
        // change fragment
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_container, lectureResponseFragment);
        // Add FragmentA to the back stack so the user can return to it
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();

    }





}
