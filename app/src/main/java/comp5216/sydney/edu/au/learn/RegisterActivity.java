package comp5216.sydney.edu.au.learn;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    public EditText email;

    public EditText password;

    private Button registerBtn;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // 找到控件
        email = findViewById(R.id.etEmail);
        password = findViewById(R.id.etPassword);
        registerBtn = findViewById(R.id.registerBtn);

        registerBtn.setOnClickListener(this::registerClick);
        mAuth = FirebaseAuth.getInstance();

    }

    private void registerClick(View view) {
        String emailUse = email.getText().toString();
        String passwordUse = password.getText().toString();

        if (emailUse.isEmpty() || passwordUse.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "please fill the full information", Toast.LENGTH_SHORT).show();
        } else {
            registerUser(emailUse, passwordUse);
        }
    }


    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();
                        sendVerificationEmail(user); // send verify email

                    } else {
                        // register failed
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(RegisterActivity.this, "This email has been registered", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "register failed，please trt later", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void sendVerificationEmail(FirebaseUser user) {
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                            builder.setTitle("Successful !!")
                                    .setMessage("The verified Email has been send")
                                    .setPositiveButton("Back", (dialogInterface, i) -> {

                                    });

                            builder.create().show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "can`t send email, please try later", Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }


}