package work.mfmii.other.ASM_System;

import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.command.Shutdown;
import work.mfmii.other.ASM_System.command.*;
import work.mfmii.other.ASM_System.event.MessageReceived;
import work.mfmii.other.ASM_System.event.SlashCommandReceived;
import work.mfmii.other.ASM_System.event.guild.GuildBan;
import work.mfmii.other.ASM_System.event.guild.GuildJoin;
import work.mfmii.other.ASM_System.utils.CommandMap;
import work.mfmii.other.ASM_System.utils.EventMap;
import work.mfmii.other.ASM_System.utils.slash.SlashCommandEvent;

public class StartUp {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    public StartUp(){
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.debug("Registering Events");
        //Event Setup
        EventMap emap = new EventMap();
        emap.register(new GuildJoin(GuildJoinEvent.class));
        emap.register(new GuildBan(GuildBanEvent.class));
        emap.register(new MessageReceived(MessageReceivedEvent.class));
        emap.register(new SlashCommandReceived(SlashCommandEvent.class));

        //OLD command setup
        logger.debug("Registering Old Commands");
        CommandMap cmap = new CommandMap();
        cmap.register(new Help("help"));
        cmap.register(new UserInfo("userinfo"));
        cmap.register(new Clear("clear"));
        cmap.register(new Reboot("reboot"));
        cmap.register(new Shutdown("shutdown"));
        cmap.register(new Leave("leave"));
        cmap.register(new Test("test"));
        cmap.register(new Update("update"));

    }
}
