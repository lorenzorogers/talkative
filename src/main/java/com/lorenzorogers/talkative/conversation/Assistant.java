package com.lorenzorogers.talkative.conversation;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface Assistant {
    @SystemMessage("You are Talkative, a helpful assistant.")
    String chat(@MemoryId int id, @UserMessage String msg);
}
