package work.mfmii.other.ASM_System;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class Listener extends ListenerAdapter {

    public Listener(){}

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String prefix = "asm!";
        if(event.getMessage().getContentDisplay().toLowerCase().startsWith(prefix)){

            if(event.getMessage().getContentDisplay().toLowerCase().startsWith(prefix+"help")){

            }
        }
    }
}
