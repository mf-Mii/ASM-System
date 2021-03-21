package work.mfmii.other.ASM_System.command;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import work.mfmii.other.ASM_System.ASMSystem;
import work.mfmii.other.ASM_System.Config;
import work.mfmii.other.ASM_System.utils.CommandManager;

import java.io.IOException;

public class Reboot extends CommandManager {
    public Reboot(String name){
        super(name);
    }

    @Override
    public boolean execute(@NotNull User sender, @NotNull String command, @NotNull String[] args, @NotNull MessageReceivedEvent event) {
        ASMSystem.jda.shutdownNow();
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec(new Config(Config.ConfigType.JSON).getString("boot_file"));

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(1);
        return true;
    }
}
