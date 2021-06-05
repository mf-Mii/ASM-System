package work.mfmii.other.ASM_System.utils.slash;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.Event;

import javax.annotation.Nonnull;

public class SlashCommandEvent extends Event {
    private final SlashCommand slashCommand;

    public SlashCommandEvent(@Nonnull JDA jda, long responseNumber, SlashCommand slashCommand){
        super(jda, responseNumber);
        this.slashCommand = slashCommand;
    }

    public SlashCommand getSlashCommand() {
        return slashCommand;
    }

    public boolean isFromGuild() {
        return getSlashCommand().getRaw().has("guild_id");
    }
}
