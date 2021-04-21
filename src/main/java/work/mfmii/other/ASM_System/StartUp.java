package work.mfmii.other.ASM_System;

import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import work.mfmii.other.ASM_System.command.*;
import work.mfmii.other.ASM_System.event.guild.GuildBan;
import work.mfmii.other.ASM_System.event.guild.GuildJoin;
import work.mfmii.other.ASM_System.utils.CommandMap;
import work.mfmii.other.ASM_System.utils.EventMap;

public class StartUp {
    public StartUp(){
        CommandMap cmap = new CommandMap();
        cmap.register(new Help("help"));
        cmap.register(new UserInfo("userinfo"));
        cmap.register(new Reboot("reboot"));
        cmap.register(new Shutdown("shutdown"));
        cmap.register(new Leave("leave"));
        cmap.register(new Test("test"));
        cmap.register(new Update("update"));

        EventMap emap = new EventMap();
        emap.register(new GuildJoin(GuildJoinEvent.class));
        emap.register(new GuildBan(GuildBanEvent.class));
    }
}
