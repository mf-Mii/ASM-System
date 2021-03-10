package work.mfmii.other.ASM_System.utils;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import work.mfmii.other.ASM_System.Config;

import javax.print.DocFlavor;
import java.util.ArrayList;
import java.util.List;

public abstract class CommandManager {
    private String name;
    private String nextLabel;
    private String label;
    private CommandMap commandMap;

    protected CommandManager(@NotNull String name){
        this.name = name;
        this.nextLabel = name;
        this.label = name;
    }

    /**
     * Executes the command, returning its success
     *
     * @param sender Source object which is executing this command
     * @param command Command sender used as String
     * @param args All arguments passed to the command, split via ' '
     * @return true if the command was successful, otherwise false
     */
    public abstract boolean execute(@NotNull User sender, @NotNull String command, @NotNull String[] args, @NotNull MessageReceivedEvent event);

    public String getCommandsRaw(){
        return new FileUtil().readFile(new FileUtil().getFile("commands.json"), "utf8");
    }


    /**
     * Returns the name of this command
     *
     * @return Name of this command
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this command.
     * <p>
     * May only be used before registering the command.
     * Will return true if the new name is set, and false
     * if the command has already been registered.
     *
     * @param name New command name
     * @return returns true if the name change happened instantly or false if
     *     the command was already registered
     */
    public boolean setName(@NotNull String name) {
        if (!isRegistered()) {
            this.name = (name == null) ? "" : name;
            return true;
        }
        return false;
    }

    /**
     * Gets the permission required by users to be able to perform this
     * command
     *
     * @return Permission name, or null if none
     */
    @Nullable
    public String getPermission() {
        return new FileUtil().getStringFromJSON(new JSONObject(getCommandsRaw()), this.name.toLowerCase()+".permission");
    }

    /**
     * Registers this command to a CommandMap.
     * Once called it only allows changes the registered CommandMap
     *
     * @param commandMap the CommandMap to register this command to
     * @return true if the registration was successful (the current registered
     *     CommandMap was the passed CommandMap or null) false otherwise
     */
    public boolean register(@NotNull CommandMap commandMap) {
        if (allowChangesFrom(commandMap)) {
            this.commandMap = commandMap;
            return true;
        }

        return false;
    }


    /**
     * Unregisters this command from the passed CommandMap applying any
     * outstanding changes
     *
     * @param commandMap the CommandMap to unregister
     * @return true if the unregistration was successful (the current
     *     registered CommandMap was the passed CommandMap or null) false
     *     otherwise
     */
    public boolean unregister(@NotNull CommandMap commandMap) {
        if (allowChangesFrom(commandMap)) {
            this.commandMap = null;
            return true;
        }

        return false;
    }

    private boolean allowChangesFrom(@NotNull CommandMap commandMap) {
        return (null == this.commandMap || this.commandMap == commandMap);
    }

    /**
     * Returns the current registered state of this command
     *
     * @return true if this command is currently registered false otherwise
     */
    public boolean isRegistered() {
        return (null != this.commandMap);
    }

    /**
     * Returns the label for this command
     *
     * @return Label of this command
     */
    @NotNull
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label of this command.
     * <p>
     * May only be used before registering the command.
     * Will return true if the new name is set, and false
     * if the command has already been registered.
     *
     * @param name The command's name
     * @return returns true if the name change happened instantly or false if
     *     the command was already registered
     */
    public boolean setLabel(@NotNull String name) {
        if (name == null) {
            name = "";
        }
        this.nextLabel = name;
        if (!isRegistered()) {
            this.label = name;
            return true;
        }
        return false;
    }

    /**
     * Returns a list of active aliases of this command
     *
     * @return List of aliases
     */
    @NotNull
    public List<String> getAliases() {
        List<String> aliases = new ArrayList<>();
        new FileUtil().getJSONArrayFromJSON(new JSONObject(getCommandsRaw()), this.name.toLowerCase()+".aliases").forEach(o -> {
            aliases.add(o.toString());
        });
        return aliases;
    }

    /**
     * Returns a message to be displayed on a failed permission check for this
     * command
     *
     * @return Permission check failed message
     */
    @Nullable
    public String getPermissionMessage(LanguageUtil.Language lang) {
        String message = new LanguageUtil().getMessage(lang, "command."+this.name.toLowerCase()+".permissionMessage");
        message = message.replaceAll("\\$\\{default\\.permissionMessage\\}", new LanguageUtil().getMessage(lang, "default.permissionMessage"));
        return message;
    }

    /**
     * Gets a brief description of this command
     *
     * @return Description of this command
     */
    @NotNull
    public String getDescription(LanguageUtil.Language lang) {
        String description = new LanguageUtil().getMessage(lang, "command."+this.name.toLowerCase()+".description");
        description = description.replaceAll("\\$\\{default\\.description\\}", new FileUtil().getStringFromJSON(new JSONObject(getCommandsRaw()), this.name.toLowerCase()+".description"));
        return description;
    }

    /**
     * Gets a brief about of this command
     *
     * @return Description of this command
     */
    @NotNull
    public String getAbout(LanguageUtil.Language lang) {
        String about = new LanguageUtil().getMessage(lang, "command."+this.name.toLowerCase()+".about");
        about = about.replaceAll("\\$\\{default\\.about\\}", new FileUtil().getStringFromJSON(new JSONObject(getCommandsRaw()), this.name.toLowerCase()+".about"));
        return about;
    }


    /**
     * Gets an example usage of this command
     *
     * @return One or more example usages
     */
    @NotNull
    public String getUsage(LanguageUtil.Language lang) {
        String usage = new LanguageUtil().getMessage(lang, "command."+this.name.toLowerCase()+".usage");
        usage = usage.replaceAll("\\$\\{default\\.usage\\}", new FileUtil().getStringFromJSON(new JSONObject(getCommandsRaw()), this.name.toLowerCase()+".usage"));
        usage = usage.replaceAll("\\$\\{prefix\\}", new Config(Config.ConfigType.JSON).getString("prefix"));
        return usage;
    }


}
