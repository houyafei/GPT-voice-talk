package com.lightmatter.voice_talk.util;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;


public class BaiduAi {

    private static final String TAG = "Baidu";
    private static final String API_KEY = "";
    private static final String SECRET_KEY = "";

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

    private static final String UID = "123231231";

    /**
     * 从用户的AK，SK生成鉴权签名（Access Token）
     *
     * @return 鉴权签名（Access Token）
     * @throws IOException IO异常
     */
    private static String getAccessToken() throws IOException {
        if (API_KEY.isEmpty()){
            Log.e(TAG, "API-Key 是 空");
            return "";
        }
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" + API_KEY
                + "&client_secret=" + SECRET_KEY);
        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        String rspBody = response.body().string();
        return JSONObject.parseObject(rspBody).getString("access_token");
    }

    public static String postRaw(String filePath) throws Exception {
        MediaType mediaType = MediaType.parse("audio/pcm;rate=16000");
        File file = new File(filePath);
        Log.d(TAG, "postRaw: "+file.exists());
        if (!file.exists()){
            return "No Recode file";
        }
        RequestBody requestBody = RequestBody.create(mediaType, file);


        Request request = new Request.Builder()
                .url("https://vop.baidu.com/server_api?dev_pid=1537&cuid="+UID+"&token=" + getAccessToken())
                .post(requestBody)
                .build();


        Response response = HTTP_CLIENT.newCall(request).execute();

        String result = response.body().string();
        Log.e(TAG, "postRaw: " + result);
        return JSONObject.parseObject(result).getJSONArray("result").getString(0);
    }

}
