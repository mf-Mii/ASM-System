package work.mfmii.other.ASM_System.command;

import jdk.jfr.Event;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import work.mfmii.other.ASM_System.utils.CommandManager;

public class Help extends CommandManager {
    public Help(String name){
        super(name);
    }

    @Override
    public boolean execute(@NotNull User sender, @NotNull String command, @NotNull String[] args, @NotNull GenericEvent genericEvent) {
        if(genericEvent instanceof MessageReceivedEvent) {
            MessageReceivedEvent event = (MessageReceivedEvent) genericEvent;
            if (command.equalsIgnoreCase("help")) {
                event.getChannel().sendMessage("TEST").queue();
                return true;
            }
        }
        return false;

    }
}
