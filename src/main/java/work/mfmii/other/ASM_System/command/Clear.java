package work.mfmii.other.ASM_System.command;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.utils.CommandManager;
import work.mfmii.other.ASM_System.utils.LanguageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Clear extends CommandManager {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    public Clear(String name){
        super(name);
    }

    @Override
    public boolean execute(@NotNull User sender, @NotNull String command, @NotNull String[] args, @NotNull MessageReceivedEvent event) {
        LanguageUtil.Language lang = new LanguageUtil().getUserLanguage(sender);
        if (!event.isFromGuild()){
            event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.clear.error.guild")).queue();
            return true;
        }
        if (args.length == 0) {
            event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.clear.error.not-selected")).queue();
            return true;
        }
        if (!event.getChannelType().equals(ChannelType.TEXT)) {
            event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.clear.error.text-ch")).queue();
            return true;
        }
        List<List<Message>> messages = new ArrayList<>();
        AtomicInteger all_size = new AtomicInteger();
        TextChannel textChannel = event.getTextChannel();
        if (args[0].matches("^[0-9]{18}")){
            logger.debug("id: "+args[0]);
            logger.debug("MessageIsNull: "+(event.getTextChannel().retrieveMessageById(args[0]).complete()==null?"true":"false"));
            logger.debug(String.format("MessageID/Raw: %s/%s", event.getTextChannel().retrieveMessageById(args[0]).complete().getId(), event.getTextChannel().retrieveMessageById(args[0]).complete().getContentRaw()));
            AtomicReference<Message> last_msg = new AtomicReference<>(event.getTextChannel().retrieveMessageById(args[0]).complete());
            if (last_msg.get() != null) {
                logger.debug(String.format("'%s' is message id", args[0]));
                //指定ID以降すべて取得
                AtomicBoolean hasNext = new AtomicBoolean(true);
                while (true) {
                    if (!hasNext.get()) break;
                    if (event.getChannel().getHistoryAfter(last_msg.get().getId(), 1).complete().getRetrievedHistory().size() != 0) {
                        event.getChannel().getHistoryAfter(last_msg.get().getId(), 100).queue(messageHistory -> {
                            logger.debug(String.valueOf(messageHistory.getRetrievedHistory().size()));
                            List<Message> out = new ArrayList<>(messageHistory.getRetrievedHistory());
                            if (out.size() == 100) {
                                out.remove(99);
                                out.add(last_msg.get());
                                last_msg.set(out.get(99));
                            } else {
                                out.add(last_msg.get());
                                hasNext.set(false);
                            }
                            messages.add(out);
                            all_size.addAndGet(out.size());
                        });
                    }
                }
            }
        }else if (args[0].matches("^[0-9]+$")){
            List<Message> out = event.getChannel().getHistory().retrievePast(Integer.parseInt(args[0])).complete();
            messages.add(out);
            all_size.addAndGet(out.size());
        }else if(args[0].matches("http(s)?://discord.com/channels/[0-9]{18}/[0-9]{18}/[0-9]{18}")){
            String[] target_urls = args[0].split("/");
            for (int i = 0; i < target_urls.length; i++) {
                if (target_urls[i].equalsIgnoreCase("discord.com")){
                    logger.debug("i+1(channels): "+target_urls[i+1]);
                    logger.debug("i+2(guildId): "+target_urls[i+2]);
                    logger.debug("i+3(channelId): "+target_urls[i+3]);
                    logger.debug("i+4(messageId): "+target_urls[i+4]);
                    if (target_urls[i+1].equalsIgnoreCase("channels")){
                        if (target_urls[i+2].equals(event.getGuild().getId())){
                            if (event.getGuild().getTextChannelById(target_urls[i+3]) != null){
                                textChannel = event.getGuild().getTextChannelById(target_urls[i+3]);
                                AtomicReference<Message> last_msg = new AtomicReference<>(textChannel.retrieveMessageById(target_urls[i+4]).complete());
                                if (last_msg.get() != null) {
                                    //指定ID以降すべて取得
                                    AtomicBoolean hasNext = new AtomicBoolean(true);
                                    while (true) {
                                        if (!hasNext.get()) break;
                                        if (event.getChannel().getHistoryAfter(last_msg.get().getId(), 1).complete().getRetrievedHistory().size() != 0) {
                                            event.getChannel().getHistoryAfter(last_msg.get().getId(), 100).queue(messageHistory -> {
                                                logger.debug(String.valueOf(messageHistory.getRetrievedHistory().size()));
                                                List<Message> out = new ArrayList<>(messageHistory.getRetrievedHistory());
                                                if (out.size() == 100) {
                                                    out.remove(99);
                                                    out.add(last_msg.get());
                                                    last_msg.set(out.get(99));
                                                } else {
                                                    out.add(last_msg.get());
                                                    hasNext.set(false);
                                                }
                                                messages.add(out);
                                                all_size.addAndGet(out.size());
                                            });
                                        }
                                    }
                                }
                            }
                        }else {
                            event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.clear.error.another-guild")).queue();
                        }
                    }
                    break;
                }
            }
        } else {
            event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.clear.error.invalid-val")).queue();
            return true;
        }
        TextChannel finalTextChannel = textChannel;
        messages.forEach(messages1 -> {
            finalTextChannel.deleteMessages(messages1).queue();
        });
        event.getChannel().sendMessage(String.format(new LanguageUtil().getMessage(lang, "command.clear.success"), all_size.get())).queue();

        return true;
    }
}
