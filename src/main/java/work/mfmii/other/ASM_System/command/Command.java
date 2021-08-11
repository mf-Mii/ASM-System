package work.mfmii.other.ASM_System.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.Config;
import work.mfmii.other.ASM_System.utils.*;

import java.util.ArrayList;
import java.util.List;

public class Command extends CommandManager {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    public Command(String name){
        super(name);
    }

    @Override
    public boolean execute(@NotNull User sender, @NotNull String command, @NotNull String[] args, @NotNull MessageReceivedEvent event) {
        LanguageUtil.Language lang = new LanguageUtil().getUserLanguage(event.getAuthor());
        if (!new PermissionUtil().hasPermission(event.getGuild().getId(), event.getChannel().getId(), sender.getId(), this.getPermission())){
            event.getChannel().sendMessage(this.getPermissionMessage(lang)).queue();
            return true;
        }
        Message message = createResponse(lang, event.getGuild().getId(), event.getChannel().getId(), sender.getId(), null);
        event.getMessage().reply(message).mentionRepliedUser(false).queue();

        /*}else {
            StringBuilder sb = new StringBuilder();
            sb.append(new LanguageUtil().getMessage(lang, "command.help.content.text").replaceAll("\\$\\{prefix\\}", new Config(Config.ConfigType.JSON).getString("prefix")));

            final List<String> _afterAdd = new ArrayList<>();
            final List<String> _added = new ArrayList<>();
            new CommandMap().getCommands().forEach(commandManager -> {
                if (!_added.contains(commandManager.getName()) && new PermissionUtil().hasPermission(event.getGuild().getId(), event.getChannel().getId(), sender.getId(), commandManager.getPermission())) {
                    String _out;
                    String prefix = commandManager.isAdminCommand() ? new Config(Config.ConfigType.JSON).getString("admin.prefix") : new Config(Config.ConfigType.JSON).getString("prefix");
                    String aliases_str = !commandManager.getAliases().isEmpty()?"["+String.join(" | ", commandManager.getAliases())+"]":"";
                    _out = "\n**"+commandManager.getUsage(lang).replaceAll("\\$\\{command\\}", prefix+commandManager.getName()+aliases_str)+"**\n"+commandManager.getAbout(lang);
                    if (commandManager.isAdminCommand()){
                        _afterAdd.add(_out);
                    }
                    else {
                        if (sb.length() + _out.length() > 2000) {
                            event.getChannel().sendMessage(sb.toString()).queue();
                            sb.delete(0, 2000);
                        }
                        sb.append(_out);
                    }
                    _added.add(commandManager.getName());
                }
            });
            _afterAdd.forEach(s -> {
                if (sb.length() + s.length() > 2000) {
                    event.getChannel().sendMessage(sb.toString()).queue();
                    sb.delete(0, 2000);
                }
                sb.append(s);
            });
            event.getChannel().sendMessage(sb.toString()).queue();
        }

         */
        return true;

    }

    @Override
    public boolean executeSlash(@NotNull User sender, @NotNull String command, @NotNull SlashCommandEvent event) {
        String guild_id = event.isFromGuild() ? event.getGuild().getId() : null;
        String channel_id = event.isFromGuild() ? event.getChannel().getId() : null;
        String user_id = sender.getId();
        LanguageUtil.Language lang = new LanguageUtil().getUserLanguage(sender);
        if (new PermissionUtil().hasPermission(guild_id, channel_id, user_id, this.getPermission())){
            String target = event.getOption("cmd")!=null?event.getOption("cmd").getAsString():null;
            Message response = createResponse(lang, guild_id, channel_id, user_id, target);
            event.reply(response).setEphemeral(true).addActionRow(new MessageGenerate().closeButton(lang)).mentionRepliedUser(true).queue();
        }else {
            event.reply(this.getPermissionMessage(lang)).setEphemeral(true).queue();
        }
        return false;
    }

    private Message createResponse(LanguageUtil.Language lang, String guildId, String channelId, String userId, String target){
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(new LanguageUtil().getMessage(lang, "command.commands.embed.title"))
                .setAuthor(new LanguageUtil().getMessage(lang, "command.commands.embed.author.name"),
                        new LanguageUtil().getMessage(lang, "command.commands.embed.author.url"),
                        new LanguageUtil().getMessage(lang, "command.commands.embed.author.iconUrl"));
        eb.setDescription(new LanguageUtil().getMessage(lang, "command.commands.embed.description").replaceAll("\\$\\{prefix\\}", new Config(Config.ConfigType.JSON).getString("prefix")));
        final List<String> _added = new ArrayList<>();
        final List<MessageEmbed.Field> _afterFields = new ArrayList<>();
        new CommandMap().getCommands().forEach(commandManager -> {
            if (!_added.contains(commandManager.getName()) && new PermissionUtil().hasPermission(guildId, channelId, userId, commandManager.getPermission())) {
                String aliases_str = !commandManager.getAliases().isEmpty()?"["+String.join(" | ", commandManager.getAliases())+"]":"";
                String _s1 = commandManager.getUsage(lang).replaceAll("\\$\\{command\\}", commandManager.getName() + aliases_str);
                String _s2 = commandManager.getAbout(lang);
                if (commandManager.isAdminCommand()){
                    _s1 = "<:PrefixCommand:864803690429808650><:Moderator:864804761638273024>"+_s1;
                    _afterFields.add(new MessageEmbed.Field(_s1, _s2, false));
                }
                else {
                    if (commandManager.isSlashCommand()) _s1 = "<:SlashCommand:864803691085561867>"+_s1;
                    if (commandManager.isPrefixCommand()) _s1 = "<:PrefixCommand:864803690429808650>"+_s1;
                    eb.addField(_s1, _s2, false);
                }
                _added.add(commandManager.getName());
            }
        });
        _afterFields.forEach(eb::addField);
        Message res_msg = new MessageBuilder().append(new LanguageUtil().getMessage(lang, "command.commands.embed.text")).setEmbeds(eb.build()).build();
        return res_msg;
    }
}
