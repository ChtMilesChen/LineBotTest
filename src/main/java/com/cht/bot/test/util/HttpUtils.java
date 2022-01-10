package com.cht.bot.test.util;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import okhttp3.*;

/**
 * 處理各類 HTTP 請求的模組.
 */
@Component
public class HttpUtils {

    /** Time Out 秒數 */
    private int timeout = 150;
    private OkHttpClient httpClient;

    /**
     * 設定連線參數.
     */
    public HttpUtils() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    /**
     * HTTP GET 請求 (需認證).
     *
     * @param url       請求網址
     * @param keyValues 請求參數
     * @param token     Bearer token
     * @return 回傳訊息
     * @throws IOException 請求失敗
     */
    public Response get(String url, Map<String, String> keyValues, String token) throws IOException {
        HttpUrl.Builder urlBuider = HttpUrl.parse(url).newBuilder();

        if (keyValues != null) {
            for(Map.Entry<String, String> param : keyValues.entrySet())
                urlBuider.addQueryParameter(param.getKey(), param.getValue());
        }

        Request.Builder builder = new Request.Builder()
                .url(urlBuider.build());

        return sendRequest(builder, token);
    }

    /**
     * HTTP POST 請求.
     *
     * @param url       請求網址
     * @param keyValues 請求參數
     * @return 回傳訊息
     * @throws IOException 請求失敗
     */
    public Response post(String url, Map<String, Object> keyValues) throws IOException {
        FormBody.Builder formBuilder = new FormBody.Builder();

        if (keyValues != null) {
            for (Map.Entry<String, Object> item : keyValues.entrySet())
                formBuilder.add(item.getKey(), item.getValue().toString());
        }

        RequestBody body = formBuilder.build();

        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(body);

        return sendRequest(builder, null);
    }

    /**
     * 送出請求.
     *
     * @param builder 欲送出的請求
     * @param token   Bearer token
     * @return 回傳訊息
     * @throws IOException 請求失敗
     */
    private Response sendRequest(Request.Builder builder, String token) throws IOException {
        if (token != null)
            builder.addHeader("Authorization", "Bearer " + token);

        Request request = builder.build();
        Response response = httpClient.newCall(request).execute();

        return response;
    }
}
