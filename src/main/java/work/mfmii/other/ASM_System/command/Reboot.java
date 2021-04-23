package work.mfmii.other.ASM_System.command;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.ASMSystem;
import work.mfmii.other.ASM_System.Config;
import work.mfmii.other.ASM_System.utils.CommandManager;
import work.mfmii.other.ASM_System.utils.LanguageUtil;

import java.io.File;

public class Reboot extends CommandManager {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    public Reboot(String name){
        super(name);
    }

    @Override
    public boolean execute(@NotNull User sender, @NotNull String command, @NotNull String[] args, @NotNull MessageReceivedEvent event) {
        LanguageUtil.Language lang = new LanguageUtil().getUserLanguage(sender);
        if (args.length <= 1){
            if (new File(args.length == 0 ? new Config(Config.ConfigType.JSON).getString("start_script") : args[0]).isFile()){
                event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.reboot.success")).queue();
                restart(args.length == 0 ? new Config(Config.ConfigType.JSON).getString("start_script") : args[0]);
            }else {
                event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.reboot.error.file-notFound")).queue();
                restart(null);
            }
        } else {
            event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.reboot.error.args-length")).queue();
        }
        return true;
    }

    private static void restart(final String restartScript){
        ASMSystem.jda.shutdown();
        System.out.println("JDA shutdown...");
        if (restartScript != null) {
            Thread shutdownHook = new Thread() {
                @Override
                public void run() {
                    try {
                        String os = System.getProperty("os.name").toLowerCase(java.util.Locale.ENGLISH);
                        if (os.contains("win")) {
                            Runtime.getRuntime().exec("cmd /c start " + restartScript);
                        } else {
                            Runtime.getRuntime().exec("sh " + restartScript);
                        }
                        System.out.println("Running new process...");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            shutdownHook.setDaemon(true);
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }else {
            System.exit(0);
        }
    }
}
