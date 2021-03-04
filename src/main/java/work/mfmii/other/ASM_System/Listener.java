package work.mfmii.other.ASM_System;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

public class Listener implements EventListener {



    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if(event instanceof ReadyEvent){
            ASMSystem.jda.getTextChannelById("").sendMessage("").queue();
        }
    }
}
