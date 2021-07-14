package work.mfmii.other.ASM_System.command;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
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

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class Contact extends CommandManager {
    public Contact(String name){
        super(name);
    }

    @Override
    public boolean execute(@NotNull User sender, @NotNull String command, @NotNull String[] args, @NotNull MessageReceivedEvent event) {
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
            StringBuilder message = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                message.append(args[i]);
            }
            sendMessage(sender, message.toString(), ContactType.fromKey(args[0]), false);
            event.getMessage().reply("☑お問い合わせを送信しました！\n```DMにBotを介して返信されます。返信できない場合は、公式サイトにて質問内容と回答を公開します。```").queue();
            return true;
        }
    }

    @Override
    public boolean executeSlash(@NotNull User sender, @NotNull String command, @NotNull SlashCommandEvent event) {
        return false;
    }

    private void sendMessage(@NotNull User sender, @NotNull String message, @NotNull ContactType type, boolean isSlash){
        sendToDiscord(sender, message, type);
        sendToMail(sender, message, type);
    }

    private void sendToDiscord(@NotNull User sender, @NotNull String message, @NotNull ContactType type){
        JSONArray array = new Config(Config.ConfigType.JSON).getJSONArray("contact.webhook");
        array.forEach(o -> {
            String wh_url = o.toString();
            WebhookClient webhookClient = new WebhookClientBuilder(wh_url).build();
            WebhookEmbed embed = new WebhookEmbedBuilder().setTitle(new WebhookEmbed.EmbedTitle(String.format("お問い合わせ【%s】", type.getKey()), null))
                    .setAuthor(new WebhookEmbed.EmbedAuthor(sender.getAsTag(), sender.getAvatarUrl(), null))
                    .addField(new WebhookEmbed.EmbedField(false, "内容", message.length()>100?message.substring(1, 100):message))
                    .setTimestamp(OffsetDateTime.now())
                    .build();
            webhookClient.send(embed);
            webhookClient.close();
        });
    }
    private void sendToMail(@NotNull User sender, @NotNull String message, @NotNull ContactType type){
        JSONArray array = new Config(Config.ConfigType.JSON).getJSONArray("contact.mail");
        array.forEach(o -> {
            String mail_addr = o.toString();
            StringBuilder content = new StringBuilder();
            content.append(String.format("送信者：%s(%s)\n", sender.getAsTag(), sender.getId()));
            content.append(String.format("日時：%s\n",LocalDateTime.now()));
            content.append(String.format("お問い合わせタイプ: %s\n", type.getKey()));
            content.append(String.format("内容:\n%s\n", message));
            new MailUtil().sendMail(mail_addr, "【ASMBot】お問い合わせがありました: タイプ="+type.getKey(), content.toString());
        });
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
