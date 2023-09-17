package comp5216.sydney.edu.au.learn.util;

import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FormValidator {

    public static boolean validateTextInputLayout(TextInputLayout inputLayout, String value, String errorMessage) {
        if (value.isEmpty()) {
            inputLayout.setError(errorMessage);
            return false;
        } else {
            inputLayout.setError(null);
            return true;
        }
    }

    public static boolean validateEmail(TextInputLayout inputLayout, String email) {
        if (!isValidEmail(email)) {
            inputLayout.setError("Invalid email address");
            return false;
        } else {
            inputLayout.setError(null);
            return true;
        }
    }



    public static boolean validatePassword(TextInputLayout inputLayout, String password) {
        if (!isStrongPassword(password)) {
            inputLayout.setError("password is invalid");
            return false;
        } else {
            inputLayout.setError(null);
            return true;
        }
    }


    public static boolean validateRepeatPassword(TextInputLayout inputLayout, String password, String password2) {
        if (!password.equals(password2)) {
            inputLayout.setError("two password is not same");
            return false;
        } else {
            inputLayout.setError(null);
            return true;
        }
    }

    private static boolean isValidEmail(String email) {
        // 邮箱校验逻辑
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private static boolean isStrongPassword(String password) {
        // 密码强度校验逻辑
        if (password == null || password.isEmpty()) {
            return false;
        }

        // 正则表达式匹配至少一个字母和一个数字
        String pattern = "^(?=.*[a-zA-Z])(?=.*\\d).+$";

        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(password);

        return matcher.matches();
    }
}

