package work.mfmii.other.ASM_System.utils;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public abstract class EventManager {
    private Class eventType;
    private EventMap eventMap;

    protected EventManager(@NotNull Class<?> event){
        this.eventType = event;
    }

    /**
     * Executes the command, returning its success
     *
     * @param sender Source object which is executing this command
     * @param event Event from JDA
     * @return true if the event executing was successful, otherwise false
     */
    public abstract boolean execute(User sender, @NotNull Event event);

    public String getEventsRaw(){
        return new FileUtil().readFile(new FileUtil().getFile("events.json"), "utf8");
    }

    /**
     * Gets the permission required by users to be able to perform this
     * command
     *
     * @return Permission name, or null if none
     */
    @Nullable
    public String getPermission() {
        return new FileUtil().getStringFromJSON(new JSONObject(getEventsRaw()), this.eventType.getSimpleName().toLowerCase()+".permission");
    }

    /**
     * Registers this event to a EventMap.
     * Once called it only allows changes the registered EventMap
     *
     * @param eventMap the CommandMap to register this event to
     * @return true if the registration was successful (the current registered
     *     EventMap was the passed EventMap or null) false otherwise
     */
    public boolean register(@NotNull EventMap eventMap) {
        if (allowChangesFrom(eventMap)) {
            this.eventMap = eventMap;
            return true;
        }

        return false;
    }


    /**
     * Unregisters this event from the passed EventMap applying any
     * outstanding changes
     *
     * @param eventMap the CommandMap to unregister
     * @return true if the unregistration was successful (the current
     *     registered CommandMap was the passed CommandMap or null) false
     *     otherwise
     */
    public boolean unregister(@NotNull EventMap eventMap) {
        if (allowChangesFrom(eventMap)) {
            this.eventMap = null;
            return true;
        }

        return false;
    }

    private boolean allowChangesFrom(@NotNull EventMap eventMap) {
        return (null == this.eventMap || this.eventMap == eventMap);
    }

    /**
     * Returns the current registered state of this event
     *
     * @return true if this event is currently registered false otherwise
     */
    public boolean isRegistered() {
        return (null != this.eventMap);
    }


    /**
     * Returns a message to be displayed on a failed permission check for this
     * command
     *
     * @return Permission check failed message
     */
    @Nullable
    public String getPermissionMessage(LanguageUtil.Language lang) {
        String message = new LanguageUtil().getMessage(lang, "event."+this.eventType.getSimpleName().toLowerCase()+".permissionMessage");
        message = message.replaceAll("\\$\\{default\\}", new LanguageUtil().getMessage(lang, "default.eventPermissionMessage"));
        return message;
    }

}
