package work.mfmii.other.ASM_System;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class ASMSystem {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    public static JDA jda;
    public static void main(String[] args) throws LoginException, InterruptedException {

        jda = JDABuilder.createDefault(new Config(Config.ConfigType.JSON).getString("token"))
                .addEventListeners(new Listener())
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .enableIntents(GatewayIntent.GUILD_PRESENCES)
                .setStatus(OnlineStatus.ONLINE)
                .build();
        jda.awaitReady();
        System.out.println("Logged in as "+jda.getSelfUser().getAsTag());
        jda.getPresence().setActivity(Activity.playing(
                new Config(Config.ConfigType.JSON).getString("activity.name").replaceAll("\\$\\{guilds\\.count\\}",  String.valueOf(jda.getGuilds().size())).replaceAll("\\$\\{members\\.count\\}", String.valueOf(jda.getUsers().size()))
        ));
        new StartUp();
    }
}
