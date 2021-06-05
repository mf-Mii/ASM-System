package work.mfmii.other.ASM_System.event;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.utils.EventManager;
import work.mfmii.other.ASM_System.utils.slash.SlashCommand;
import work.mfmii.other.ASM_System.utils.slash.SlashCommandEvent;

import javax.annotation.Nonnull;

public class SlashCommandReceived extends EventManager {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    public SlashCommandReceived(@Nonnull Class type){
        super(type);
    }

    @Override
    public boolean execute(@NotNull GenericEvent genericEvent) {
        try {
            SlashCommandEvent event = (SlashCommandEvent) genericEvent;
            SlashCommand command = event.getSlashCommand();
            logger.debug(event.getSlashCommand().getRaw().toString());
            if (command.getName().equalsIgnoreCase("ping")) {
                EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Ping").addField("GatewayPing", event.getJDA().getGatewayPing()+"ms", false);
                command.replyMessage(SlashCommand.replyType.CHANNEL_MESSAGE_WITH_SOURCE,
                        null, embedBuilder.build(), false);
            }
            return true;
        } catch (Exception e){
            ((SlashCommandEvent) genericEvent).getSlashCommand().getChannel().sendMessage("Error!!\n```"+e.getMessage()+"```").queue();
            e.printStackTrace();
            return false;
        }
    }
}
