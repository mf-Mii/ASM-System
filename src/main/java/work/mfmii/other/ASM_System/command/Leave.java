package work.mfmii.other.ASM_System.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.Config;
import work.mfmii.other.ASM_System.utils.CommandManager;
import work.mfmii.other.ASM_System.utils.GuildUtil;
import work.mfmii.other.ASM_System.utils.LanguageUtil;

import java.util.ArrayList;
import java.util.List;

public class Leave extends CommandManager {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    private boolean debug = false;
    public Leave(String name){
        super(name);
    }

    @Override
    public boolean execute(@NotNull User sender, @NotNull String command, @NotNull String[] args, @NotNull MessageReceivedEvent event) {
        debug = new Config(Config.ConfigType.JSON).isDebugMode();
        LanguageUtil.Language lang = new LanguageUtil().getUserLanguage(sender);
        String targetId = event.isFromGuild() ? event.getGuild().getId(): null;
        int i = 0;//5 /1 .2
        List<String> reason = new ArrayList<>();
        if (args.length > i) {//5>0 /1 > 0 .2>0
            if (args[i].equalsIgnoreCase("-id") || args[i].equalsIgnoreCase("-target") || args[i].equalsIgnoreCase("-guild")) {
                if(debug)System.out.println("targetId select");
                if (args.length > ++i){//5>1 .2 > 1
                    targetId = args[i++];//[1]
                    if(debug)System.out.println("targetId set.");
                }else {
                    event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.leave.error.id-length")).queue();
                    if(debug)System.out.println("There isn't target id");
                }
            }
            if (args[i].equalsIgnoreCase("-r") || args[i].equalsIgnoreCase("-reason")){//[2]
                if(debug)System.out.println("reason set.");
                for (int j = ++i; j < args.length; j++) {//3<5 //4<5
                    reason.add(args[j]);//[3] [4]
                }
            }
        }
        if (reason.isEmpty()){
            reason.add("__なし__");
        }
        if (targetId == null){
            event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.leave.error.not-guild")).queue();
            if(debug)System.out.println("targetId is null");
            return true;
        }
        if(debug)System.out.println(targetId);
        if (!targetId.matches("[0-9]{18}")){
            event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.leave.error.id-length")).queue();
            if(debug)System.out.println("targetId length is not 18");
            return true;
        }
        Guild g = event.getJDA().getGuildById(targetId);
        if (g == null) {
            event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.leave.error.guild-not-found")).queue();
            if(debug)System.out.println("guild not found.");
            return true;
        }
        if(debug){
            System.out.println("GuildLang: "+new GuildUtil(targetId).getLanguage()+"("+new GuildUtil(targetId).getLanguage().getKey()+")");
        }
        try {
            event.getChannel().sendMessage(String.format(new LanguageUtil().getMessage(new GuildUtil(targetId).getLanguage(), "command.leave.success.channel"), String.join(" ", reason))).queue();
        } catch (Exception ignored) {
        }
        g.leave().queue();
        return false;
    }
}
