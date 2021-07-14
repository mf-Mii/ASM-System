package work.mfmii.other.ASM_System.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.Config;
import work.mfmii.other.ASM_System.utils.CommandManager;
import work.mfmii.other.ASM_System.utils.LanguageUtil;
import work.mfmii.other.ASM_System.utils.PermissionUtil;
import work.mfmii.other.ASM_System.utils.UserUtil;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserInfo extends CommandManager {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    public UserInfo(String name){
        super(name);
    }

    @Override
    public boolean execute(@NotNull User sender, @NotNull String command, @NotNull String[] args, @NotNull MessageReceivedEvent event) {
        LanguageUtil.Language lang = new LanguageUtil().getUserLanguage(event.getAuthor());
        if(this.getAliases().contains(command) || this.getName().equalsIgnoreCase(command)) {
            if (!new PermissionUtil().hasPermission(event.getGuild().getId(), event.getChannel().getId(), sender.getId(), this.getPermission())) {
                String output = this.getPermissionMessage(lang);
                if(output==null) output = "500: Internal Error";
                event.getChannel().sendMessage(output).queue();
                return true;
            }
            else {
                final boolean isGuild = event.isFromGuild();

                boolean isSelf = true;
                if (args.length > 0 && args[0].matches("[0-9]{18}")) isSelf = false;

                String targetId = isSelf ? sender.getId() : args[0];
                final EmbedBuilder embedBuilder = new EmbedBuilder();
                User target = event.getJDA().getUserById(targetId);


                StringBuilder output = new StringBuilder();
                for (int i = 0; i < args.length; i++) {
                    if (i == 0 && !isSelf) i++;
                    if (args.length - 1 > i) {
                        if (args[i].equalsIgnoreCase("-p") || args[i].equalsIgnoreCase("-perm") || args[i].equalsIgnoreCase("-permission")) {
                            output.append("\nPermissionInfo\n");
                            output.append(args[++i]);
                            output.append(": ");
                            if (event.isFromGuild())
                                output.append(new PermissionUtil().hasPermission(event.getGuild().getId(), event.getChannel().getId(), targetId, args[i]));
                            else output.append(new PermissionUtil().hasPermission(null, null, targetId, args[i]));
                            output.append("\n");
                        }
                    }
                }
                if (target == null && !isSelf) {
                    JSONObject jo = new UserUtil(targetId).getUserdataFromHttpApi();
                    if (jo.has("message")) {
                        output.append(String.format("\nAPI Error\n```%s```", jo.getString("message")));
                    } else {
                        String reputation_str;
                        double reputation = new UserUtil(targetId).getReputation();
                        if (reputation == -125) {
                            reputation_str = "**SQL Error!**";
                        } else if (reputation == -127) {
                            reputation_str = "未評価";
                        } else {
                            reputation_str = String.format("%s / 10.0", reputation);
                        }
                        output.append(new LanguageUtil().getMessage(lang, "command.userinfo.embed.text").replaceAll("\\$\\{username\\}", jo.getString("username") + "#" + jo.getString("discriminator")));
                        embedBuilder.setTitle(new LanguageUtil().getMessage(new LanguageUtil().getUserLanguage(sender), "command.userinfo.embed.title"))
                                .setThumbnail(String.format("https://cdn.discordapp.com/avatars/%s/%s.png?size=128", jo.getString("id"), jo.getString("avatar")));
                        embedBuilder.addField(new LanguageUtil().getMessage(lang, "command.userinfo.embed.field.nameid"), String.format("%s\n%s", jo.getString("username") + "#" + jo.getString("discriminator"), targetId), true)
                                .addField(new LanguageUtil().getMessage(lang, "command.userinfo.embed.field.created"), new LanguageUtil().getMessage(lang, "default.unknown"), true);
                        embedBuilder.addField(new LanguageUtil().getMessage(lang, "command.userinfo.embed.field.bot"), new LanguageUtil().getMessage(lang, "default.unknown"), true);
                        embedBuilder.addField(new LanguageUtil().getMessage(lang, "command.userinfo.embed.field.reputation"), reputation_str, true);
                    }
                } else {
                    output.append(new LanguageUtil().getMessage(lang, "command.userinfo.text").replaceAll("\\$\\{username\\}", target.getAsTag()));
                    String reputation_str;
                    double reputation = new UserUtil(target).getReputation();
                    if (reputation == -125) {
                        reputation_str = "**SQL Error!**";
                    } else if (reputation == -127) {
                        reputation_str = "未評価";
                    } else {
                        reputation_str = String.format("%s / 10.0", reputation);
                    }
                    embedBuilder.setTitle(new LanguageUtil().getMessage(new LanguageUtil().getUserLanguage(sender), "command.userinfo.embed.title"))
                            .setThumbnail(target.getAvatarUrl());
                    embedBuilder.addField(new LanguageUtil().getMessage(lang, "command.userinfo.embed.field.nameid"), String.format("%s\n%s", target.getAsTag(), targetId), true)
                            .addField(new LanguageUtil().getMessage(lang, "command.userinfo.embed.field.created"), target.getTimeCreated().format(DateTimeFormatter.ofPattern("yyyy年M月dd日\nHH時mm分ss秒")), true);
                    {
                        Member targetMember = target.getMutualGuilds().size()!=0?target.getMutualGuilds().get(0).getMember(target):null;
                        logger.debug("status target is null: "+(targetMember==null));
                        String status = "";
                        if (targetMember.getOnlineStatus() == OnlineStatus.ONLINE)
                            status = new LanguageUtil().getMessage(lang, "default.status.online");
                        if (targetMember.getOnlineStatus() == OnlineStatus.IDLE)
                            status = new LanguageUtil().getMessage(lang, "default.status.idle");
                        embedBuilder.addField(new LanguageUtil().getMessage(lang, "command.userinfo.embed.field.status"), targetMember.getOnlineStatus().name(), true);
                    }

                    if (isGuild && target.getMutualGuilds().contains(event.getGuild())) {
                        Member targetMember = event.getGuild().getMember(target);
                        embedBuilder.addField(new LanguageUtil().getMessage(lang, "command.userinfo.embed.field.joined"), targetMember.getTimeJoined().format(DateTimeFormatter.ofPattern("yyyy年M月dd日\nHH時mm分ss秒")), true)
                                .addField(new LanguageUtil().getMessage(lang, "command.userinfo.embed.field.nickname"), (targetMember.getNickname() == null || event.getGuild().getMember(target).getNickname().isEmpty()) ? "_なし_" : event.getGuild().getMember(target).getNickname(), true);
                    }
                    embedBuilder.addField(new LanguageUtil().getMessage(lang, "command.userinfo.embed.field.bot"), new LanguageUtil().getMessage(lang, String.format("default.%s", target.isBot() ? "yes" : "no")), true);
                    embedBuilder.addField(new LanguageUtil().getMessage(lang, "command.userinfo.embed.field.reputation"), reputation_str, true);
                    if (isGuild && (target.getMutualGuilds().contains(event.getGuild()) || isSelf)) {
                        Member targetMember = event.getGuild().getMember(target);
                        final List<String> role_mentions = new ArrayList<>();
                        targetMember.getRoles().forEach(role -> {
                            role_mentions.add(role.getAsMention());
                        });
                        embedBuilder.addField(new LanguageUtil().getMessage(lang, "command.userinfo.embed.field.roles"), String.join(",", role_mentions), false);
                        final Map<String, List<String>> perms_map = new HashMap<>();
                        targetMember.getPermissions(event.getTextChannel()).forEach(permission -> {
                            logger.debug("GettingPermission>"+permission.getName());
                            String perm_key = new Config(Config.ConfigType.DEFAULT).getString("permissions." + permission.getName());
                            String group = perm_key.split("\\.")[0];
                            final List<String> _perms;
                            if (!perms_map.containsKey(group)) {
                                _perms = new ArrayList<>();
                            } else {
                                _perms = perms_map.get(group);
                            }
                            _perms.add("`" + new LanguageUtil().getMessage(lang, "default.permissions." + perm_key) + "`");
                            perms_map.put(group, _perms);
                        });
                        embedBuilder.addField(new LanguageUtil().getMessage(lang, "command.userinfo.embed.field.perms"), "", false);
                        perms_map.forEach((k, v) -> {
                            embedBuilder.addField("> **"+new LanguageUtil().getMessage(lang, "default.permissions." + k + ".name")+"**", String.join(", ", v), false);
                        });
                    }
                    /*
                    if(new Config(Config.ConfigType.JSON).getBoolean("debug_mode")){
                        System.out.println("isGuild: "+isGuild+", targetID: "+targetId+", target: "+target.getAsTag()+", \ntarget.getMutualGuilds().contains(event.getGuild()): "+target.getMutualGuilds().contains(event.getGuild()));
                    }

                     */


                }
                if (!embedBuilder.isEmpty())
                    event.getChannel().sendMessage(output.toString()).embed(embedBuilder.build()).queue();
                else event.getChannel().sendMessage(output.toString()).queue();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean executeSlash(@NotNull User sender, @NotNull String command, @NotNull SlashCommandEvent event) {
        return false;
    }
}
