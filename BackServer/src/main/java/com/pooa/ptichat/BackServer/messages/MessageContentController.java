package com.pooa.ptichat.BackServer.messages;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class MessageContentController {
    @MessageMapping("/messages")
    @SendTo("/listen/messages")
    public MessageContent messageContent(Message message) throws Exception {
        Thread.sleep(1000); // simulated delay
        return new MessageContent("Hello");
    }
}
