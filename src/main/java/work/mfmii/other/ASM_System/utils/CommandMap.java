package work.mfmii.other.ASM_System.utils;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class CommandMap {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final Pattern PATTERN_ON_SPACE = Pattern.compile(" ", Pattern.LITERAL);
    protected static final Map<String, CommandManager> knownCommands = new HashMap<>();

    public void registerAll(@NotNull String prefix, @NotNull List<CommandManager> commands){
        if(commands!=null){
            for (CommandManager c : commands) {
                register(prefix, c);
            }
        }
    }

    public boolean register(@NotNull CommandManager command){
        return register(command.getName(), command);
    }

    public boolean register(@NotNull String label, @NotNull CommandManager command){
        label = label.toLowerCase().trim();
        boolean registered = register(label, command, false);
        Iterator<String> iterator = command.getAliases().iterator();
        while (iterator.hasNext()){
            if(!register(iterator.next(), command, true)){// if false
                iterator.remove();
            }
        }

        if(!registered){
            command.setLabel(label);
        }

        command.register(this);

        return registered;
    }

    public synchronized boolean register(@NotNull String label, @NotNull CommandManager command, boolean isAlias){
        knownCommands.put(label, command);
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

    public boolean dispatch(User sender, String commandLine, MessageReceivedEvent event, boolean isAdminPrefix) {
        //String[] args = PATTERN_ON_SPACE.split(commandLine);
        String[] args = commandLine.split(" ");
        if (args.length == 0) {
            return false;
        }

        String sentCommandLabel = args[0].toLowerCase();
        CommandManager target = getCommand(sentCommandLabel);
        if(target == null){
            return false;
        }
        if (new PermissionUtil().hasPermission(event.isFromGuild()?event.getGuild().getId():null, event.getChannel().getId(), sender.getId(), target.getPermission())) {
            if (!target.isPrefixCommand()){
                event.getMessage().reply(new LanguageUtil().getMessage(new LanguageUtil().getUserLanguage(sender), "command.error-msg.prefix-disallow.message")).complete().delete().queueAfter(5, TimeUnit.SECONDS);
                event.getMessage().delete().queueAfter(5, TimeUnit.SECONDS);
                return true;
            }
            try {
                if (target.isAdminCommand()) {
                    if(isAdminPrefix)
                        target.execute(sender, sentCommandLabel, Arrays.copyOfRange(args, 1, args.length), event);
                    else
                        event.getChannel().sendMessage(target.getPermissionMessage(new LanguageUtil().getUserLanguage(sender))).queue();
                }else {
                    if (!isAdminPrefix)
                        target.execute(sender, sentCommandLabel, Arrays.copyOfRange(args, 1, args.length), event);
                    else return false;
                }
            } catch (Throwable ex) {
                System.out.println("Unhandled exception executing '" + commandLine + "' in " + target + ex.getMessage());
                ex.printStackTrace();
            }
        }else {
            event.getChannel().sendMessage(target.getPermissionMessage(new LanguageUtil().getUserLanguage(sender))).queue();
        }

        // return true as command was handled
        return true;
    }

    public boolean dispatchSlash(User sender, String name, SlashCommandEvent event){
        CommandManager target = getCommand(name);
        if(target == null){
            return false;
        }
        if (new PermissionUtil().hasPermission(event.isFromGuild()?event.getGuild().getId():null, event.getChannel().getId(), sender.getId(), target.getPermission())) {
            if (!target.isSlashCommand()){
                event.reply(new LanguageUtil().getMessage(new LanguageUtil().getUserLanguage(sender), "command.error-msg.slash-disallow.message"))
                        .addActionRow(Button.primary(
                                "cmd:command "+target.getName(),
                                new LanguageUtil().getMessage(new LanguageUtil().getUserLanguage(sender), "command.error-msg.slash-disallow.button")))
                        .setEphemeral(true).queue();
                return true;
            }
            try {
                target.executeSlash(sender, name, event);
            } catch (Throwable ex) {
                System.out.println("Unhandled exception executing '/" + name + "' in " + target + ex.getMessage());
                ex.printStackTrace();
            }
        }else {
            event.reply(target.getPermissionMessage(new LanguageUtil().getUserLanguage(sender))).setEphemeral(true).queue();
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

    public Collection<CommandManager> getCommands() {
        return Collections.unmodifiableCollection(knownCommands.values());
    }

}
