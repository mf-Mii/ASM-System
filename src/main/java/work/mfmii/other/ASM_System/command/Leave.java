package work.mfmii.other.ASM_System.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import work.mfmii.other.ASM_System.utils.CommandManager;
import work.mfmii.other.ASM_System.utils.GuildUtil;
import work.mfmii.other.ASM_System.utils.LanguageUtil;

import java.util.ArrayList;
import java.util.List;

public class Leave extends CommandManager {
    public Leave(String name){
        super(name);
    }

    @Override
    public boolean execute(@NotNull User sender, @NotNull String command, @NotNull String[] args, @NotNull MessageReceivedEvent event) {
        LanguageUtil.Language lang = new LanguageUtil().getUserLanguage(sender);
        String targetId = event.isFromGuild() ? event.getGuild().getId(): null;
        int i = 0;//5 /1 .2
        List<String> reason = new ArrayList<>();
        if (args.length > i) {//5>0 /1 > 0 .2>0
            if (args[i].equalsIgnoreCase("-id") || args[i].equalsIgnoreCase("-target") || args[i].equalsIgnoreCase("-guild")) {
                if (args.length > ++i){//5>1 .2 > 1
                    targetId = args[i++];//[1]
                }else {
                    event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "")).queue();
                }
            }
            if (args[i].equalsIgnoreCase("-r") || args[i].equalsIgnoreCase("-reason")){//[2]
                for (int j = ++i; j < args.length; j++) {//3<5 //4<5
                    reason.add(args[j]);//[3] [4]
                }
            }
        }
        if (targetId == null){
            event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.leave.error.not-guild")).queue();
            return true;
        }
        if (targetId.matches("[0-9]{18}")){
            event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.leave.error.id-length")).queue();
            return true;
        }
        Guild g = event.getJDA().getGuildById(targetId);
        if (g == null) {
            event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "guild-not-found")).queue();
            return true;
        }

        event.getChannel().sendMessage(String.format(new LanguageUtil().getMessage(new GuildUtil(targetId).getLanguage(), "command.leave.success.channel"), String.join(" ", reason))).queue();

        g.leave().queue();
        return false;
    }
}
