package work.mfmii.other.ASM_System.utils;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

public class EventMap {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final Pattern PATTERN_ON_SPACE = Pattern.compile(" ", Pattern.LITERAL);
    protected static final Map<Class, List<EventManager>> registeredEventListeners = new HashMap<>();

    public boolean register(@NotNull EventManager eventManager){
        boolean registered = register(eventManager.getEventType(), eventManager);

        eventManager.register(this);

        return registered;
    }

    public synchronized boolean register(@NotNull Class eventType, @NotNull EventManager eventManager){
        List<EventManager> _managers;
        if (registeredEventListeners.containsKey(eventType)) {
            _managers = registeredEventListeners.get(eventType);
            _managers.add(eventManager);
            registeredEventListeners.put(eventType, _managers);
        } else {
            _managers = new ArrayList<>();
            _managers.add(eventManager);
            registeredEventListeners.put(eventType, _managers);
        }

        return true;
    }

    public boolean dispatch(@NotNull Class eventType, @NotNull GenericEvent event) {
        //String[] args = PATTERN_ON_SPACE.split(commandLine);

        List<EventManager> targets = getEventListeners(eventType);
        if(targets == null || targets.isEmpty()){
            return false;
        }
        for (EventManager target: targets) {
            try {
                target.execute(event);
            } catch (Throwable ex) {
                logger.warn("Unhandled exception executing '" + target.getEventType().getName() + "' in " + target + ex.getMessage());
                ex.printStackTrace();
            }
        }

        // return true as command was handled
        return true;
    }

    public synchronized void clearListeners() {
        registeredEventListeners.forEach((type, eventManagers) -> {
            eventManagers.forEach(listener -> {
                listener.unregister(this);
            });
        });
        registeredEventListeners.clear();
    }

    public List<EventManager> getEventListeners(Class eventType) {
        if (registeredEventListeners.containsKey(eventType))
            return registeredEventListeners.get(eventType);
        else return null;
    }

    public Map<Class, List<EventManager>> getAllEventListeners() {
        return registeredEventListeners;
    }
}
