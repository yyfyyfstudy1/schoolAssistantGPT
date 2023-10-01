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
    public EditText passwordConfirm;

    private Button registerBtn;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // match the widgets
        email = findViewById(R.id.etEmail);
        password = findViewById(R.id.etPassword);
        passwordConfirm = findViewById(R.id.repeatPassword);
        registerBtn = findViewById(R.id.registerBtn);

        registerBtn.setOnClickListener(this::registerClick);
        mAuth = FirebaseAuth.getInstance();

    }

    // validate all fields on clicking register btn
    private void registerClick(View view) {
        String emailUse = email.getText().toString().trim();
        String passwordUse = password.getText().toString().trim();
        String passwordConfirmUse = passwordConfirm.getText().toString().trim();

        boolean isEmailEmpty = emailUse.isEmpty();
        boolean isPasswordEmpty = passwordUse.isEmpty();
        boolean isPasswordMatch = passwordUse.equals(passwordConfirmUse);

        if (isEmailEmpty) {
            Toast.makeText(RegisterActivity.this, "Email is required", Toast.LENGTH_SHORT).show();
        } else if (isPasswordEmpty) {
            Toast.makeText(RegisterActivity.this, "Password is required", Toast.LENGTH_SHORT).show();
        } else if (!isPasswordMatch) {
            Toast.makeText(RegisterActivity.this, "Password and confirmation do not match", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(RegisterActivity.this,
                                    "Email is already registered.\nPlease sign in or use a new address.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Registration failed. \nPlease try again later.",
                                    Toast.LENGTH_SHORT).show();
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
                            builder.setTitle("Account Created Successfully!")
                                    .setMessage("A verification email has been sent. Please verify before login.")
                                    .setPositiveButton("OK", (dialogInterface, i) -> {

                                    });

                            builder.create().show();
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Can't send verification email.\nPlease try again later.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }


}