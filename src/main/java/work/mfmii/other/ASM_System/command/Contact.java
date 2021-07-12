package work.mfmii.other.ASM_System.command;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import work.mfmii.other.ASM_System.Config;
import work.mfmii.other.ASM_System.utils.CommandManager;
import work.mfmii.other.ASM_System.utils.LanguageUtil;
import work.mfmii.other.ASM_System.utils.slash.SlashCommandEvent;

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
        }
        return false;
    }

    @Override
    public boolean executeSlash(@NotNull User sender, @NotNull String command, @NotNull SlashCommandEvent event) {
        return false;
    }

    private void doAction(@NotNull User sender, @NotNull String message, @NotNull ContactType type, boolean isSlash){


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
                JSONObject _type = new JSONObject(type_jo);
                if (key.equalsIgnoreCase(_type.getString("name"))){
                    name = key;
                }else {
                    for (Object aliases : _type.getJSONArray("aliases")) {
                        if (key.equalsIgnoreCase(aliases.toString())) {
                            name = key;
                        }
                    }
                }
            }
            for (ContactType value : values()) {
                if (value.getKey().equalsIgnoreCase(key)){
                    return value;
                }
            }
            return UNKNOWN;
        }
    }
}
