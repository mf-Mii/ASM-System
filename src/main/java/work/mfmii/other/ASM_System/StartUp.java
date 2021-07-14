package work.mfmii.other.ASM_System;

import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.command.Shutdown;
import work.mfmii.other.ASM_System.command.*;
import work.mfmii.other.ASM_System.event.ButtonAction;
import work.mfmii.other.ASM_System.event.MessageReceived;
import work.mfmii.other.ASM_System.event.SlashCommandReceived;
import work.mfmii.other.ASM_System.event.guild.GuildBan;
import work.mfmii.other.ASM_System.event.guild.GuildJoin;
import work.mfmii.other.ASM_System.utils.CommandManager;
import work.mfmii.other.ASM_System.utils.CommandMap;
import work.mfmii.other.ASM_System.utils.EventMap;
import work.mfmii.other.ASM_System.utils.LanguageUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        emap.register(new ButtonAction(ButtonClickEvent.class));

        //Command setup
        logger.debug("Registering Old Commands");
        CommandMap cmap = new CommandMap();
        cmap.register(new Command("commands"));
        cmap.register(new UserInfo("userinfo"));
        cmap.register(new Clear("clear"));
        cmap.register(new Contact("contact"));
        cmap.register(new Reboot("reboot"));
        cmap.register(new Shutdown("shutdown"));
        cmap.register(new Leave("leave"));
        cmap.register(new Test("test"));
        cmap.register(new Update("update"));


        //SlashCommand Setup
        //Creating RegisteredSlashCommandList
        List<String> registeredCommands = new ArrayList<>();
        ASMSystem.jda.retrieveCommands().complete().forEach(command -> {
            registeredCommands.add(command.getName());
            logger.debug("SlashCommandExist: "+command.getName());
        });
        //Creating Registered localCommandList
        Map<String, CommandManager> commands = new HashMap<>();
        cmap.getCommands().forEach(commandManager -> {
            //logger.debug("adding: "+commandManager.getName());
            if (!commands.containsValue(commandManager)) {
                commands.put(commandManager.getName(), commandManager);
            }
        });
        if (!registeredCommands.contains("commands") && commands.containsKey("commands")) {
            CommandManager cmd = cmap.getCommand("commands");
            CommandData data = new CommandData(cmd.getName(), cmd.getAbout(LanguageUtil.Language.DEFAULT) + " / " + cmd.getAbout(LanguageUtil.Language.ENGLISH));
            OptionData optionData = new OptionData(OptionType.STRING, "name", "Select command name that you want to get help", false);
            commands.forEach((s, commandManager) -> {
                optionData.addChoice(s, s);
                logger.debug("Adding choice: "+s);
            });
            data.addOptions(optionData);
            ASMSystem.jda.upsertCommand(data).queue();
        }



    }
}
