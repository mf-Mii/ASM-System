package work.mfmii.other.ASM_System.utils;

import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.print.DocFlavor;
import java.util.ArrayList;
import java.util.List;

public abstract class CommandManager {
    private String name;
    private String nextLabel;
    private String label;
    private String permission;
    private List<String> aliases;
    protected String about;
    protected String description;
    protected String usage;
    private String permissionMessage;
    private CommandMap commandMap;

    protected CommandManager(@NotNull String name){
        this(name, "", "", "/"+name, new ArrayList<String>());
    }

    protected CommandManager(@NotNull String name,@NotNull String about, @NotNull String description, @NotNull String usage, @NotNull List<String> aliases){
        this.name = name;
        this.nextLabel = name;
        this.label = name;
        this.about = about;
        this.description = description;
        this.usage = usage;
        this.aliases = aliases;
    }

    /**
     * Executes the command, returning its success
     *
     * @param sender Source object which is executing this command
     * @param command Command sender used as String
     * @param args All arguments passed to the command, split via ' '
     * @return true if the command was successful, otherwise false
     */
    public abstract boolean execute(@NotNull User sender, @NotNull String command, @NotNull String[] args);

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
        return permission;
    }

    /**
     * Sets the permission required by users to be able to perform this
     * command
     *
     * @param permission Permission name or null
     */
    public void setPermission(@Nullable String permission) {
        this.permission = permission;
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
        return aliases;
    }

    /**
     * Returns a message to be displayed on a failed permission check for this
     * command
     *
     * @return Permission check failed message
     */
    @Nullable
    public String getPermissionMessage() {
        return permissionMessage;
    }

    /**
     * Gets a brief description of this command
     *
     * @return Description of this command
     */
    @NotNull
    public String getDescription() {
        return description;
    }

    /**
     * Gets an example usage of this command
     *
     * @return One or more example usages
     */
    @NotNull
    public String getUsage() {
        return usage;
    }

    /**
     * Sets the list of aliases to request on registration for this command.
     *
     * @param aliases aliases to register to this command
     * @return this command object, for chaining
     */
    @NotNull
    public CommandManager setAliases(@NotNull List<String> aliases) {
        this.aliases = aliases;
        return this;
    }

    /**
     * Sets a brief description of this command.
     *
     * @param description new command description
     * @return this command object, for chaining
     */
    @NotNull
    public CommandManager setDescription(@NotNull String description) {
        this.description = (description == null) ? "" : description;
        return this;
    }

    /**
     * Sets the message sent when a permission check fails
     *
     * @param permissionMessage new permission message, null to indicate
     *     default message, or an empty string to indicate no message
     * @return this command object, for chaining
     */
    @NotNull
    public CommandManager setPermissionMessage(@Nullable String permissionMessage) {
        this.permissionMessage = permissionMessage;
        return this;
    }

    /**
     * Sets the example usage of this command
     *
     * @param usage new example usage
     * @return this command object, for chaining
     */
    @NotNull
    public CommandManager setUsage(@NotNull String usage) {
        this.usage = (usage == null) ? "" : usage;
        return this;
    }
}
