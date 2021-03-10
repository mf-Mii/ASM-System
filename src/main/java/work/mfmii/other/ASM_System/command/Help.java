package work.mfmii.other.ASM_System.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import work.mfmii.other.ASM_System.Config;
import work.mfmii.other.ASM_System.utils.CommandManager;
import work.mfmii.other.ASM_System.utils.LanguageUtil;

public class Help extends CommandManager {
    public Help(String name){
        super(name);
    }

    @Override
    public boolean execute(@NotNull User sender, @NotNull String command, @NotNull String[] args, @NotNull MessageReceivedEvent event) {
        if (command.equalsIgnoreCase("help")) {
            boolean isEmbed = true;
            if(args.length > 0){
                if(args[args.length-1] == "-ne"){
                    isEmbed = false;
                }
            }
            LanguageUtil.Language lang = new LanguageUtil().getUserLanguage(event.getAuthor());
            if(isEmbed) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle(new LanguageUtil().getMessage(lang, "command.help.content.embed.title"))
                        .setAuthor(new LanguageUtil().getMessage(lang, "command.help.content.embed.author.name"),
                                new LanguageUtil().getMessage(lang, "command.help.content.embed.author.url"),
                                new LanguageUtil().getMessage(lang, "command.help.content.embed.author.iconUrl"));
                eb.setDescription(new LanguageUtil().getMessage(lang, "command.help.content.embed.description").replaceAll("\\$\\{prefix\\}", new Config(Config.ConfigType.JSON).getString("prefix")));
                event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.help.content.embed.text"))
                        .embed(eb.build())
                        .queue();
            }else {
                StringBuilder sb = new StringBuilder();
                sb.append(new LanguageUtil().getMessage(lang, "command.help.content.text"));


                event.getChannel().sendMessage(sb.toString()).queue();
            }
            return true;
        }
        return false;

    }
}
