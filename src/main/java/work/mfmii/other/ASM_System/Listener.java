package work.mfmii.other.ASM_System;

import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import work.mfmii.other.ASM_System.utils.CommandManager;
import work.mfmii.other.ASM_System.utils.CommandMap;
import work.mfmii.other.ASM_System.utils.LanguageUtil;

public class Listener implements EventListener {

    public Listener(){}

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        Event e = (Event) event;
        if(event instanceof MessageReceivedEvent){
            MessageReceivedEvent mEvent = (MessageReceivedEvent) event;
            System.out.println("LOG: "+mEvent.getMessage());
            if (mEvent.getMessage().getContentRaw().startsWith(new Config(Config.ConfigType.JSON).getString("prefix"))) {
                boolean exec_res = new CommandMap().dispatch(mEvent.getAuthor(), mEvent.getMessage().getContentRaw().replaceFirst(new Config(Config.ConfigType.JSON).getString("prefix"), ""), mEvent);
                if (!exec_res){
                    mEvent.getChannel().sendMessage(new LanguageUtil().getMessage(new LanguageUtil().getUserLanguage(mEvent.getAuthor()), "command.unknown")).queue();
                }
            }
        }
    }
}
