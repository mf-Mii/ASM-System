package work.mfmii.other.ASM_System.command;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import work.mfmii.other.ASM_System.utils.CommandManager;
import work.mfmii.other.ASM_System.utils.PermissionUtil;

public class UserInfo extends CommandManager {
    public UserInfo(String name){
        super(name);
    }

    @Override
    public boolean execute(@NotNull User sender, @NotNull String command, @NotNull String[] args, @NotNull MessageReceivedEvent event) {
        if(command.equalsIgnoreCase("userinfo") || command.equalsIgnoreCase("user")){
            System.out.println("OK");
            StringBuilder output = new StringBuilder();
            for (int i = 0; i < args.length; i++) {
                System.out.println(i+":"+args[i]);
                if(args[i].equalsIgnoreCase("-p") || args[i].equalsIgnoreCase("-perm") || args[i].equalsIgnoreCase("-permission")){
                    System.out.println("having Parameter");
                    output.append("PermissionInfo\n");
                    output.append(args[++i]);
                    output.append(": ");
                    output.append(new PermissionUtil().hasPermission(null,null,sender.getId(), args[i]));
                    output.append("\n");
                }
            }
            event.getChannel().sendMessage(output.toString()).queue();
            return true;
        }
        return false;
    }
}
