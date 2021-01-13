package com.wire.bots.tracking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wire.bots.tracking.DAO.EventsDAO;
import com.wire.xenon.MessageHandlerBase;
import com.wire.xenon.WireClient;
import com.wire.xenon.assets.Reaction;
import com.wire.xenon.backend.models.NewBot;
import com.wire.xenon.backend.models.SystemMessage;
import com.wire.xenon.models.*;
import com.wire.xenon.tools.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class MessageHandler extends MessageHandlerBase {
    private final ObjectMapper mapper = new ObjectMapper();
    private final EventsDAO eventsDAO;

    MessageHandler(EventsDAO eventsDAO) {
        this.eventsDAO = eventsDAO;
    }

    @Override
    public void onNewConversation(WireClient client, SystemMessage msg) {
        UUID convId = msg.convId;
        UUID botId = client.getId();
        UUID messageId = msg.id;
        String type = msg.type;

        persist(convId, null, botId, messageId, type, msg);
    }

    @Override
    public void onMemberJoin(WireClient client, SystemMessage msg) {
        UUID botId = client.getId();
        UUID convId = msg.convId;
        UUID messageId = msg.id;
        String type = msg.type;

        persist(convId, null, botId, messageId, type, msg);
    }

    @Override
    public void onMemberLeave(WireClient client, SystemMessage msg) {
        UUID convId = msg.convId;
        UUID botId = client.getId();
        UUID messageId = msg.id;
        String type = msg.type;

        persist(convId, null, botId, messageId, type, msg);
    }

    @Override
    public void onConversationRename(WireClient client, SystemMessage msg) {
        UUID convId = msg.convId;
        UUID botId = client.getId();
        UUID messageId = msg.id;
        String type = msg.type;

        persist(convId, null, botId, messageId, type, msg);
    }

    @Override
    public void onBotRemoved(UUID botId, SystemMessage msg) {
        UUID convId = msg.convId;
        UUID messageId = msg.id;
        String type = "conversation.member-leave.bot-removed";

        persist(convId, null, botId, messageId, type, msg);
    }

    @Override
    public void onText(WireClient client, TextMessage msg) {
        UUID userId = msg.getUserId();
        UUID botId = client.getId();
        UUID messageId = msg.getMessageId();
        UUID convId = client.getConversationId();
        String type = "conversation.otr-message-add.new-text";

        persist(convId, userId, botId, messageId, type, msg);
    }

    @Override
    public void onText(WireClient client, EphemeralTextMessage msg) {
        UUID convId = client.getConversationId();
        UUID userId = client.getId();
        UUID senderId = msg.getUserId();
        UUID messageId = msg.getMessageId();
        String type = "conversation.otr-message-add.new-text";

        persist(convId, senderId, userId, messageId, type, msg);
    }

    @Override
    public void onEditText(WireClient client, EditedTextMessage msg) {
        UUID botId = client.getId();
        UUID convId = client.getConversationId();
        UUID userId = msg.getUserId();
        UUID messageId = msg.getMessageId();
        String type = "conversation.otr-message-add.edit-text";

        persist(convId, userId, botId, messageId, type, msg);
    }

    @Override
    public void onDelete(WireClient client, DeletedTextMessage msg) {
        UUID botId = client.getId();
        UUID messageId = msg.getMessageId();
        UUID convId = client.getConversationId();
        UUID userId = msg.getUserId();
        String type = "conversation.otr-message-add.delete-text";

        persist(convId, userId, botId, messageId, type, msg);
    }

    @Override
    public void onImage(WireClient client, ImageMessage msg) {
        UUID convId = client.getConversationId();
        UUID messageId = msg.getMessageId();
        UUID botId = client.getId();
        UUID userId = msg.getUserId();
        String type = "conversation.otr-message-add.new-image";

        persist(convId, userId, botId, messageId, type, msg);
    }

    @Override
    public void onVideo(WireClient client, VideoMessage msg) {
        UUID convId = client.getConversationId();
        UUID messageId = msg.getMessageId();
        UUID botId = client.getId();
        UUID userId = msg.getUserId();
        String type = "conversation.otr-message-add.new-video";

        persist(convId, userId, botId, messageId, type, msg);
    }

    @Override
    public void onVideoPreview(WireClient client, ImageMessage msg) {
        UUID convId = client.getConversationId();
        UUID messageId = UUID.randomUUID();
        UUID botId = client.getId();
        UUID userId = msg.getUserId();
        String type = "conversation.otr-message-add.new-preview";

        persist(convId, userId, botId, messageId, type, msg);
    }

    public void onLinkPreview(WireClient client, LinkPreviewMessage msg) {
        UUID convId = client.getConversationId();
        UUID messageId = msg.getMessageId();
        UUID botId = client.getId();
        UUID userId = msg.getUserId();
        String type = "conversation.otr-message-add.new-link";

        persist(convId, userId, botId, messageId, type, msg);
    }

    @Override
    public void onAttachment(WireClient client, AttachmentMessage msg) {
        UUID convId = client.getConversationId();
        UUID botId = client.getId();
        UUID messageId = msg.getMessageId();
        UUID userId = msg.getUserId();
        String type = "conversation.otr-message-add.new-attachment";

        persist(convId, userId, botId, messageId, type, msg);

        if (msg.getName().endsWith(".cms")) {
            try {
                byte[] cms = client.downloadAsset(
                        msg.getAssetKey(),
                        msg.getAssetToken(),
                        msg.getSha256(),
                        msg.getOtrKey()
                );

                for (AttachmentMessage pdfMessage : listPDFs(convId)) {
                    byte[] pdf = client.downloadAsset(
                            pdfMessage.getAssetKey(),
                            pdfMessage.getAssetToken(),
                            pdfMessage.getSha256(),
                            pdfMessage.getOtrKey()
                    );

                    if (Tools.verify(pdf, cms)) {
                        client.send(new Reaction(messageId, "❤️"));
                        client.send(new Reaction(pdfMessage.getMessageId(), "❤️"));
                        Logger.info("Signature verified. %s", msg.getName());
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private AttachmentMessage findPDF(String filename, UUID convId) throws IOException {
        final String assetId = filename.substring(filename.indexOf("(") + 1, filename.indexOf(")"));

        for (String payload : eventsDAO.getEvents(convId, "conversation.otr-message-add.new-attachment")) {
            AttachmentMessage pdfMessage = mapper.readValue(payload, AttachmentMessage.class);
            final String assetKey = pdfMessage.getAssetKey();
            final String mimeType = pdfMessage.getMimeType();

            if (mimeType.equals("application/pdf") && assetKey.equals(assetId)) {
                return pdfMessage;
            }
        }
        return null;
    }

    private ArrayList<AttachmentMessage> listPDFs(UUID convId) throws IOException {
        ArrayList<AttachmentMessage> ret = new ArrayList<>();
        for (String payload : eventsDAO.getEvents(convId, "conversation.otr-message-add.new-attachment")) {
            AttachmentMessage pdfMessage = mapper.readValue(payload, AttachmentMessage.class);
            final String mimeType = pdfMessage.getMimeType();
            if (mimeType.equals("application/pdf")) {
                ret.add(pdfMessage);
            }
        }
        return ret;
    }

    @Override
    public void onReaction(WireClient client, ReactionMessage msg) {
        UUID convId = client.getConversationId();
        UUID messageId = msg.getMessageId();
        UUID botId = client.getId();
        UUID userId = msg.getUserId();
        String type = "conversation.otr-message-add.new-reaction";

        persist(convId, userId, botId, messageId, type, msg);
    }

    @Override
    public void onPing(WireClient client, PingMessage msg) {
        UUID botId = client.getId();
        UUID convId = client.getConversationId();
        UUID messageId = msg.getMessageId();
        UUID userId = msg.getUserId();
        String type = "conversation.otr-message-add.new-ping";

        persist(convId, userId, botId, messageId, type, msg);
    }

    public String getName(NewBot newBot) {
        return "Verified";
    }

    private void persist(UUID convId, UUID senderId, UUID userId, UUID msgId, String type, Object msg) throws RuntimeException {
        try {
            String payload = mapper.writeValueAsString(msg);
            int insert = eventsDAO.insert(msgId, convId, type, payload);

            Logger.debug("%s: conv: %s, %s -> %s, msg: %s, insert: %d",
                    type,
                    convId,
                    senderId,
                    userId,
                    msgId,
                    insert);
        } catch (Exception e) {
            String error = String.format("%s: conv: %s, user: %s, msg: %s, e: %s",
                    type,
                    convId,
                    userId,
                    msgId,
                    e);
            Logger.error(error);
            throw new RuntimeException(error);
        }
    }
}
