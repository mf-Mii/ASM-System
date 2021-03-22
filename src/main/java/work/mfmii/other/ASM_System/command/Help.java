package work.mfmii.other.ASM_System.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import work.mfmii.other.ASM_System.Config;
import work.mfmii.other.ASM_System.utils.CommandManager;
import work.mfmii.other.ASM_System.utils.CommandMap;
import work.mfmii.other.ASM_System.utils.LanguageUtil;
import work.mfmii.other.ASM_System.utils.PermissionUtil;

import java.util.ArrayList;
import java.util.List;

public class Help extends CommandManager {
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
            }
            if(useEmbed) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle(new LanguageUtil().getMessage(lang, "command.help.content.embed.title"))
                        .setAuthor(new LanguageUtil().getMessage(lang, "command.help.content.embed.author.name"),
                                new LanguageUtil().getMessage(lang, "command.help.content.embed.author.url"),
                                new LanguageUtil().getMessage(lang, "command.help.content.embed.author.iconUrl"));
                eb.setDescription(new LanguageUtil().getMessage(lang, "command.help.content.embed.description").replaceAll("\\$\\{prefix\\}", new Config(Config.ConfigType.JSON).getString("prefix")));
                final List<String> _added = new ArrayList<>();
                new CommandMap().getCommands().forEach(commandManager -> {
                    if (!_added.contains(commandManager.getName()) && new PermissionUtil().hasPermission(event.getGuild().getId(), event.getChannel().getId(), sender.getId(), commandManager.getPermission())) {
                        String aliases_str = !commandManager.getAliases().isEmpty()?"["+String.join(" | ", commandManager.getAliases())+"]":"";
                        String _s1 = commandManager.getUsage(lang).replaceAll("\\$\\{command\\}", new Config(Config.ConfigType.JSON).getString("prefix") + commandManager.getName() + aliases_str);
                        String _s2 = commandManager.getAbout(lang);
                        eb.addField(_s1, _s2, false);
                        _added.add(commandManager.getName());
                    }
                });
                event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.help.content.embed.text"))
                        .embed(eb.build())
                        .queue();
            }else {
                StringBuilder sb = new StringBuilder();
                sb.append(new LanguageUtil().getMessage(lang, "command.help.content.text").replaceAll("\\$\\{prefix\\}", new Config(Config.ConfigType.JSON).getString("prefix")));

                final List<String> _added = new ArrayList<>();
                new CommandMap().getCommands().forEach(commandManager -> {
                    if (!_added.contains(commandManager.getName()) && new PermissionUtil().hasPermission(event.getGuild().getId(), event.getChannel().getId(), sender.getId(), commandManager.getPermission())) {
                        String _out;
                        String aliases_str = !commandManager.getAliases().isEmpty()?"["+String.join(" | ", commandManager.getAliases())+"]":"";
                        _out = "\n**"+commandManager.getUsage(lang).replaceAll("\\$\\{command\\}", new Config(Config.ConfigType.JSON).getString("prefix")+commandManager.getName()+aliases_str)+"**\n"+commandManager.getAbout(lang);
                        if (sb.length()+_out.length() > 2000){
                            event.getChannel().sendMessage(sb.toString()).queue();
                            sb.delete(0, 2000);
                        }
                        sb.append(_out);
                        _added.add(commandManager.getName());
                    }
                });
                event.getChannel().sendMessage(sb.toString()).queue();
            }
            return true;
        }
        return false;

    }
}
