package work.mfmii.other.ASM_System.event.guild;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import org.jetbrains.annotations.NotNull;
import work.mfmii.other.ASM_System.utils.EventManager;
import work.mfmii.other.ASM_System.utils.GuildUtil;
import work.mfmii.other.ASM_System.utils.LanguageUtil;

public class GuildJoin extends EventManager {
    public GuildJoin(@NotNull Class eventType){
        super(eventType);
    }

    @Override
    public boolean execute(@NotNull GenericEvent genericEvent) {
        if (genericEvent instanceof GuildJoinEvent){
            GuildJoinEvent event = (GuildJoinEvent) genericEvent;
            Guild guild = event.getGuild();
            if (guild.getDefaultChannel() != null && guild.getDefaultChannel().canTalk()) {
                guild.getDefaultChannel().sendMessage(new LanguageUtil().getMessage(LanguageUtil.Language.fromKey(guild.getLocale().getLanguage()), "event.GuildJoin.defaultChannelMessage")).queue();
            }
            new GuildUtil(guild.getId()).register(guild.getLocale().getLanguage());
        }
        return false;
    }
}

