package work.mfmii.other.ASM_System.event;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import org.jetbrains.annotations.NotNull;
import work.mfmii.other.ASM_System.utils.EventManager;

public class SelfJoin extends EventManager {
    public SelfJoin(@NotNull Class eventType){
        super(eventType);
    }

    @Override
    public boolean execute(@NotNull GenericEvent genericEvent) {
        if (genericEvent instanceof GuildJoinEvent){
            GuildJoinEvent event = (GuildJoinEvent) genericEvent;
            Guild guild = event.getGuild();
        }
        return false;
    }
}
