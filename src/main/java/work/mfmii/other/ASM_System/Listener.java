package work.mfmii.other.ASM_System;

import net.dv8tion.jda.api.events.*;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.utils.EventMap;

import java.io.IOException;

public class Listener implements EventListener {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    public Listener(){}

    @Override
    public void onEvent(@NotNull GenericEvent genericEvent) {
        if(genericEvent instanceof MessageReceivedEvent){
            new EventMap().dispatch(MessageReceivedEvent.class, genericEvent);
        }else if(genericEvent instanceof GuildJoinEvent){
            new EventMap().dispatch(GuildJoinEvent.class, genericEvent);
        }else if(genericEvent instanceof GuildBanEvent){
            new EventMap().dispatch(GuildBanEvent.class, genericEvent);
        }else if(genericEvent instanceof DisconnectEvent){

        }else if(genericEvent instanceof ExceptionEvent){

        }else if (genericEvent instanceof ReadyEvent){

        }else if(genericEvent instanceof ResumedEvent){

        }else if(genericEvent instanceof ReconnectedEvent){

        }else if(genericEvent instanceof ShutdownEvent){

        }else if(genericEvent instanceof StatusChangeEvent){

        }else if(genericEvent instanceof GatewayPingEvent){

        }else if(genericEvent instanceof MessageBulkDeleteEvent){

        }else if(genericEvent instanceof GuildMemberJoinEvent){

        }


        else if(genericEvent instanceof RawGatewayEvent){
            RawGatewayEvent e = (RawGatewayEvent) genericEvent;
            JSONObject data = new JSONObject(e.getPackage().toString());
            if (data.getString("t").equalsIgnoreCase("INTERACTION_CREATE")){//SlashCommand
                logger.debug("SlashCommandReceived");
                logger.debug(e.getPackage().toString());
                try {
                    JSONObject j = new JSONObject();
                    j.put("type", 4)
                            .put("data", new JSONObject().put("content", "イントラクションを受信📥"));
                    RequestBody requestBody = RequestBody.create(j.toString(), MediaType.parse("application/json;charset=utf8"));
                    Request request = new Request.Builder()
                            .url(String.format("https://discord.com/api/interactions/%s/%s/callback",
                                    String.valueOf(data.getJSONObject("d").getLong("id")),
                                    data.getJSONObject("d").getString("token")))
                            .post(requestBody)
                            .addHeader("Authorization", ASMSystem.jda.getToken())
                            .build();
                    logger.debug(request.toString());
                    Response response = ASMSystem.jda.getHttpClient().newCall(request).execute();
                    logger.debug("response: "+response.body().string());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }
}
