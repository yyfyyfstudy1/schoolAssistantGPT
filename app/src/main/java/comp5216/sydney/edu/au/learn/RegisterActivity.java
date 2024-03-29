package comp5216.sydney.edu.au.learn;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

import comp5216.sydney.edu.au.learn.util.FireBaseUtil;

public class RegisterActivity extends AppCompatActivity {
    public TextInputEditText username;
    public TextInputEditText email;
    public TextInputEditText password;
    public TextInputEditText passwordConfirm;

    private Button registerBtn;
    private Button loginBtn;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // match the widgets
        username = findViewById(R.id.etName);
        email = findViewById(R.id.etEmail);
        password = findViewById(R.id.etPassword);
        passwordConfirm = findViewById(R.id.repeatPassword);
        registerBtn = findViewById(R.id.registerBtn);
        loginBtn = findViewById(R.id.loginBtn);
        registerBtn.setOnClickListener(this::registerClick);
        mAuth = FirebaseAuth.getInstance();
        loginBtn.setOnClickListener(this::toLoginClick);

    }

    private void toLoginClick(View view){
        // finish current activity so user can switch back to log in page
        finish();
    }

    // validate all fields on clicking register btn
    private void registerClick(View view) {
        String nameUse = username.getText().toString().trim();
        String emailUse = email.getText().toString().trim();
        String passwordUse = password.getText().toString().trim();
        String passwordConfirmUse = passwordConfirm.getText().toString().trim();

        boolean isNameEmpty = nameUse.isEmpty();
        boolean isEmailEmpty = emailUse.isEmpty();
        boolean isPasswordEmpty = passwordUse.isEmpty();
        boolean isPasswordMatch = passwordUse.equals(passwordConfirmUse);

        if (isNameEmpty) {
            Toast.makeText(RegisterActivity.this, "User name is required", Toast.LENGTH_SHORT).show();
        } else if (isEmailEmpty) {
            Toast.makeText(RegisterActivity.this, "Email is required", Toast.LENGTH_SHORT).show();
        } else if (isPasswordEmpty) {
            Toast.makeText(RegisterActivity.this, "Password is required", Toast.LENGTH_SHORT).show();
        } else if (!isPasswordMatch) {
            Toast.makeText(RegisterActivity.this, "Password and confirmation do not match", Toast.LENGTH_SHORT).show();
        } else {
            registerUser(emailUse, passwordUse, nameUse);
        }
    }

    private void registerUser(String email, String password, String displayName) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        // insert the user to firebase database
                        FireBaseUtil.insertNewUser(user.getUid(), email, displayName, success -> {
                            if (success) {
                                Log.d(TAG, "User data insertion was successful");
                            } else {
                                Log.d(TAG, "Failed to insert user data");
                            }
                        });

                        // Update the user's profile with the username
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(displayName)
                                .build();

                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        // Username updated successfully
                                        sendVerificationEmail(user); // send verify email
                                    } else {
                                        //username update fails
                                        Toast.makeText(RegisterActivity.this,
                                                "Failed to update username. \nPlease try again.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Exception exception = task.getException();
                        Log.e("UniFlow", "Registration failed", exception);
                        // register failed
                        if (exception instanceof FirebaseAuthWeakPasswordException) {
                            // Handle weak password exception
                            Toast.makeText(RegisterActivity.this,
                                    "Password is too weak. Please use a password with at least 6 characters.",
                                    Toast.LENGTH_SHORT).show();
                        } else if (exception instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(RegisterActivity.this,
                                    "Email is already registered. Please sign in or use a new address.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Registration failed. Please try again later.",
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
                                        finish();
                                    });

                            builder.create().show();
                            // TODO: should return to log in page after success registration
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Can't send verification email.\nPlease try again later.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }


}