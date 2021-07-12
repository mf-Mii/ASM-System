package work.mfmii.other.ASM_System;

import net.dv8tion.jda.api.events.DisconnectEvent;
import net.dv8tion.jda.api.events.ExceptionEvent;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.utils.EventMap;

public class Listener implements EventListener {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    public Listener(){}

    @Override
    public void onEvent(@NotNull GenericEvent genericEvent) {
        if(genericEvent instanceof MessageReceivedEvent) {
            new EventMap().dispatch(MessageReceivedEvent.class, genericEvent);
        }else if(genericEvent instanceof SlashCommandEvent){
            new EventMap().dispatch(SlashCommandEvent.class, genericEvent);
        }else if(genericEvent instanceof GuildJoinEvent){
            new EventMap().dispatch(GuildJoinEvent.class, genericEvent);
        }else if(genericEvent instanceof GuildBanEvent){
            new EventMap().dispatch(GuildBanEvent.class, genericEvent);
        }else if(genericEvent instanceof DisconnectEvent){

        }else if(genericEvent instanceof ExceptionEvent){
            ExceptionEvent event = (ExceptionEvent) genericEvent;

        }

        /*
        else if(genericEvent instanceof RawGatewayEvent){
            RawGatewayEvent e = (RawGatewayEvent) genericEvent;
            JSONObject data = new JSONObject(e.getPackage().toString());
            if (data.getString("t").equalsIgnoreCase("INTERACTION_CREATE")){//SlashCommand
                JDAImpl jda = (JDAImpl) e.getJDA();
                SlashCommand slCmd = new SlashCommand(data.getJSONObject("d"),
                        data.getJSONObject("d").getLong("id"),
                        e.getJDA().getTextChannelById(data.getJSONObject("d").getString("channel_id")),
                        false,
                        data.getJSONObject("d").has("guild_id") ? e.getJDA().getGuildById(data.getJSONObject("d").getString("guild_id")).getMemberById(data.getJSONObject("d").getJSONObject("member").getJSONObject("user").getString("id")) : null,
                        OffsetDateTime.now()
                        );
                jda.handleEvent(new work.mfmii.other.ASM_System.utils.slash.SlashCommandEvent(
                        e.getJDA(),
                        e.getResponseNumber(),
                        slCmd
                ));
            }
        }else if (genericEvent instanceof work.mfmii.other.ASM_System.utils.slash.SlashCommandEvent){
            new EventMap().dispatch(work.mfmii.other.ASM_System.utils.slash.SlashCommandEvent.class, genericEvent);
        }

             */
    }
}
