package work.mfmii.other.ASM_System.utils;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;

public class EventMap {
    private static final Pattern PATTERN_ON_SPACE = Pattern.compile(" ", Pattern.LITERAL);
    protected static final List<Map<Class, EventManager>> registeredEventListeners = new ArrayList<>();

    public boolean register(@NotNull EventManager eventManager){
        boolean registered = register(eventManager.getEventType(), eventManager);

        eventManager.register(this);

        return registered;
    }

    public synchronized boolean register(@NotNull Class eventType, @NotNull EventManager eventManager){
        Map<Class, EventManager> _mng = new HashMap<>();
        _mng.put(eventType, eventManager);
        registeredEventListeners.add(_mng);

        return true;
    }

    public boolean dispatch(@NotNull Class eventType, User sender, @NotNull Event event) {
        //String[] args = PATTERN_ON_SPACE.split(commandLine);

        List<EventManager> targets = getListeners(eventType);
        if(targets == null || targets.isEmpty()){
            return false;
        }
        for (EventManager target: targets) {
            try {
                target.execute(sender, event);
            } catch (Throwable ex) {
                System.out.println("Unhandled exception executing '" + target.getEventType().getName() + "' in " + target + ex.getMessage());
                ex.printStackTrace();
            }
        }

        // return true as command was handled
        return true;
    }

    public synchronized void clearListeners() {
        for (Map<Class, EventManager> _map: registeredEventListeners) {
            for (Map.Entry<Class, EventManager> entry : _map.entrySet()) {
                entry.getValue().unregister(this);
            }
        }
        registeredEventListeners.clear();
    }

    public List<EventManager> getListeners(Class eventType) {
        List<EventManager> _temp = new ArrayList<>();
        for (Map<Class, EventManager> _map: registeredEventListeners) {
            _temp.add(_map.get(eventType));
            _map.values();
        }
        return _temp;
    }

    public ListIterator<Map<Class, EventManager>> getListeners() {
        return registeredEventListeners.listIterator();
    }
}
