package work.mfmii.other.ASM_System.event;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.utils.CommandMap;
import work.mfmii.other.ASM_System.utils.EventManager;

import javax.annotation.Nonnull;

public class SlashCommandReceived extends EventManager {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    public SlashCommandReceived(@Nonnull Class type){
        super(type);
    }

    @Override
    public boolean execute(@NotNull GenericEvent genericEvent) {
        SlashCommandEvent event = (SlashCommandEvent) genericEvent;
        new CommandMap().dispatchSlash(event.getUser(), event.getName(), event);

        if (event.getName().equalsIgnoreCase("ping")){
            EmbedBuilder emBuild = new EmbedBuilder().setTitle("Ping").addField("GatewayPing", event.getJDA().getGatewayPing()+"ms", false);
            Button button = Button.secondary("re_cmd:ping", "Retest");
            event.replyEmbeds(emBuild.build()).setEphemeral(true).addActionRow(button).queue();
        }
        return false;
    }
}
