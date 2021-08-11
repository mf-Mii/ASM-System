package work.mfmii.other.ASM_System.command;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import work.mfmii.other.ASM_System.Config;
import work.mfmii.other.ASM_System.utils.CommandManager;
import work.mfmii.other.ASM_System.utils.LanguageUtil;
import work.mfmii.other.ASM_System.utils.MailUtil;
import work.mfmii.other.ASM_System.utils.MessageGenerate;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class Contact extends CommandManager {
    public Contact(String name){
        super(name);
    }

    @Override
    public boolean execute(@NotNull User sender, @NotNull String command, @NotNull String[] args, @NotNull MessageReceivedEvent event) {
        event.getGuild().moveVoiceMember(event.getMember(), event.getGuild().getVoiceChannelById("")).queue();
        LanguageUtil.Language lang = new LanguageUtil().getUserLanguage(sender);
        if (args.length == 0){
            event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.contact.error.no-type")).queue();
            return true;
        }else
        if (args.length == 1){
            event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.contact.error.no-content")).queue();
            return true;

        }else
        if (ContactType.fromKey(args[0]) == ContactType.UNKNOWN){
            event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "command.contact.error.unknown-type")).queue();
            return true;
        } else {
            List<String> messages = new ArrayList<>();
            for (int i = 1; i < args.length; i++) {
                messages.add(args[i]);
            }
            Message response = event.getMessage()
                    .reply("<a:loading:864931605931360277>"+new LanguageUtil().getMessage(lang, "command.contact.success.sending"))
                    .mentionRepliedUser(false).complete();
            try {
                sendMessage(sender, String.join(" ", messages), ContactType.fromKey(args[0]), false);
                response.editMessage("✅"+new LanguageUtil().getMessage(lang, "command.contact.success.ok")).mentionRepliedUser(false).queue();
            } catch (Exception exception) {
                response.editMessage(new MessageGenerate().errorMessage(lang, exception)).mentionRepliedUser(false).queue();
                exception.printStackTrace();
            }
            return true;
        }
    }

    @Override
    public boolean executeSlash(@NotNull User sender, @NotNull String command, @NotNull SlashCommandEvent event) {
        String type = event.getOption("type")!=null?event.getOption("type").getAsString():null;
        String content = event.getOption("body")!=null?event.getOption("body").getAsString():null;
        try {
            sendMessage(sender, content, ContactType.fromKey(type), true);
        } catch (Exception e) {
            event.reply(new MessageGenerate().errorMessage(new LanguageUtil().getUserLanguage(sender), e)).setEphemeral(true).mentionRepliedUser(false).queue();
            e.printStackTrace();
        }
        return false;
    }

    private void sendMessage(@NotNull User sender, @NotNull String message, @NotNull ContactType type, boolean isSlash) throws Exception {
        sendToMail(sender, message, type);
        sendToDiscord(sender, message, type);
    }

    private void sendToDiscord(@NotNull User sender, @NotNull String message, @NotNull ContactType type){
        JSONArray array = new Config(Config.ConfigType.JSON).getJSONArray("contact.webhook");
        array.forEach(o -> {
            String wh_url = o.toString();
            WebhookClient webhookClient = new WebhookClientBuilder(wh_url).build();
            WebhookEmbed embed = new WebhookEmbedBuilder().setTitle(new WebhookEmbed.EmbedTitle(String.format("お問い合わせ【%s】", type.getKey()), null))
                    .setAuthor(new WebhookEmbed.EmbedAuthor(sender.getAsTag(), sender.getAvatarUrl(), null))
                    .addField(new WebhookEmbed.EmbedField(false, "内容", message.length()>1024?message.substring(0, 1022)+"…":message))
                    .setTimestamp(OffsetDateTime.now())
                    .build();
            webhookClient.send(embed);
            webhookClient.close();
        });
    }
    private void sendToMail(@NotNull User sender, @NotNull String message, @NotNull ContactType type) throws Exception {
        JSONArray array = new Config(Config.ConfigType.JSON).getJSONArray("contact.mail");
        List<String> mail_addr = new ArrayList<>();
        array.forEach(o -> {
            mail_addr.add(o.toString());
        });
        StringBuilder content = new StringBuilder();
        content.append(String.format("送信者：%s(%s)\n", sender.getAsTag(), sender.getId()));
        content.append(String.format("日時：%s\n",LocalDateTime.now()));
        content.append(String.format("お問い合わせタイプ: %s\n", type.getKey()));
        content.append(String.format("内容:\n%s\n", message));
        new MailUtil().sendMail(mail_addr, "【ASMBot】お問い合わせがありました: タイプ="+type.getKey(), content.toString());
    }

    private enum ContactType{
        HELP("help"),
        BUGS("bug"),
        REQUEST("request"),
        MESSAGE("message"),
        OTHER("other"),
        UNKNOWN("");

        private final String key;

        ContactType(String key){
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public static ContactType fromKey(String key){
            String name = "";
            JSONArray type_jos = new Config(Config.ConfigType.COMMANDS).getJSONArray("contact.types");
            for (Object type_jo : type_jos) {
                JSONObject _type = new JSONObject(type_jo.toString());
                if (key.equalsIgnoreCase(_type.getString("name"))){
                    name = key;
                }else {
                    for (Object aliases : _type.getJSONArray("aliases")) {
                        if (key.equalsIgnoreCase(aliases.toString())) {
                            name = _type.getString("name");
                        }
                    }
                }
            }
            for (ContactType value : values()) {
                if (value.getKey().equalsIgnoreCase(name)){
                    return value;
                }
            }
            return UNKNOWN;
        }
    }
}
