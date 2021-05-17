package work.mfmii.other.ASM_System.utils;

import net.dv8tion.jda.api.entities.Guild;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.ASMSystem;
import work.mfmii.other.ASM_System.Config;
import work.mfmii.other.ASM_System.utils.exceptions.SlashCommandException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SlashCommandUtil {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * commandListのJSONObject
     * {
     *     "id": "000000000000000000",
     *     "guild_id": null(or GuildID(18)),
     *     "data": {
     *         "name": "example",
     *         "description": "hogehoge",
     *         "default_permission": true
     *     }
     * }
     */
    public static Map<Long, JSONObject> commandList = new HashMap<>();
    public SlashCommandUtil(){}

    public Map<Long, JSONObject> getCommands(){
        return commandList;
    }

    public static class Command{
        public JSONObject json = null;
        public long id = -1;


        /**
         *
         * @param id get SlashCommandUtil.Command from commandList with ID
         * @throws SlashCommandException return command not found if command not found
         */
        public Command(@Nonnull final long id) throws SlashCommandException {
            if (commandList.get(id) != null) {
                json = commandList.get(id);
                this.id = id;
            }
            else throw new SlashCommandException("Command not found");
        }

        public String getName(){
            return this.json.getString("name");
        }
        public String getDescription(){
            return this.json.getString("description");
        }

        public String getGuildId(){
            return this.json.getString("guild_id");
        }
        public JSONObject getData(){
            return this.json.getJSONObject("data");
        }
        
        public JSONObject getRaw(){
            return this.json;
        }
        
        public String getId(){
            return String.valueOf(this.id);
        }
        
        public long getIdAsLong(){
            return this.id;
        }
        
        


    }

    public static class Builder{
        JSONObject build_json = new JSONObject();
        public Builder(@Nonnull String name, @Nonnull String description){
            build_json.put("name", name);
            build_json.put("description", description);
        }

        public Builder(@Nonnull String name, @Nonnull String description, boolean default_use){
            build_json.put("name", name);
            build_json.put("description", description);
            build_json.put("default_permission", default_use);
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
        public JSONObject build() {
            return build_json;
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

    public JSONObject submitToDiscord(@Nonnull JSONObject cmd_json){
        RequestBody post = RequestBody.create(MediaType.parse("application/json;charset=utf8"), cmd_json.toString());
        Request request = new Request.Builder()
                .url(String.format("https://discord.com/api/v8/applications/%s/commands", new Config(Config.ConfigType.JSON).getString("applicationId")))
                .addHeader("Authorization", ASMSystem.jda.getToken())
                .post(post)
                .build();
        Response response = null;
        try {
            response = ASMSystem.jda.getHttpClient().newCall(request).execute();
            logger.debug("SlashCommandRequestResponse: "+response.body().string());
            
            return new JSONObject(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONObject submitToDiscord(@Nonnull JSONObject cmd_json, @Nonnull Guild guild){
        RequestBody post = RequestBody.create(MediaType.parse("application/json;charset=utf8"), cmd_json.toString());
        Request request = new Request.Builder()
                .url(String.format("https://discord.com/api/v8/applications/%s/guilds/%s/commands",
                        new Config(Config.ConfigType.JSON).getString("applicationId"), guild.getId()))
                .addHeader("Authorization", ASMSystem.jda.getToken())
                .post(post)
                .build();
        Response response = null;
        try {
            response = ASMSystem.jda.getHttpClient().newCall(request).execute();
            logger.debug("SlashCommandRequestResponse: "+response.body().string());
            return new JSONObject(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONObject submitToDiscord(@Nonnull JSONObject cmd_json, @Nonnull String guild_id){
        RequestBody post = RequestBody.create(MediaType.parse("application/json;charset=utf8"), cmd_json.toString());
        Request request = new Request.Builder()
                .url(String.format("https://discord.com/api/v8/applications/%s/guilds/%s/commands",
                        new Config(Config.ConfigType.JSON).getString("applicationId"), guild_id))
                .addHeader("Authorization", ASMSystem.jda.getToken())
                .post(post)
                .build();
        Response response = null;
        try {
            response = ASMSystem.jda.getHttpClient().newCall(request).execute();
            logger.debug("SlashCommandRequestResponse: "+response.body().string());
            return new JSONObject(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONObject submitToDiscord(@Nonnull String cmd_json){
        RequestBody post = RequestBody.create(MediaType.parse("application/json;charset=utf8"), cmd_json);
        Request request = new Request.Builder()
                .url(String.format("https://discord.com/api/v8/applications/%s/commands", new Config(Config.ConfigType.JSON).getString("applicationId")))
                .addHeader("Authorization", ASMSystem.jda.getToken())
                .post(post)
                .build();
        Response response = null;
        try {
            response = ASMSystem.jda.getHttpClient().newCall(request).execute();
            logger.debug("SlashCommandRequestResponse: "+response.body().string());
            return new JSONObject(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONObject submitToDiscord(@Nonnull String cmd_json, @Nonnull Guild guild){
        RequestBody post = RequestBody.create(MediaType.parse("application/json;charset=utf8"), cmd_json);
        Request request = new Request.Builder()
                .url(String.format("https://discord.com/api/v8/applications/%s/guilds/%s/commands",
                        new Config(Config.ConfigType.JSON).getString("applicationId"), guild.getId()))
                .addHeader("Authorization", ASMSystem.jda.getToken())
                .post(post)
                .build();
        Response response = null;
        try {
            response = ASMSystem.jda.getHttpClient().newCall(request).execute();
            logger.debug("SlashCommandRequestResponse: "+response.body().string());
            return new JSONObject(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONObject submitToDiscord(@Nonnull String  cmd_json, @Nonnull String guild_id){
        RequestBody post = RequestBody.create(MediaType.parse("application/json;charset=utf8"), cmd_json);
        Request request = new Request.Builder()
                .url(String.format("https://discord.com/api/v8/applications/%s/guilds/%s/commands",
                        new Config(Config.ConfigType.JSON).getString("applicationId"), guild_id))
                .addHeader("Authorization", ASMSystem.jda.getToken())
                .post(post)
                .build();
        Response response = null;
        try {
            response = ASMSystem.jda.getHttpClient().newCall(request).execute();
            logger.debug("SlashCommandRequestResponse: "+response.body().string());
            return new JSONObject(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteFromDiscordGlobal(@Nonnull Command command){
        Request request = new Request.Builder()
                .url(String.format("https://discord.com/api/v8/applications/%s/commands/%s", new Config(Config.ConfigType.JSON).getString("applicationId"), command.getId()))
                .addHeader("Authorization", ASMSystem.jda.getToken())
                .delete()
                .build();
        Response response = null;
        try {
            response = ASMSystem.jda.getHttpClient().newCall(request).execute();
            if (response.code()==204)
                return true;
            else throw new SlashCommandException("Response not 204.\n"+response.code()+"\n"+response.body().string());
        } catch (IOException | SlashCommandException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteFromDiscordGuild(@Nonnull Command command, @Nonnull Guild guild){
        Request request = new Request.Builder()
                .url(String.format("https://discord.com/api/v8/applications/%s/guilds/%s/commands",
                        new Config(Config.ConfigType.JSON).getString("applicationId"), guild.getId()))
                .addHeader("Authorization", ASMSystem.jda.getToken())
                .delete()
                .build();
        Response response = null;
        try {
            response = ASMSystem.jda.getHttpClient().newCall(request).execute();
            if (response.code()==204)
            return true;
            else throw new SlashCommandException("Response not 204.\n"+response.code()+"\n"+response.body().string());
        } catch (IOException | SlashCommandException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteFromDiscordGuild(@Nonnull Command command, @Nonnull String guild_id){
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
            if (response.code()==204)
                return true;
            else throw new SlashCommandException("Response not 204.\n"+response.code()+"\n"+response.body().string());
        } catch (IOException | SlashCommandException e) {
            e.printStackTrace();
            return false;
        }
    }

    public JSONArray getAllSubmitted(){
        Request request = new Request.Builder()
                .url(String.format("https://discord.com/api/v8/applications/%s/commands",
                        new Config(Config.ConfigType.JSON).getString("applicationId")))
                .addHeader("Authorization", ASMSystem.jda.getToken())
                .build();
        Response response = null;
        try {
            response = ASMSystem.jda.getHttpClient().newCall(request).execute();
            logger.debug("SlashCommandRequestResponse: "+response.body().string());
            try {
                return new JSONArray(response.body().string());
            } catch (JSONException ex){
                logger.warn(ex.getMessage());
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONArray getAllSubmitted(Guild guild){
        Request request = new Request.Builder()
                .url(String.format("https://discord.com/api/v8/applications/%s/guilds/%s/commands",
                        new Config(Config.ConfigType.JSON).getString("applicationId"), guild.getId()))
                .addHeader("Authorization", ASMSystem.jda.getToken())
                .build();
        Response response = null;
        try {
            response = ASMSystem.jda.getHttpClient().newCall(request).execute();
            logger.debug("SlashCommandRequestResponse: "+response.body().string());
            try {
                return new JSONArray(response.body().string());
            } catch (JSONException ex){
                logger.warn(ex.getMessage());
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public JSONArray getAllSubmitted(String guild_id){
        Request request = new Request.Builder()
                .url(String.format("https://discord.com/api/v8/applications/%s/guilds/%s/commands",
                        new Config(Config.ConfigType.JSON).getString("applicationId"), guild_id))
                .addHeader("Authorization", ASMSystem.jda.getToken())
                .build();
        Response response = null;
        try {
            response = ASMSystem.jda.getHttpClient().newCall(request).execute();
            logger.debug("SlashCommandRequestResponse: "+response.body().string());
            try {
                return new JSONArray(response.body().string());
            } catch (JSONException ex){
                logger.warn(ex.getMessage());
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nonnull
    public enum OptionType{
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
