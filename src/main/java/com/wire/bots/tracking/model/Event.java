package com.wire.bots.tracking.model;

import java.util.UUID;

public class Event {
    public UUID messageId;
    public UUID conversationId;
    public UUID botId;
    public String type;
    public String payload;
    public String time;
}
