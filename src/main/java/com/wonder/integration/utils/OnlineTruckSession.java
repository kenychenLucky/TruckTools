package com.wonder.integration.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.nio.sctp.SendFailedNotification;
import com.wonder.integration.truck.impl.TruckSessionServics;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;



import static java.lang.Thread.sleep;

public class OnlineTruckSession extends TruckSessionServics {

    private static Logger logger = LoggerFactory.getLogger(OnlineTruckSession.class);

    public void OnlineTruckSession(String sessionId){
       super.setServicsCookie(sessionId);
    }


    public void setTruckSession(String id) {
        String web_base = "https://simulator.foodtruck-staging.com";
        String web_url = web_base + "/ajax/truck-session/" + id + "/online";
        String referer_url = web_base + "/truck-session/" + id;

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url(web_url)
                .method("PUT", body)
                .addHeader("authority", "simulator.foodtruck-staging.com")
                .addHeader("content-length", "0")
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36")
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .addHeader("origin", web_base)
                .addHeader("sec-fetch-site", "same-origin")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("referer", referer_url)
                .addHeader("accept-language", "en-US,en;q=0.9")
                .addHeader("cookie", super.COOKIE)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String jsonStr = response.body().string();
            logger.info("setTruckSession="+id +" OK" + jsonStr);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void TruckSessionOnline(int start,int end) {
        OnlineTruckSession onlineTruckSession = new OnlineTruckSession();
        for (int i = start; i <= end; i++) {
            onlineTruckSession.setTruckSession(String.valueOf(i));
            try {
                sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }




}
