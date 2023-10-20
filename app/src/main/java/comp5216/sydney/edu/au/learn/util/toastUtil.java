package comp5216.sydney.edu.au.learn.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.androidadvance.topsnackbar.TSnackbar;

import comp5216.sydney.edu.au.learn.R;

public class toastUtil {
    public static Toast mToast;
    public static void showToast(Context context, String msg){
        if(mToast == null){
            mToast =  Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        }else {
            mToast.setText(msg);
        }
        mToast.show();
    }


    @SuppressLint({"ResourceAsColor", "WrongConstant"})
    public static void topSnackBar(View view, String message){
        TSnackbar snackbar = TSnackbar
                .make(view, message, TSnackbar.LENGTH_LONG);

        int customDuration = 3000; // 3 seconds in milliseconds
        snackbar.setDuration(customDuration);
        snackbar.setActionTextColor(ContextCompat.getColor(view.getContext(), R.color.textColorPrimary));
        snackbar.setIconRight(R.drawable.uniflow_logo, 48); //Resize to bigger dp
        snackbar.setIconPadding(8);
        snackbar.setMaxWidth(3000); //if you want fullsize on tablets
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        TextView textView = (TextView) snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(20);
        snackbar.show();
    }
}
