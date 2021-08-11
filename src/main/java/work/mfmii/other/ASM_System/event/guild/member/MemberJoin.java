package work.mfmii.other.ASM_System.event.guild.member;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import org.jetbrains.annotations.NotNull;
import work.mfmii.other.ASM_System.utils.EventManager;
import work.mfmii.other.ASM_System.utils.GuildUtil;
import work.mfmii.other.ASM_System.utils.LanguageUtil;
import work.mfmii.other.ASM_System.utils.UserUtil;

import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MemberJoin extends EventManager {
    public MemberJoin(Class type){
        super(type);
    }
    @Override
    public boolean execute(@NotNull GenericEvent genericEvent) throws Exception {
        if (genericEvent instanceof GuildMemberJoinEvent){
            GuildMemberJoinEvent event = (GuildMemberJoinEvent) genericEvent;
            double member_rep = new UserUtil(event.getUser()).getReputation();
            double guild_rep = new GuildUtil(event.getGuild()).getMinMemberReputation();
            LanguageUtil.Language lang = new LanguageUtil().getUserLanguage(event.getUser());
            if (guild_rep > member_rep) {
                try {
                    sendDenyMessage(event.getGuild(), event.getUser(), lang);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }

            return true;
        }else return false;
    }

    private void sendDenyMessage(@NotNull Guild guild, @NotNull User user, @NotNull LanguageUtil.Language lang) throws Exception {
        double guild_rep = new GuildUtil(guild).getReputation();
        double guild_req_rep = new GuildUtil(guild).getMinMemberReputation();
        double user_rep = new UserUtil(user).getReputation();

        List<String> user_rep_log_str_array = new ArrayList<>();
        //for (UserUtil.Reputation reputation : new UserUtil(user).getReputationLog()) {
        for (int i = 0; i < new UserUtil(user).getReputationLog().size(); i++) {
            UserUtil.Reputation reputation = new UserUtil(user).getReputationLog().get(i);
            if (i < 5){
                StringBuilder _rep_build = new StringBuilder();
                boolean rep_down = reputation.getFrom_val() > reputation.getVal();
                _rep_build.append("`");
                _rep_build.append(rep_down?"⬇":"⬆");
                _rep_build.append(String.format(" [%s → %s]", reputation.getFrom_val(), reputation.getVal()));
                _rep_build.append(" ");
                _rep_build.append(String.format("%s - %s", reputation.getReason(), reputation.getDateTime().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))));
                _rep_build.append("`");
                user_rep_log_str_array.add(_rep_build.toString());
            }
        }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(new LanguageUtil().getMessage(lang, "event.GuildMemberJoin.denied.embed.title")
                .replaceAll("\\$\\{reason\\}", new LanguageUtil().getMessage(lang, "event.GuildMemberJoin.reputation_not_enough-DM.name")));
        eb.setDescription(new LanguageUtil().getMessage(lang, "event.GuildMemberJoin.reputation_not_enough-DM.desc"));
        eb.setColor(Color.RED);
        eb.addField(new LanguageUtil().getMessage(lang, "event.GuildMemberJoin.denied.embed.fields.guild_rep"), guild_rep+" / 10.0", true);
        eb.addField(new LanguageUtil().getMessage(lang, "event.GuildMemberJoin.denied.embed.fields.guild_req_rep"), guild_req_rep+"≦", true);
        eb.addField(new LanguageUtil().getMessage(lang, "event.GuildMemberJoin.denied.embed.fields.user_rep"), user+" / 10.0", true);
        eb.addField(new LanguageUtil().getMessage(lang, "event.GuildMemberJoin.denied.embed.fields.user_rep_log"), String.join("\n", user_rep_log_str_array), false);
        user.openPrivateChannel().complete()
                .sendMessage(new LanguageUtil().getMessage(lang, "event.GuildMemberJoin.denied.text".replaceAll("\\$\\{guild\\}", guild.getName()))).queue();
    }
}
