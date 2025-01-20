package com.lorenzorogers.talkative.conversation;

import java.util.ArrayList;

public record Conversation(int id, ArrayList<Message> messages) {

    public record Message(String content, MessageType type) {
    }

    public enum MessageType {
        QUERY("You"),
        RESPONSE("TalkativeAI");

        private final String type;

        MessageType(String type) {
            this.type = type;
        }

        public String getType() {
            return this.type;
        }
    }
}

