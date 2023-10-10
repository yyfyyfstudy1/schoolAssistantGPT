package comp5216.sydney.edu.au.learn.util;

import static android.content.ContentValues.TAG;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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



}
