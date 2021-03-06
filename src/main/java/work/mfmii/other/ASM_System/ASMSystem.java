package work.mfmii.other.ASM_System;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;

public class ASMSystem {
    public static JDA jda;
    public static void main(String[] args) throws LoginException {
        jda = JDABuilder.createDefault(new Config(Config.ConfigType.JSON).getString("token"))
                .setActivity(Activity.playing("ASM"))
                .setStatus(OnlineStatus.ONLINE)
                .build();
    }
}
