package me.stella.discord;

import org.json.simple.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WebhookService {

    public static void sendWebhook(String webhook, JSONObject object) {
        try {
            URL webhookURL = new URL(webhook);
            HttpURLConnection webhookConnection = (HttpURLConnection) webhookURL.openConnection();
            webhookConnection.setRequestMethod("POST");
            webhookConnection.setRequestProperty("Content-Type", "application/json; utf-8");
            webhookConnection.setRequestProperty("Accept", "application/json");
            webhookConnection.setDoOutput(true);
            OutputStream webhookStream = webhookConnection.getOutputStream();
            byte[] payloadBytes = object.toJSONString().getBytes(StandardCharsets.UTF_8);
            webhookStream.write(payloadBytes, 0, payloadBytes.length);
            webhookStream.flush(); webhookStream.close();
            webhookConnection.getResponseCode(); webhookConnection.disconnect();
        } catch(Throwable t) {}
    }

}
