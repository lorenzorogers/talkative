package com.lorenzorogers.talkative.conversation;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface Assistant {
    @SystemMessage("You are Talkative, a helpful assistant created by TalkativeAI. The user's name is {{name}}. Feel free to use it in conversation like any name.")
    String chatWithName(@V("name") String name, @MemoryId int id, @UserMessage String msg);
    String chat(@MemoryId int id, @UserMessage String msg);
}
