package work.mfmii.other.ASM_System.event;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.utils.EventManager;

public class ButtonAction extends EventManager {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    public ButtonAction(Class clazz){
        super(clazz);
    }

    @Override
    public boolean execute(@NotNull GenericEvent genericEvent) {
        ButtonClickEvent event = (ButtonClickEvent) genericEvent;
        logger.debug("ButtonID: "+(event.getButton().getId()==null?null:event.getButton().getId()));
        if (event.getButton().getId().equalsIgnoreCase("re_cmd:ping")){
            EmbedBuilder emBuild = new EmbedBuilder().setTitle("Ping").addField("GatewayPing", event.getJDA().getGatewayPing()+"ms", false);
            event.getInteraction().editMessageEmbeds(emBuild.build()).queue();
        }
        return false;
    }

}
