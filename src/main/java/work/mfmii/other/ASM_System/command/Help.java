package work.mfmii.other.ASM_System.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.Config;
import work.mfmii.other.ASM_System.utils.CommandManager;
import work.mfmii.other.ASM_System.utils.CommandMap;
import work.mfmii.other.ASM_System.utils.LanguageUtil;
import work.mfmii.other.ASM_System.utils.PermissionUtil;

import java.util.ArrayList;
import java.util.List;

public class Help extends CommandManager {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    public Help(String name){
        super(name);
    }

    @Override
    public boolean execute(@NotNull User sender, @NotNull String command, @NotNull String[] args, @NotNull MessageReceivedEvent event) {
        LanguageUtil.Language lang = new LanguageUtil().getUserLanguage(event.getAuthor());
        if (command.equalsIgnoreCase("help")) {
            if (!new PermissionUtil().hasPermission(event.getGuild().getId(), event.getChannel().getId(), sender.getId(), this.getPermission())){
                String output = this.getPermissionMessage(lang);
                if(output==null) output = "500: Internal Error";
                event.getChannel().sendMessage(output).queue();
                return true;
            }
            boolean useEmbed = true;
            if(args.length > 0){
                if(args[args.length - 1].equals("-ne")){
                    useEmbed = false;
                }
                for (int i = 0; i < args.length; i++) {

                }
            }
            if(useEmbed) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle(new LanguageUtil().getMessage(lang, "command.help.content.embed.title"))
                        .setAuthor(new LanguageUtil().getMessage(lang, "command.help.content.embed.author.name"),
                                new LanguageUtil().getMessage(lang, "command.help.content.embed.author.url"),
                                new LanguageUtil().getMessage(lang, "command.help.content.embed.author.iconUrl"));
                eb.setDescription(new LanguageUtil().getMessage(lang, "command.help.content.embed.description").replaceAll("\\$\\{prefix\\}", new Config(Config.ConfigType.JSON).getString("prefix")));
                final List<String> _added = new ArrayList<>();
                final List<MessageEmbed.Field> _afterFields = new ArrayList<>();
                new CommandMap().getCommands().forEach(commandManager -> {
                    if (!_added.contains(commandManager.getName()) && new PermissionUtil().hasPermission(event.getGuild().getId(), event.getChannel().getId(), sender.getId(), commandManager.getPermission())) {
                        String prefix = commandManager.isAdminCommand() ? new Config(Config.ConfigType.JSON).getString("admin.prefix") : new Config(Config.ConfigType.JSON).getString("prefix");
                        String aliases_str = !commandManager.getAliases().isEmpty()?"["+String.join(" | ", commandManager.getAliases())+"]":"";
                        String _s1 = commandManager.getUsage(lang).replaceAll("\\$\\{command\\}", prefix + commandManager.getName() + aliases_str);
                        String _s2 = commandManager.getAbout(lang);
                        if (commandManager.isAdminCommand()){
                            _afterFields.add(new MessageEmbed.Field(_s1, _s2, false));
                        }
                        else {
                            eb.addField(_s1, _s2, false);
                        }
                        _added.add(commandManager.getName());
                    }
                });
                _afterFields.forEach(eb::addField);
                event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.help.content.embed.text"))
                        .embed(eb.build())
                        .queue();
            }else {
                StringBuilder sb = new StringBuilder();
                sb.append(new LanguageUtil().getMessage(lang, "command.help.content.text").replaceAll("\\$\\{prefix\\}", new Config(Config.ConfigType.JSON).getString("prefix")));

                final List<String> _afterAdd = new ArrayList<>();
                final List<String> _added = new ArrayList<>();
                new CommandMap().getCommands().forEach(commandManager -> {
                    if (!_added.contains(commandManager.getName()) && new PermissionUtil().hasPermission(event.getGuild().getId(), event.getChannel().getId(), sender.getId(), commandManager.getPermission())) {
                        String _out;
                        String prefix = commandManager.isAdminCommand() ? new Config(Config.ConfigType.JSON).getString("admin.prefix") : new Config(Config.ConfigType.JSON).getString("prefix");
                        String aliases_str = !commandManager.getAliases().isEmpty()?"["+String.join(" | ", commandManager.getAliases())+"]":"";
                        _out = "\n**"+commandManager.getUsage(lang).replaceAll("\\$\\{command\\}", prefix+commandManager.getName()+aliases_str)+"**\n"+commandManager.getAbout(lang);
                        if (commandManager.isAdminCommand()){
                            _afterAdd.add(_out);
                        }
                        else {
                            if (sb.length() + _out.length() > 2000) {
                                event.getChannel().sendMessage(sb.toString()).queue();
                                sb.delete(0, 2000);
                            }
                            sb.append(_out);
                        }
                        _added.add(commandManager.getName());
                    }
                });
                _afterAdd.forEach(s -> {
                    if (sb.length() + s.length() > 2000) {
                        event.getChannel().sendMessage(sb.toString()).queue();
                        sb.delete(0, 2000);
                    }
                    sb.append(s);
                });
                event.getChannel().sendMessage(sb.toString()).queue();
            }
            return true;
        }
        return false;

    }
}
