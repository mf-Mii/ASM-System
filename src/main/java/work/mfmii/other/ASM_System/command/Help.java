package work.mfmii.other.ASM_System.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import work.mfmii.other.ASM_System.Config;
import work.mfmii.other.ASM_System.utils.*;

import java.util.Iterator;

public class Help extends CommandManager {
    public Help(String name){
        super(name);
    }

    @Override
    public boolean execute(@NotNull User sender, @NotNull String command, @NotNull String[] args, @NotNull MessageReceivedEvent event) {
        LanguageUtil.Language lang = new LanguageUtil().getUserLanguage(event.getAuthor());
        if (command.equalsIgnoreCase("help")) {
            if (!new PermissionUtil().hasPermission(event.getGuild().getId(), event.getChannel().getId(), sender.getId(), "asm.command.help")){
                String output = new LanguageUtil().getMessage(lang, "command.help.permissionMessage");
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
                new CommandMap().getCommands().forEach(commandManager -> {
                    eb.addField(commandManager.getName(), commandManager.getAbout(lang), false);
                });
                event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.help.content.embed.text"))
                        .embed(eb.build())
                        .queue();
            }else {
                StringBuilder sb = new StringBuilder();
                sb.append(new LanguageUtil().getMessage(lang, "command.help.content.text").replaceAll("\\$\\{prefix\\}", new Config(Config.ConfigType.JSON).getString("prefix")));


                event.getChannel().sendMessage(sb.toString()).queue();
            }
            return true;
        }
        return false;

    }
}
