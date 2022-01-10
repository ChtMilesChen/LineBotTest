package com.cht.bot.test;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;

import com.cht.bot.test.service.LineBotService;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.ConfirmTemplate;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

@LineMessageHandler
public class LineBotController {

    @Autowired
    private LineMessagingClient lineMessagingClient;

    @Autowired
    private LineBotService botService;

    @EventMapping
    public Message handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
        String groupId = event.getSource().getSenderId();
        final String originalMessageText = event.getMessage().getText();

        try {
            switch (originalMessageText) {
            case "renew":       // 重新取得 channelToken
                renewChannelToken();
                pushTextMessage(groupId, "ChannelToken renew!");
                break;
            case "push":        // 群組推播訊息
                pushTextMessage(groupId, "Group message!");
                break;
            case "ask":         // 群組推播特殊的詢問式訊息
                pushAskMessage(groupId);
                break;
            case "groupname":
                String groupName = botService.getGroupName(groupId);
                pushTextMessage(groupId, groupName);
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String responseMessage = "You say: " + originalMessageText;
        return new TextMessage(responseMessage);
    }

    private void renewChannelToken() throws IOException {
        String channelToken = botService.requestChannelToken();

        lineMessagingClient = LineMessagingClient
                .builder(channelToken)
                .build();       // 目前以此方式更新 token 會失敗
    }

    private CompletableFuture<BotApiResponse> pushTextMessage(String groupId, String textMessage) {
        return lineMessagingClient.pushMessage(new PushMessage(groupId, new TextMessage(textMessage)));
    }

    private CompletableFuture<BotApiResponse> pushAskMessage(String groupId) {
        String altText = "altText";
        String question = "Do you like this BOT?";
        String act1 = "yes";
        String act2 = "no";

        return lineMessagingClient.pushMessage(new PushMessage(groupId, new TemplateMessage(altText, new ConfirmTemplate(question, new MessageAction(act1, act1), new MessageAction(act2, act2)))));
    }
}
