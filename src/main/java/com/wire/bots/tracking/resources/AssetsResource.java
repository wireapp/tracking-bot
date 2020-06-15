package com.wire.bots.tracking.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.bots.sdk.API;
import com.wire.bots.sdk.exceptions.HttpException;
import com.wire.bots.sdk.exceptions.MissingStateException;
import com.wire.bots.sdk.factories.StorageFactory;
import com.wire.bots.sdk.models.AttachmentMessage;
import com.wire.bots.sdk.server.model.ErrorMessage;
import com.wire.bots.sdk.state.State;
import com.wire.bots.sdk.tools.Util;
import com.wire.bots.tracking.DAO.EventsDAO;
import com.wire.bots.tracking.model.Event;
import io.swagger.annotations.*;
import org.skife.jdbi.v2.DBI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.UUID;

@Api
@Path("/assets")
public class AssetsResource {
    private final Client client;
    private final EventsDAO eventsDAO;
    private final StorageFactory storageFactory;
    private final ObjectMapper objectMapper;

    public AssetsResource(Client client, DBI jdbi, ObjectMapper objectMapper, StorageFactory storageFactory) {
        this.client = client;
        this.objectMapper = objectMapper;
        eventsDAO = jdbi.onDemand(EventsDAO.class);
        this.storageFactory = storageFactory;
    }

    @GET
    @Path("/{messageId}")
    @ApiOperation(value = "Get the asset file")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Asset file")})
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@ApiParam @PathParam("messageId") UUID messageId) {
        try {
            final Event event = eventsDAO.get(messageId);
            if (event == null) {
                return Response.ok(new ErrorMessage("Unknown MessageId: " + messageId))
                        .status(404)
                        .build();
            }

            AttachmentMessage message = objectMapper.readValue(event.payload, AttachmentMessage.class);

            if (message.getMimeType() == null) {
                return Response.ok(new ErrorMessage("This is not an asset"))
                        .status(418)
                        .build();
            }

            if (message.getAssetToken() == null) {
                return Response.ok(new ErrorMessage("This asset is missing the token"))
                        .status(400)
                        .build();
            }

            if (event.botId == null) {
                return Response.ok(new ErrorMessage("This asset is missing botId"))
                        .status(400)
                        .build();
            }

            final State state = storageFactory.create(event.botId);

            API api = new API(client, state.getState().token);
            final byte[] cipher = api.downloadAsset(message.getAssetKey(), message.getAssetToken());
            byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(cipher);
            if (!Arrays.equals(sha256, message.getSha256())) {
                Response.ok(new ErrorMessage("SHA256 mismatch"))
                        .status(400)
                        .build();
            }

            final byte[] bytes = Util.decrypt(message.getOtrKey(), cipher);

            return Response.ok(bytes)
                    .type(message.getMimeType())
                    .header("Content-Disposition", String.format("attachment; filename=\"%s\"", message.getName()))
                    .build();
        } catch (HttpException e) {
            return Response.ok(new ErrorMessage(e.getMessage()))
                    .status(e.getCode())
                    .build();
        } catch (MissingStateException e) {
            return Response.ok(new ErrorMessage(e.getMessage()))
                    .status(400)
                    .build();
        } catch (Exception e) {
            return Response.ok(new ErrorMessage(e.getMessage()))
                    .status(500)
                    .build();
        }
    }
}
