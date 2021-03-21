package work.mfmii.other.ASM_System;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import work.mfmii.other.ASM_System.command.Help;
import work.mfmii.other.ASM_System.command.Reboot;
import work.mfmii.other.ASM_System.command.UserInfo;
import work.mfmii.other.ASM_System.utils.CommandMap;

import javax.security.auth.login.LoginException;

public class ASMSystem {
    public static JDA jda;
    public static void main(String[] args) throws LoginException {

        jda = JDABuilder.createDefault(new Config(Config.ConfigType.JSON).getString("token"))
                .addEventListeners(new Listener())
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .enableIntents(GatewayIntent.GUILD_PRESENCES)
                .setStatus(OnlineStatus.ONLINE)
                .build();
        try{
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
        jda.getPresence().setActivity(Activity.playing(
                new Config(Config.ConfigType.JSON).getString("activity.name").replaceAll("\\$\\{guilds\\.count\\}",  String.valueOf(jda.getGuilds().size())).replaceAll("\\$\\{members\\.count\\}", String.valueOf(jda.getUsers().size()))
        ));
        new CommandMap().register(new Help("help"));
        new CommandMap().register(new UserInfo("userinfo"));
        new CommandMap().register(new Reboot("reboot"));
    }
}
