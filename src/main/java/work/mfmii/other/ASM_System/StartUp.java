package work.mfmii.other.ASM_System;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import work.mfmii.other.ASM_System.command.Help;
import work.mfmii.other.ASM_System.command.Reboot;
import work.mfmii.other.ASM_System.command.UserInfo;
import work.mfmii.other.ASM_System.event.SelfJoin;
import work.mfmii.other.ASM_System.utils.CommandMap;
import work.mfmii.other.ASM_System.utils.EventMap;

public class StartUp {
    public StartUp(){
        CommandMap cmap = new CommandMap();
        cmap.register(new Help("help"));
        cmap.register(new UserInfo("userinfo"));
        cmap.register(new Reboot("reboot"));

        EventMap emap = new EventMap();
        emap.register(new SelfJoin(GuildJoinEvent.class));
    }
}
