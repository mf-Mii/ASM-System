package work.mfmii.other.ASM_System.command;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import work.mfmii.other.ASM_System.ASMSystem;
import work.mfmii.other.ASM_System.Config;
import work.mfmii.other.ASM_System.utils.CommandManager;
import work.mfmii.other.ASM_System.utils.LanguageUtil;

import java.io.File;

public class Shutdown extends CommandManager {
    public Shutdown(String name){
        super(name);
    }

    @Override
    public boolean execute(@NotNull User sender, @NotNull String command, @NotNull String[] args, @NotNull MessageReceivedEvent event) {
        LanguageUtil.Language lang = new LanguageUtil().getUserLanguage(sender);
        event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.shutdown.message")).queue();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        shutdown();
        return true;
    }

    private static void shutdown(){
        ASMSystem.jda.shutdown();
        System.out.println("JDA shutdown...");
        System.exit(0);
    }
}
