package work.mfmii.other.ASM_System.utils;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import work.mfmii.other.ASM_System.Config;

import java.util.*;
import java.util.regex.Pattern;

public class CommandMap {
    private static final Pattern PATTERN_ON_SPACE = Pattern.compile(" ", Pattern.LITERAL);
    protected final Map<String, CommandManager> knownCommands = new HashMap<>();

    public void registerAll(@NotNull String prefix, @NotNull List<CommandManager> commands){
        if(commands!=null){
            for (CommandManager c : commands) {
                register(prefix, c);
            }
        }
    }

    public boolean register(@NotNull String prefix, @NotNull CommandManager command){
        return register(command.getName(), prefix, command);
    }

    public boolean register(@NotNull String label, @NotNull String prefix, @NotNull CommandManager command){
        label = label.toLowerCase().trim();
        prefix = prefix.toLowerCase().trim();
        boolean registered = register(label, command, false, prefix);
        Iterator<String> iterator = command.getAliases().iterator();
        while (iterator.hasNext()){
            if(!register(iterator.next(), command, true, prefix)){// if false
                iterator.remove();
            }
        }

        if(!registered){
            command.setLabel(prefix+":"+label);
        }

        command.register(this);

        return registered;
    }

    public synchronized boolean register(@NotNull String label, @NotNull CommandManager command, boolean isAlias, @NotNull String prefix){
        knownCommands.put(prefix+":"+label, command);
        if(isAlias && knownCommands.containsKey(label)){
            return false;
        }

        boolean registered = true;
        //エイリアスじゃなくて本物なの？っていう感じなのかそうじゃないのか…
        CommandManager conflict = knownCommands.get(label);
        if(conflict != null && conflict.getLabel().equals(label)){
            return false;
        }

        if(!isAlias){
            command.setLabel(label);
        }
        knownCommands.put(label, command);

        return registered;
    }

    public boolean dispatch(User sender, String commandLine, GenericEvent event) {
        String[] args = PATTERN_ON_SPACE.split(commandLine);

        if (args.length == 0) {
            return false;
        }

        String sentCommandLabel = args[0].toLowerCase();
        CommandManager target = getCommand(sentCommandLabel);

        if (target == null) {
            return false;
        }

        try {
            target.execute(sender, sentCommandLabel, Arrays.copyOfRange(args, 1, args.length), event);
        } catch (Throwable ex) {
            System.out.println("Unhandled exception executing '" + commandLine + "' in " + target+ex.getMessage());
        }

        // return true as command was handled
        return true;
    }

    public synchronized void clearCommands() {
        for (Map.Entry<String, CommandManager> entry : knownCommands.entrySet()) {
            entry.getValue().unregister(this);
        }
        knownCommands.clear();
    }

    public CommandManager getCommand(String name) {
        CommandManager target = knownCommands.get(name.toLowerCase());
        return target;
    }

}
