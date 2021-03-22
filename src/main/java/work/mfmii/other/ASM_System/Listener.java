package work.mfmii.other.ASM_System;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import work.mfmii.other.ASM_System.utils.CommandMap;
import work.mfmii.other.ASM_System.utils.LanguageUtil;

public class Listener implements EventListener {

    public Listener(){}

    @Override
    public void onEvent(@NotNull GenericEvent genericEvent) {
        if(genericEvent instanceof MessageReceivedEvent){
            MessageReceivedEvent event = (MessageReceivedEvent) genericEvent;
            System.out.println("LOG: "+event.getMessage());
            if (event.getMessage().getContentRaw().startsWith(new Config(Config.ConfigType.JSON).getString("prefix"))) {
                boolean exec_res = new CommandMap().dispatch(event.getAuthor(), event.getMessage().getContentRaw().replaceFirst(new Config(Config.ConfigType.JSON).getString("prefix"), ""), event);
                if (!exec_res){
                    event.getChannel().sendMessage(new LanguageUtil().getMessage(new LanguageUtil().getUserLanguage(event.getAuthor()), "command.unknown")).queue();
                }
            }
        }else if(genericEvent instanceof GuildJoinEvent){
            GuildJoinEvent event = (GuildJoinEvent) genericEvent;

        }
    }
}
