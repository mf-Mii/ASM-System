package work.mfmii.other.ASM_System.utils;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CommandMap {
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
}
