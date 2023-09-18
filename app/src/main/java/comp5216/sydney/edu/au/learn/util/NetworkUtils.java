package comp5216.sydney.edu.au.learn.util;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class NetworkUtils {

    private static final String TAG = "NetworkUtils";

    public static final String apiURL = "https://api.openai.com/v1/chat/completions";
    public static final String apiKey = "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";
    // 设置超时时间
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public static void postJsonRequest(String JsonText, Callback callback) {


        MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON_MEDIA_TYPE, JsonText);
        String useUrl = apiURL;
        Request request = new Request.Builder()
                .url(useUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void getRequest(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void getWithParamsRequest( Object object, String url, Callback callback) {

        HttpUrl.Builder urlBuilder = HttpUrl.parse(apiURL + url).newBuilder();
        // 拼接参数
        Map<String, String> queryParams = convertObjectToMap(object);
        // 添加查询参数
        if (queryParams != null) {
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }

        String useUrl = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(useUrl)
                .build();

        client.newCall(request).enqueue(callback);
    }


    public static void postRequest(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(null, new byte[0]))
                .build();

        client.newCall(request).enqueue(callback);
    }

    private static Map<String, String> convertObjectToMap(Object obj) {
        if (obj == null) {
            return null;
        }

        Map<String, String> paramMap = new HashMap<>();
        Field[] fields = obj.getClass().getDeclaredFields();

        try {
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Object fieldValue = field.get(obj);
                if (fieldValue != null) {
                    paramMap.put(fieldName, fieldValue.toString());
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return paramMap;
    }
}
