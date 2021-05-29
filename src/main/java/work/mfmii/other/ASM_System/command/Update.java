package work.mfmii.other.ASM_System.command;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.ASMSystem;
import work.mfmii.other.ASM_System.Config;
import work.mfmii.other.ASM_System.utils.CommandManager;
import work.mfmii.other.ASM_System.utils.FileUtil;
import work.mfmii.other.ASM_System.utils.LanguageUtil;

import javax.annotation.Nonnull;
import java.io.File;

public class Update extends CommandManager {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    public Update(String name){
        super(name);
    }

    @Override
    public boolean execute(@NotNull User sender, @NotNull String command, @NotNull String[] args, @NotNull MessageReceivedEvent event) {
        LanguageUtil.Language lang = new LanguageUtil().getUserLanguage(sender);
        if (args.length >= 1){
            event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.update.error.args-length")).queue();
            return true;
        }
        if (new FileUtil().getFile("update/").listFiles().length == 1){
            File file = null;
            for (File f : new FileUtil().getFile("update/").listFiles()) {
                file = f;
            }
            if (file==null){
                event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.update.error.update-file")).queue();
            }else {
                event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.update.success")).queue();
                update(new Config(Config.ConfigType.JSON).getString("update_script"), file.getName());
            }
        }else {
            event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.update.error.update-file")).queue();
        }
        return true;
    }

    private static void update(final String updateScript, final @Nonnull String updateFile){
        ASMSystem.jda.shutdown();
        System.out.println("JDA shutdown...");
        String os = System.getProperty("os.name").toLowerCase(java.util.Locale.ENGLISH);
        if(os.contains("win")){
            StringBuilder sb1 = new StringBuilder();
            sb1.append("@echo off");
            sb1.append("\njava -jar "+updateFile);
            sb1.append("\nexit");
            new FileUtil().writeFile(new Config(Config.ConfigType.JSON).getString("start_script"), sb1.toString(), false);
        }else {
            String s1 = "java -jar "+updateFile;
            new FileUtil().writeFile(updateScript, s1, false);
        }
        if (updateScript != null) {
            Thread shutdownHook = new Thread() {
                @Override
                public void run() {
                    try {
                        String executable = new File(ASMSystem.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
                        if (os.contains("win")) {
                            Runtime.getRuntime().exec("cmd /c start " + updateScript + " "+executable+" "+updateFile);
                        } else {
                            Runtime.getRuntime().exec("sh " + updateScript + " "+ updateFile);
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
