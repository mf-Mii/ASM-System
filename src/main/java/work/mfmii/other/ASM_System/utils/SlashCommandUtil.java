package work.mfmii.other.ASM_System.utils;

import net.dv8tion.jda.api.entities.Guild;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.ASMSystem;
import work.mfmii.other.ASM_System.Config;
import work.mfmii.other.ASM_System.utils.exceptions.SlashCommandException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SlashCommandUtil {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    public static List<JSONObject> commandList = new ArrayList<>();
    public SlashCommandUtil(){}

    public List<JSONObject> getCommands(){
        return commandList;
    }

    public static class Command{
        public JSONObject json = null;

        /**
         * @param name get SlashCommandUtil.Command with the name
         * @throws SlashCommandException return command not found if command not found
         */
        public Command(@Nonnull final String name) throws SlashCommandException {
            commandList.forEach(jsonObject -> {
                if (jsonObject.getString("name").equals(name))
                    json = jsonObject;
            });
            if (json == null)
                throw new SlashCommandException("Command not found");
        }

        /**
         *
         * @param index get SlashCommandUtil.Command from commandList
         * @throws SlashCommandException return command not found if command not found
         */
        public Command(@Nonnull final int index) throws SlashCommandException {
            if (commandList.get(index) != null)
                json = commandList.get(index);
            else throw new SlashCommandException("Command not found");
        }

        public String getName(){
            return this.json.getString("name");
        }
        public String getDescription(){
            return this.json.getString("description");
        }

        public JSONObject getData(){
            return this.json;
        }


    }

    public static class Builder{
        JSONObject build_json = new JSONObject();
        public Builder(@Nonnull String name, @Nonnull String description){
            build_json.put("name", name);
            build_json.put("description", description);
        }

        public Builder getName(@Nonnull String name){
            build_json.put("name", name);
            return this;
        }

        public Builder getDescription(@Nonnull String description){
            build_json.put("description", description);
            return this;
        }

        public Builder addOption(@Nonnull Option option) throws SlashCommandException {
            if (build_json.has("options")){//すでにoptionsがあったとき
                for (Object o : build_json.getJSONArray("options")) {//name重複確認
                    if (new JSONObject(o.toString()).getString("name").equalsIgnoreCase(option.getName())){//重複
                        throw new SlashCommandException("the option name already exists");//エラーで処理中断
                    }
                }
                build_json.put("options", build_json.getJSONArray("options").put(option.getData()));
            }else {
                build_json.put("options", new JSONArray().put(option.getData()));
            }
            return this;
        }
        public Command build() throws SlashCommandException {
            commandList.add(build_json);
            for (int i = 0; i < commandList.size(); i++) {
                if (commandList.get(i).equals(build_json))
                    return new SlashCommandUtil.Command(i);
            }
            throw new SlashCommandException("failed add build_json to commandList");
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static class Option{
        JSONObject opt_json = new JSONObject();
        public Option(@Nonnull String name, @Nonnull String description, boolean required, @Nonnull OptionType type){
            opt_json.put("name", name);
            opt_json.put("description", description);
            opt_json.put("required", required);
            opt_json.put("type", type.getKey());
        }

        public Option setName(@Nonnull String name){
            opt_json.put("name", name);
            return this;
        }

        public Option setDescription(@Nonnull String description){
            opt_json.put("description", description);
            return this;
        }

        public Option setRequired(boolean required){
            opt_json.put("required", required);
            return this;
        }

        public Option removeRequired(){
            opt_json.remove("required");
            return this;
        }

        public Option addOption(@Nonnull Option option) throws SlashCommandException {
            if (opt_json.has("options")){//すでにoptionsがあったとき
                for (Object o : opt_json.getJSONArray("options")) {//name重複確認
                    if (new JSONObject(o.toString()).getString("name").equalsIgnoreCase(option.getName())){//重複
                        throw new SlashCommandException("the option name already exists");//エラーで処理中断
                    }
                }
                opt_json.put("options", opt_json.getJSONArray("options").put(option.getData()));
            }else {
                opt_json.put("options", new JSONArray().put(option.getData()));
            }
            return this;
        }

        public Option addChoice(@Nonnull String name, @Nonnull String value) throws SlashCommandException {
            if (opt_json.has("choices")){
                for (Object o : opt_json.getJSONArray("choices")) {
                    if (new JSONObject(o.toString()).getString("name").equalsIgnoreCase(name)
                            || new JSONObject(o.toString()).getString("value").equalsIgnoreCase(value)){
                        throw new SlashCommandException("name or value already exists in choices");
                    }
                }
                opt_json.put("choices", opt_json.getJSONArray("choices").put(
                        new JSONObject().put("name", name).put("value", value)
                ));
            }else {
                opt_json.put("choices", new JSONArray().put(
                        new JSONObject().put("name", name).put("value", value)
                ));
            }
            return this;
        }

        public String getName(){
            return this.opt_json.getString("name");
        }

        public String getDescription(){
            return this.opt_json.getString("description");
        }

        public JSONObject getData(){
            return this.opt_json;
        }
    }

    public boolean submitToDiscord(@Nonnull Command command){
        RequestBody post = RequestBody.create(MediaType.parse("application/json;charset=utf8"), command.getData().toString());
        Request request = new Request.Builder()
                .url(String.format("https://discord.com/api/v8/applications/%s/commands", new Config(Config.ConfigType.JSON).getString("applicationId")))
                .addHeader("Authorization", ASMSystem.jda.getToken())
                .post(post)
                .build();
        Response response = null;
        try {
            response = ASMSystem.jda.getHttpClient().newCall(request).execute();
            logger.info("SlashCommandRequestResponse: "+response.body().string());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean submitToDiscord(@Nonnull Command command, @Nonnull Guild guild){
        RequestBody post = RequestBody.create(MediaType.parse("application/json;charset=utf8"), command.getData().toString());
        Request request = new Request.Builder()
                .url(String.format("https://discord.com/api/v8/applications/%s/guilds/%s/commands",
                        new Config(Config.ConfigType.JSON).getString("applicationId"), guild.getId()))
                .addHeader("Authorization", ASMSystem.jda.getToken())
                .post(post)
                .build();
        Response response = null;
        try {
            response = ASMSystem.jda.getHttpClient().newCall(request).execute();
            logger.info("SlashCommandRequestResponse: "+response.body().string());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean submitToDiscord(@Nonnull Command command, @Nonnull String guild_id){
        RequestBody post = RequestBody.create(MediaType.parse("application/json;charset=utf8"), command.getData().toString());
        Request request = new Request.Builder()
                .url(String.format("https://discord.com/api/v8/applications/%s/guilds/%s/commands",
                        new Config(Config.ConfigType.JSON).getString("applicationId"), guild_id))
                .addHeader("Authorization", ASMSystem.jda.getToken())
                .post(post)
                .build();
        Response response = null;
        try {
            response = ASMSystem.jda.getHttpClient().newCall(request).execute();
            logger.info("SlashCommandRequestResponse: "+response.body().string());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Nonnull
    public static enum OptionType{
        SUB_COMMAND(1),
        SUB_COMMAND_GROUP(2),
        STRING(3),
        INTEGER(4),
        BOOLEAN(5),
        USER(6),
        CHANNEL(7),
        ROLE(8),
        MENTIONABLE(9);

        private final int key;
        OptionType(int key){
            this.key = key;
        }
        public int getKey(){
            return key;
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
