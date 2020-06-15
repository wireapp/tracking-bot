package com.wire.bots.tracking.DAO;

import com.wire.bots.tracking.model.Event;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;
import java.util.UUID;

public interface EventsDAO {
    @SqlUpdate("INSERT INTO Events (messageId, conversationId, botId, type, payload, time) " +
            "VALUES (:messageId, :conversationId, :botId, :type, :payload, CURRENT_TIMESTAMP) ON CONFLICT (messageId) DO " +
            "UPDATE SET type = EXCLUDED.type, payload = EXCLUDED.payload")
    int insert(@Bind("messageId") UUID messageId,
               @Bind("conversationId") UUID conversationId,
               @Bind("botId") UUID botId,
               @Bind("type") String type,
               @Bind("payload") String payload);

    @SqlQuery("SELECT payload FROM Events WHERE conversationId = :conversationId AND type = :type")
    List<String> getEvents(@Bind("conversationId") UUID conversationId,
                           @Bind("type") String type);

    @SqlQuery("SELECT * FROM Events WHERE messageId = :messageId")
    @RegisterMapper(EventsResultSetMapper.class)
    Event get(@Bind("messageId") UUID messageId);

}
