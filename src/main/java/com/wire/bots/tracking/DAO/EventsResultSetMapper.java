package com.wire.bots.tracking.DAO;

import com.wire.bots.tracking.model.Event;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class EventsResultSetMapper implements ResultSetMapper<Event> {
    @Override
    public Event map(int i, ResultSet rs, StatementContext statementContext) throws SQLException {
        Event event = new Event();
        Object conversationId = rs.getObject("conversationId");
        if (conversationId != null)
            event.conversationId = (UUID) conversationId;
        event.time = rs.getString("time");
        event.type = rs.getString("type");
        event.payload = rs.getString("payload");
        Object messageId = rs.getObject("messageId");
        if (messageId != null)
            event.messageId = (UUID) messageId;
        Object botId = rs.getObject("botId");
        if (botId != null)
            event.botId = (UUID) botId;
        return event;
    }
}
