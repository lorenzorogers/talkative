package com.lorenzorogers.talkative.conversation;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;

public interface Assistant {
    String chat(@MemoryId int id, @UserMessage String msg);
}
