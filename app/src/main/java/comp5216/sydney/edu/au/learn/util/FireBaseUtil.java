package comp5216.sydney.edu.au.learn.util;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import comp5216.sydney.edu.au.learn.Common.Message;
import comp5216.sydney.edu.au.learn.Common.lectureHistoryDTO;

public class FireBaseUtil {

    public static List<String> getUserEmailHistory(String userId){
        List<String> resultList = new ArrayList<>();
        // get FirebaseFirestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // create a query for search
        Query query = db.collection("user").whereEqualTo("id", userId);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null) {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            // 由于 userId 应该是唯一的，只有一个文档匹配
                            List<String> emailHistory = (List<String>) document.get("emailHistory");

                            if (emailHistory!=null){
                                resultList.addAll(emailHistory);
                            }

                            Log.d(TAG, "History:"+ emailHistory);

                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "Query failed with ", task.getException());
                }
            }
        });

        return resultList;
    }


    public interface EmailHistoryInsertionCallback {
        void onInsertionCompleted(boolean success);
    }

    public static void insertEmailHistory(String userId, String unescapedHtml, EmailHistoryInsertionCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("user").whereEqualTo("id", userId);

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        DocumentReference docRef = document.getReference();
                        String newEmailHistoryEntry = unescapedHtml;

                        docRef.update(
                                "emailHistory", FieldValue.arrayUnion(newEmailHistoryEntry)
                        ).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully updated!");
                                callback.onInsertionCompleted(true);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error updating document", e);
                                callback.onInsertionCompleted(false);
                            }
                        });
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                    callback.onInsertionCompleted(false);
                }
            }
        });
    }

    // insert new user to database callback
    public interface UserInsertionCallback {
        void onInsertionCompleted(boolean success);
    }

    public static void insertNewUser(String userId, String email, String displayName, UserInsertionCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> newUser = new HashMap<>();
        newUser.put("id", userId);
        newUser.put("userEmail", email);
        newUser.put("name", displayName);

        db.collection("user").document(userId).set(newUser)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User data added successfully");
                    callback.onInsertionCompleted(true);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding user data", e);
                    callback.onInsertionCompleted(false);
                });
    }


    // getTimeStamp call back
    public interface FirebaseCallback {
        void onCallback(List<Long> timestamps);
    }


    public static void getAllMarkedDate(String userId, final FirebaseCallback callback) {

        // 1. 初始化Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 2. 定位特定用户
        DocumentReference userDocRef = db.collection("user").document(userId);

        // 3. 获取用户数据并从中提取时间标记
        userDocRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        List<Long> resultList = new ArrayList<>();
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // 获取timeSchedule字段
                                Map<String, Object> timeScheduleMap = (Map<String, Object>) document.get("timeScadule");
                                if (timeScheduleMap != null) {
                                    // 输出所有的时间戳
                                    for (String timestamp : timeScheduleMap.keySet()) {
                                        resultList.add(Long.parseLong(timestamp.trim()));
                                    }
                                }
                            } else {
                                Log.d("Firestore", "No such document");
                            }
                        } else {
                            Log.d("Firestore", "get failed with ", task.getException());
                        }

                        // 使用回调返回结果
                        callback.onCallback(resultList);
                    }
                });
    }


    public interface FirebaseListCallback {
        void onCallback(List<String> dataList);
    }

    public static void getTimeScheduleData(String userId, String key, final FirebaseListCallback callback) {

        // 1. 初始化Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 2. 定位特定用户
        DocumentReference userDocRef = db.collection("user").document(userId);

        // 3. 获取用户数据并从中提取指定的数据
        userDocRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // 获取timeSchedule字段
                                Map<String, Object> timeScheduleMap = (Map<String, Object>) document.get("timeScadule");
                                if (timeScheduleMap != null) {
                                    // 获取指定key的值
                                    List<String> dataList = (List<String>) timeScheduleMap.get(key);
                                    // 使用回调返回结果
                                    callback.onCallback(dataList);
                                }
                            } else {
                                Log.d("Firestore", "No such document");
                            }
                        } else {
                            Log.d("Firestore", "get failed with ", task.getException());
                        }
                    }
                });
        }




    public interface FirebaseUpdateCallback {
        void onSuccess();
        void onFailure(Exception e);
    }



    public static void updateOrInsertTimestamp(String userId, String timestamp, List<String> updatedList, final FirebaseUpdateCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDocRef = db.collection("user").document(userId);

        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        Map<String, Object> timeScheduleMap = (Map<String, Object>) document.get("timeScadule");

                        // Check if timestamp key already exists
                        if (timeScheduleMap == null) {
                            timeScheduleMap = new HashMap<>();
                        }

                        // Add or replace the current timestamp key with the updated list
                        timeScheduleMap.put(timestamp, updatedList);

                        // Update the document with the new timeScheduleMap
                        userDocRef.update("timeScadule", timeScheduleMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    callback.onSuccess();
                                } else {
                                    callback.onFailure(task.getException());
                                }
                            }
                        });
                    }
                } else {
                    callback.onFailure(task.getException());
                }
            }
        });
    }


    public interface FirebaseFetchCallback {
        void onSuccess(String formattedData);
        void onFailure(Exception e);
    }


    public static void fetchAndFormatSchedule(String userId, final FirebaseFetchCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDocRef = db.collection("user").document(userId);

        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        Map<String, Object> timeScheduleMap = (Map<String, Object>) document.get("timeScadule");

                        if (timeScheduleMap != null) {
                            StringBuilder formattedData = new StringBuilder();

                            for (Map.Entry<String, Object> entry : timeScheduleMap.entrySet()) {
                                // Convert timestamp to "year-month-day" format
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                String formattedDate = sdf.format(new Date(Long.parseLong(entry.getKey().trim())));

                                // Convert the list to a comma-separated string
                                List<String> activitiesList = (List<String>) entry.getValue();
                                String activities = String.join(", ", activitiesList);

                                formattedData.append(formattedDate).append(": ").append(activities).append("; ");
                            }

                            callback.onSuccess(formattedData.toString());
                        }
                    } else {
                        callback.onFailure(new Exception("Document does not exist"));
                    }
                } else {
                    callback.onFailure(task.getException());
                }
            }
        });
    }

    // upload to storage
    public interface FirebaseUploadCallback {
        void onSuccess(String downloadUrl);
        void onFailure(Exception exception);
    }

    // upload to file storage database
    public interface FirestoreUploadCallback {
        void onSuccess();
        void onFailure(Exception exception);
    }


    public static void uploadFileToFirebase(String userId,
                                            String filePath,
                                            FirebaseUploadCallback storageCallback,
                                            FirestoreUploadCallback firestoreCallback) {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Create a reference to the userId folder
        StorageReference userFolderRef = storageRef.child(userId);

        // Get the original file name
        String fileName = new File(filePath).getName();

        Uri fileUri = Uri.fromFile(new File(filePath));

        // 获取原始文件名，不包括扩展名
        String baseName = FilenameUtils.getBaseName(fileName);


        // 使用这个新文件名创建一个引用
        StorageReference fileRef = userFolderRef.child(fileName);

        UploadTask uploadTask = fileRef.putFile(fileUri);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // 文件上传成功
                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        storageCallback.onSuccess(uri.toString());

                        // get FirebaseFirestore instance
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        // 把PDF的名字存入Firestore
                        DocumentReference userDoc = db.collection("user").document(userId);



                        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists() && document.get("pdfFiles." + baseName) != null) {
                                        // If pdfFiles.pdfName already exists, we don't need to do anything.
                                        firestoreCallback.onSuccess();
                                    } else {
                                        // If pdfFiles.pdfName doesn't exist, create it with an empty list.
                                        userDoc.update("pdfFiles." + baseName, new ArrayList<Map<String, String>>()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                firestoreCallback.onSuccess();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                firestoreCallback.onFailure(e);
                                            }
                                        });
                                    }
                                } else {
                                    firestoreCallback.onFailure(task.getException());
                                }
                            }
                        });






                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // 文件上传失败，处理错误
                storageCallback.onFailure(exception);
            }
        });
    }


    public static void addQuestionAndAnswerToPdf(String userId, String pdfFileName, String question, String answer, FirestoreUploadCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDoc = db.collection("user").document(userId);

        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    List<Map<String, String>> qaList = (List<Map<String, String>>) document.get("pdfFiles." + pdfFileName);

                    if (qaList == null) {
                        qaList = new ArrayList<>();
                    }

                    // Create or update the map
                    Map<String, String> qaMap = new HashMap<>();
                    qaMap.put(question, answer);
                    qaList.add(qaMap);

                    // Update the array in Firestore
                    userDoc.update("pdfFiles." + pdfFileName, qaList).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            callback.onSuccess();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            callback.onFailure(e);
                        }
                    });
                } else {
                    callback.onFailure(task.getException());
                }
            }
        });
    }



    public static void uploadPDFThumbnailsToFirebase(String userId,
                                            String filePath,
                                            FirebaseUploadCallback storageCallback) {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Create a reference to the userId folder
        StorageReference userFolderRef = storageRef.child(userId);

        // Get the original file name
        String fileName = new File(filePath).getName();

        Uri fileUri = Uri.fromFile(new File(filePath));

//        // 获取原始文件名，不包括扩展名
//        String baseName = FilenameUtils.getBaseName(fileName);

        // 获取文件的扩展名
//        String extension = FilenameUtils.getExtension(fileName);

//        // 创建一个新的文件名，将时间戳添加到原始文件名中，并保留扩展名
//        String newFileName = baseName + "_" + System.currentTimeMillis() + "." + extension;


        // 使用这个新文件名创建一个引用
        StorageReference fileRef = userFolderRef.child(fileName);

        UploadTask uploadTask = fileRef.putFile(fileUri);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // 文件上传成功
                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        storageCallback.onSuccess(uri.toString());

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // 文件上传失败，处理错误
                storageCallback.onFailure(exception);
            }
        });
    }


    public interface FirebaseFilesCallback {
        void onSuccess(List<lectureHistoryDTO> fileItems);

        void onFailure(Exception exception);
    }


    public static void fetchAllPngFilesFromUserFolder(String userId, FirebaseFilesCallback callback) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Reference to the user's folder
        StorageReference userFolderRef = storageRef.child(userId);

        userFolderRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        List<lectureHistoryDTO> fileItems = new ArrayList<>();

                        List<StorageReference> pngRefs = new ArrayList<>();

                        // Filter for PNG files
                        for (StorageReference item : listResult.getItems()) {
                            if (item.getName().endsWith(".png")) {
                                pngRefs.add(item);
                            }
                        }

                        if (pngRefs.isEmpty()) {
                            callback.onSuccess(fileItems);
                            return;
                        }

                        for (StorageReference pngRef : pngRefs) {
                            pngRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    String fileName = pngRef.getName();

                                    // get pdf title
                                    String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
                                    fileItems.add(new lectureHistoryDTO(uri.toString(),fileNameWithoutExtension));

                                    if (fileItems.size() == pngRefs.size()) {
                                        callback.onSuccess(fileItems);
                                    }
                                }

                            }).addOnFailureListener(callback::onFailure);
                        }
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }




    // 回调接口
    public interface FirebaseDownloadCallback {
        void onSuccess(File localFile);
        void onFailure(Exception exception);
    }

    public static void getLocalPdfByUserIdAndFileName(Context context, String userId, String pdfFileName, FirebaseDownloadCallback callback) {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // 确保文件名以.pdf结尾
        if (!pdfFileName.endsWith(".pdf")) {
            pdfFileName = pdfFileName + ".pdf";
        }

        // 获取指向userId文件夹的引用
        StorageReference userFolderRef = storageRef.child(userId);

        // 获取指定PDF文件的引用
        StorageReference fileRef = userFolderRef.child(pdfFileName);

        // 创建一个本地临时文件
        File localFile = new File(context.getCacheDir(), pdfFileName);

        // 使用Firebase SDK的getFile方法下载文件
        fileRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                callback.onSuccess(localFile);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                callback.onFailure(exception);
            }
        });
    }



    public interface FirestoreRetrieveCallback {
        void onSuccess(List<Message> chatData);

        void onFailure(Exception e);
    }


    public static void retrievePdfQaAndPopulateChat(String userId, String pdfFileName, FirestoreRetrieveCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDoc = db.collection("user").document(userId);

        userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    List<Map<String, String>> qaList = (List<Map<String, String>>) document.get("pdfFiles." + pdfFileName);

                    List<Message> chatData = new ArrayList<>();

                    if (qaList != null) {
                        for (Map<String, String> qaMap : qaList) {
                            for (Map.Entry<String, String> entry : qaMap.entrySet()) {
                                chatData.add(new Message(entry.getKey(), Message.MessageType.SENT));
                                chatData.add(new Message(entry.getValue(), Message.MessageType.RECEIVED));
                            }
                        }
                    }

                    callback.onSuccess(chatData);
                } else {
                    callback.onFailure(task.getException());
                }
            }
        });
    }


}
