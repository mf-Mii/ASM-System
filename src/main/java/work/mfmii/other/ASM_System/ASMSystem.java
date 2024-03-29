package work.mfmii.other.ASM_System;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class ASMSystem {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    public static JDA jda;
    public static void main(String[] args) throws LoginException, InterruptedException {
        Logger logger = LoggerFactory.getLogger(ASMSystem.class);
        logger.info("ASMBot is booting...");
        logger.info("=====[Bot Info]=====");
        logger.info("Name: ASMBot");
        logger.info("Version: "+new Config(Config.ConfigType.JSON).getVersion());
        logger.info("Author: mf_Mii");
        logger.info("Licence: PRIVATE_ONLY");
        logger.info("Website: "+new Config(Config.ConfigType.JSON).getString("website")+"/bot");
        logger.info("Help: "+new Config(Config.ConfigType.JSON).getString("website")+"/admin/help/bot/");
        logger.info("=====================");
        jda = JDABuilder.createDefault(new Config(Config.ConfigType.JSON).getString("token"))
                .addEventListeners(new Listener())
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableCache(CacheFlag.ONLINE_STATUS)
                .enableCache(CacheFlag.CLIENT_STATUS)
                .enableCache(CacheFlag.ACTIVITY)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .enableIntents(GatewayIntent.GUILD_PRESENCES)
                .setStatus(OnlineStatus.ONLINE)
                .setRawEventsEnabled(true)
                .build();
        jda.awaitReady();
        logger.info("Logged in as "+jda.getSelfUser().getAsTag()+"("+ jda.getSelfUser().getId()+")");
        jda.getPresence().setActivity(Activity.playing(
                new Config(Config.ConfigType.JSON).getString("activity.name").replaceAll("\\$\\{guilds\\.count\\}",  String.valueOf(jda.getGuilds().size())).replaceAll("\\$\\{members\\.count\\}", String.valueOf(jda.getUsers().size()))
        ));
        new StartUp();
    }
}
