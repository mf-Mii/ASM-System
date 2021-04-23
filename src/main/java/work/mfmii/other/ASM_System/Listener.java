package work.mfmii.other.ASM_System;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.utils.CommandMap;
import work.mfmii.other.ASM_System.utils.EventMap;
import work.mfmii.other.ASM_System.utils.LanguageUtil;

public class Listener implements EventListener {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    public Listener(){}

    @Override
    public void onEvent(@NotNull GenericEvent genericEvent) {
        if(genericEvent instanceof MessageReceivedEvent){
            MessageReceivedEvent event = (MessageReceivedEvent) genericEvent;
            System.out.println("LOG: "+event.getMessage());
            if (event.getMessage().getContentRaw().startsWith(new Config(Config.ConfigType.JSON).getString("prefix"))) {
                boolean exec_res = new CommandMap().dispatch(event.getAuthor(), event.getMessage().getContentRaw().replaceFirst(new Config(Config.ConfigType.JSON).getString("prefix"), ""), event, false);
                if (!exec_res){
                    event.getChannel().sendMessage(new LanguageUtil().getMessage(new LanguageUtil().getUserLanguage(event.getAuthor()), "command.unknown")).queue();
                }
            }else if(event.getMessage().getContentRaw().startsWith(new Config(Config.ConfigType.JSON).getString("admin.prefix"))){
                boolean exec_res = new CommandMap().dispatch(event.getAuthor(), event.getMessage().getContentRaw().replaceFirst(new Config(Config.ConfigType.JSON).getString("admin.prefix"), ""), event, true);
                if (!exec_res){
                    event.getChannel().sendMessage(new LanguageUtil().getMessage(new LanguageUtil().getUserLanguage(event.getAuthor()), "command.unknown")).queue();
                }
            }
        }else if(genericEvent instanceof GuildJoinEvent){
            new EventMap().dispatch(GuildJoinEvent.class, genericEvent);
        }
    }
}
