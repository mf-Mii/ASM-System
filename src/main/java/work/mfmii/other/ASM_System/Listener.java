package work.mfmii.other.ASM_System;

import net.dv8tion.jda.api.events.DisconnectEvent;
import net.dv8tion.jda.api.events.ExceptionEvent;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
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
        }else if(genericEvent instanceof SlashCommandEvent) {
            new EventMap().dispatch(SlashCommandEvent.class, genericEvent);
        }else if(genericEvent instanceof ButtonClickEvent) {
            new EventMap().dispatch(ButtonClickEvent.class, genericEvent);
        }else if(genericEvent instanceof GuildJoinEvent){
            new EventMap().dispatch(GuildJoinEvent.class, genericEvent);
        }else if(genericEvent instanceof GuildBanEvent){
            new EventMap().dispatch(GuildBanEvent.class, genericEvent);
        }else if(genericEvent instanceof DisconnectEvent){

        }else if(genericEvent instanceof ExceptionEvent){
            ExceptionEvent event = (ExceptionEvent) genericEvent;

        }
    }
}
