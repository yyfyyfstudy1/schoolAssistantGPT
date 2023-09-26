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
import java.util.ArrayList;
import java.util.List;

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

                            assert emailHistory != null;
                            resultList.addAll(emailHistory);

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

}
