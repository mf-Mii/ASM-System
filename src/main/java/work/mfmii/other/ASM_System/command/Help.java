package work.mfmii.other.ASM_System.command;

import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import work.mfmii.other.ASM_System.utils.CommandManager;

public class Help extends CommandManager {
    public Help(String name){
        super(name);
    }

    @Override
    public boolean execute(@NotNull User sender, @NotNull String command, @NotNull String[] args) {
        return true;
    }
}
