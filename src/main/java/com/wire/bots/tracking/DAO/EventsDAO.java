package com.wire.bots.tracking.DAO;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.util.List;
import java.util.UUID;

public interface EventsDAO {
    @SqlUpdate("INSERT INTO Events (messageId, conversationId, type, payload, time) " +
            "VALUES (:messageId, :conversationId, :type, :payload, CURRENT_TIMESTAMP) ON CONFLICT (messageId) DO " +
            "UPDATE SET type = EXCLUDED.type, payload = EXCLUDED.payload")
    int insert(@Bind("messageId") UUID messageId,
               @Bind("conversationId") UUID conversationId,
               @Bind("type") String type,
               @Bind("payload") String payload);

    @SqlQuery("SELECT payload FROM Events WHERE conversationId = :conversationId AND type = :type")
    List<String> getEvents(@Bind("conversationId") UUID conversationId,
                           @Bind("type") String type);

}
