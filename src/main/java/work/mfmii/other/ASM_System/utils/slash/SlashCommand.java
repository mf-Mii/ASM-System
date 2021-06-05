package work.mfmii.other.ASM_System.utils.slash;

import net.dv8tion.jda.api.entities.*;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.ASMSystem;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class SlashCommand {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JSONObject raw;
    private final long id;
    private final MessageChannel channel;
    private final boolean fromWebhook;
    private final Member member;
    private final OffsetDateTime sentTime;
    public SlashCommand(JSONObject raw, long id, MessageChannel channel, boolean fromWebhook, Member member, OffsetDateTime sentTime){
        if (raw.has("d")) this.raw = raw.getJSONObject("d");
        else this.raw = raw;
        this.id = id;
        this.channel = channel;
        this.fromWebhook = fromWebhook;
        this.member = member;
        this.sentTime = sentTime;
    }

    public boolean isFromWebhook() {
        return fromWebhook;
    }

    public JSONObject getRaw() {
        return raw;
    }

    public long getIdLong() {
        return id;
    }

    public String getId(){
        return String.valueOf(id);
    }

    public Member getMember() {
        return member;
    }

    public MessageChannel getChannel() {
        return channel;
    }

    public OffsetDateTime getSentTime() {
        return sentTime;
    }

    public String getToken(){
        return raw.getString("token");
    }

    public JSONObject getCommandData() {
        return raw.getJSONObject("data");
    }

    public String getCommandId() {
        return getCommandData().getString("id");
    }

    public long getCommandIdAsLong() {
        return Long.parseLong(getCommandId());
    }

    public String getName(){
        return getCommandData().getString("name");
    }



    public Guild getGuild() {
        return member.getGuild();
    }

    public User getAuthor() {
        if (member != null) {
            return member.getUser();
        }else {
            return ASMSystem.jda.getUserById(raw.getJSONObject("user").getString("id"));
        }
    }

    public String getAuthorId() {
        if (member != null) {
            return member.getId();
        }else {
            return raw.getJSONObject("user").getString("id");
        }
    }

    public long getAuthorIdAsLong() {
        return Long.parseLong(getAuthorId());
    }

    /*
    public int replyPing() throws Exception {
        JSONObject send_data = new JSONObject();
        send_data.put("type", replyType.PING.getKey());
        Request request = new Request.Builder()
                .url(String.format("https://discord.com/api/interactions/%s/%s/callback", getId(), getToken()))
                .post(RequestBody.create(send_data.toString(), MediaType.parse("application/json;charset=utf8")))
                .addHeader("Authorization", ASMSystem.jda.getToken())
                .build();
        OkHttpClient client = ASMSystem.jda.getHttpClient();

        Response response = client.newCall(request).execute();
        int response_code = response.code();
        logger.debug("Response Code: "+response_code);
        if (response.isSuccessful())
            return response_code;
        else {
            throw new Exception(response.body().string());
        }
    }

     */

    public int replyMessage(@NotNull replyType type, String message, MessageEmbed embed, boolean privateMessage) throws Exception {
        List<MessageEmbed> embeds = new ArrayList<>();
        embeds.add(embed);
        return replyMessage(type, message, embeds, false, privateMessage);
    }

    public int replyMessage(@NotNull replyType type , String message, List<MessageEmbed> embeds, boolean tts, boolean privateMessage) throws Exception {
        if ((message == null || message.isEmpty()) && (embeds == null || embeds.isEmpty())){
            throw new Exception("You have to set content or embeds");
        }
        JSONObject send_data = new JSONObject();
        send_data.put("type", type.getKey());
        JSONObject send_cnt = new JSONObject();
        if (message != null && !message.isEmpty()) send_cnt.put("content", message);
        if (embeds != null && !embeds.isEmpty()) {
            List<JSONObject> embeds_raw = new ArrayList<>();
            for (MessageEmbed embed : embeds) {
                embeds_raw.add(new JSONObject(embed.toData().toString()));
            }
            send_cnt.put("embeds", embeds_raw);
        }
        send_cnt.put("tts", tts);
        if (privateMessage) send_cnt.put("flags", 64);

        send_data.put("data", send_cnt);


        Request request = new Request.Builder()
                .url(String.format("https://discord.com/api/interactions/%s/%s/callback", getId(), getToken()))
                .post(RequestBody.create(send_data.toString(), MediaType.parse("application/json;charset=utf8")))
                .addHeader("Authorization", ASMSystem.jda.getToken())
                .build();
        logger.debug(request.toString());
        logger.debug(send_data.toString());
        OkHttpClient client = ASMSystem.jda.getHttpClient();

        Response response = client.newCall(request).execute();
        int response_code = response.code();
        logger.debug("Response Code: "+response_code);
        if (response.isSuccessful())
            return response_code;
        else throw new Exception(response.body().string());

    }

    public enum replyType{
        PING(1),
        CHANNEL_MESSAGE_WITH_SOURCE(4),
        DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE(5),
        DEFERRED_UPDATE_MESSAGE(6),
        UPDATE_MESSAGE(7);

        private final int key;

        replyType(int key){
            this.key = key;
        }

        public int getKey() {
            return key;
        }

        public int toInt() {
            return key;
        }
    }
}
