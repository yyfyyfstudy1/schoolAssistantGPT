package comp5216.sydney.edu.au.learn.util;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    @SuppressLint("Range")
    public static File getFileFromUri(Context context, Uri uri) throws IOException {
        ContentResolver contentResolver = context.getContentResolver();

        // Query the Uri to get the file name
        String fileName;
        try (Cursor cursor = contentResolver.query(uri, null, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            } else {
                throw new IOException("Failed to retrieve file name");
            }
        }

        InputStream inputStream = contentResolver.openInputStream(uri);
        if (inputStream == null) {
            throw new IOException("Unable to obtain input stream from URI");
        }

        // Use the file name to create a temporary file
        File tempFile = new File(context.getCacheDir(), fileName);
        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4 * 1024]; // buffer size
            int read;

            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }

            outputStream.flush();
        } finally {
            inputStream.close();
        }

        return tempFile;
    }


    public static void saveBitmapAsPNG(Bitmap bitmap, File outputFile) throws IOException {
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);  // 压缩为 PNG 格式, 100 表示最高质量
        } finally {
            if (outStream != null) {
                outStream.close();
            }
        }
    }

}


