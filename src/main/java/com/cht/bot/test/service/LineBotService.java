package com.cht.bot.test.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cht.bot.test.util.HttpUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import okhttp3.Response;

@Service
public class LineBotService {

    @Value("${line.bot.channel-token}")
    private String channelToken;

    @Value("${line.bot.channel-secret}")
    private String clientSecret;

    @Value("${client.id}")
    private String clientId;

    @Autowired
    private HttpUtils httputils;

    @Autowired
    private ObjectMapper mapper;

    /**
     * 若為 short-lived channel access token，則需定期 (30天) 取得新 token.
     */
    public String requestChannelToken() throws IOException {
        final String url = "https://api.line.me/v2/oauth/accessToken";

        Map<String, Object> keyValues = new HashMap<>();
        keyValues.put("grant_type", "client_credentials");
        keyValues.put("client_id", clientId);
        keyValues.put("client_secret", clientSecret);

        Response response = httputils.post(url, keyValues);
        JsonNode responseBody = mapper.readValue(response.body().string(), ObjectNode.class);
        channelToken = responseBody.get("access_token").asText();
        long expiresIn = responseBody.get("expires_in").asLong();

        return channelToken;
    }

    public String getGroupName(String groupId) throws IOException {
        String url = "https://api.line.me/v2/bot/group/" + groupId + "/summary";

        Response response = httputils.get(url, null, channelToken);
        JsonNode responseBody = mapper.readValue(response.body().string(), ObjectNode.class);
        String groupName = responseBody.get("groupName").asText();

        return groupName;
    }
}
