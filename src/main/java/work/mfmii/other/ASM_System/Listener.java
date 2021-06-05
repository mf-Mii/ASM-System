package work.mfmii.other.ASM_System;

import net.dv8tion.jda.api.events.DisconnectEvent;
import net.dv8tion.jda.api.events.ExceptionEvent;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.RawGatewayEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.internal.JDAImpl;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.utils.EventMap;
import work.mfmii.other.ASM_System.utils.slash.SlashCommand;
import work.mfmii.other.ASM_System.utils.slash.SlashCommandEvent;

import java.io.IOException;
import java.time.OffsetDateTime;

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
            ExceptionEvent event = (ExceptionEvent) genericEvent;

        }


        else if(genericEvent instanceof RawGatewayEvent){
            RawGatewayEvent e = (RawGatewayEvent) genericEvent;
            JSONObject data = new JSONObject(e.getPackage().toString());
            if (data.getString("t").equalsIgnoreCase("INTERACTION_CREATE")){//SlashCommand
                JDAImpl jda = (JDAImpl) e.getJDA();
                SlashCommand slCmd = new SlashCommand(data.getJSONObject("d"),
                        data.getJSONObject("d").getLong("id"),
                        e.getJDA().getTextChannelById(data.getJSONObject("d").getString("channel_id")),
                        false,
                        e.getJDA().getGuildById(data.getJSONObject("d").getString("guild_id")).getMemberById(data.getJSONObject("d").getJSONObject("member").getJSONObject("user").getString("id")),
                        OffsetDateTime.now()
                        );
                jda.handleEvent(new SlashCommandEvent(
                        e.getJDA(),
                        e.getResponseNumber(),
                        slCmd
                ));
            }
        }else if (genericEvent instanceof SlashCommandEvent){
            SlashCommandEvent event = (SlashCommandEvent) genericEvent;
            try {
                event.getSlashCommand().replyMessage(SlashCommand.replyType.CHANNEL_MESSAGE_WITH_SOURCE, "イントラクションの受信を確認", null, false, true);
            } catch (IOException e) {
                event.getSlashCommand().getChannel().sendMessage("Error!!\n```"+e.getMessage()+"```").queue();
                e.printStackTrace();
            }
        }
    }
}
