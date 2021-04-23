package work.mfmii.other.ASM_System.event;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.Config;
import work.mfmii.other.ASM_System.utils.CommandManager;
import work.mfmii.other.ASM_System.utils.CommandMap;
import work.mfmii.other.ASM_System.utils.EventManager;
import work.mfmii.other.ASM_System.utils.LanguageUtil;

import javax.annotation.Nonnull;

public class MessageReceived extends EventManager {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    public MessageReceived(@Nonnull Class type){
        super(type);
    }

    @Override
    public boolean execute(@NotNull GenericEvent genericEvent) {
        if (genericEvent instanceof MessageReceivedEvent) {



            MessageReceivedEvent event = (MessageReceivedEvent) genericEvent;

            if (event.getMessage().getContentRaw().startsWith(new Config(Config.ConfigType.JSON).getString("prefix"))) {
                boolean exec_res = new CommandMap().dispatch(event.getAuthor(), event.getMessage().getContentRaw().replaceFirst(new Config(Config.ConfigType.JSON).getString("prefix"), ""), event, false);
                if (!exec_res) {
                    event.getChannel().sendMessage(new LanguageUtil().getMessage(new LanguageUtil().getUserLanguage(event.getAuthor()), "command.unknown")).queue();
                }
            } else if (event.getMessage().getContentRaw().startsWith(new Config(Config.ConfigType.JSON).getString("admin.prefix"))) {
                boolean exec_res = new CommandMap().dispatch(event.getAuthor(), event.getMessage().getContentRaw().replaceFirst(new Config(Config.ConfigType.JSON).getString("admin.prefix"), ""), event, true);
                if (!exec_res) {
                    event.getChannel().sendMessage(new LanguageUtil().getMessage(new LanguageUtil().getUserLanguage(event.getAuthor()), "command.unknown")).queue();
                }
            }
            return true;
        }else
        return false;
    }
}
