package comp5216.sydney.edu.au.learn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.Objects;

import comp5216.sydney.edu.au.learn.util.toastUtil;

public class ForgetPasswordActivity extends AppCompatActivity {

    private TextInputEditText etUsername;
    private MaterialButton sendEmailBtn;
    private TextView greeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        greeting = findViewById(R.id.greeting);
        etUsername = findViewById(R.id.etUsername);
        sendEmailBtn = findViewById(R.id.sendEmailBtn);

        sendEmailBtn.setOnClickListener(this::sendEmail);
    }

    private void sendEmail(View view) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String emailAddress = Objects.requireNonNull(etUsername.getText()).toString();

        mAuth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            greeting.setText("Please check your Email");
                            toastUtil.topSnackBar(view, "Reset link has sent to your email");
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                toastUtil.topSnackBar(view, "This email address is not exist");
                            } catch (Exception e) {
                                toastUtil.topSnackBar(view, "server error occurred");
                            }
                        }
                    }
                });

    }
}