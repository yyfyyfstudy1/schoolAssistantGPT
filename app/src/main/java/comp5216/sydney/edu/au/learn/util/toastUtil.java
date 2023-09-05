package comp5216.sydney.edu.au.learn.util;

import android.content.Context;
import android.widget.Toast;

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
}
