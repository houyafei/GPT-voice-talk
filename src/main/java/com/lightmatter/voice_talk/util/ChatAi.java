package com.lightmatter.voice_talk.util;

import android.util.Log;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.lightmatter.voice_talk.dto.ChatRequestDto;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatAi {

    private static final String TAG = "ChatAi";
    private static final String url = "http://106.55.69.28:12345/chat";

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60,TimeUnit.SECONDS)
            .build();

    private static final String TRY_AGAIN = "你说的我好想没有听清楚哎 ^_^，再试一次吧";

    public static String chatResponse(String reqContent) {
        if (reqContent == null || reqContent.length() < 4) {
            return TRY_AGAIN;
        }
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, JSON.toJSONString(new ChatRequestDto(reqContent, 1)));
        Request request = new Request
                .Builder()
                .url(url)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = null;
        try {
            response = HTTP_CLIENT

                    .newCall(request).execute();
            if (response.code() == 200) {
                String rspBody = response.body().string();
                String result = JSONObject.parseObject(rspBody).getString("content");
                if (result == null || result.length() <= 0) {
                    Log.e(TAG, "getAccessToken: response.body() " + result);
                    return TRY_AGAIN;
                }
                return result;
            } else {
                return TRY_AGAIN;
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "getAccessToken: response.body() " + e.getStackTrace());
            return TRY_AGAIN;
        }
    }
}
