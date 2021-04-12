package work.mfmii.other.ASM_System.command;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import okhttp3.*;
import okio.Buffer;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import work.mfmii.other.ASM_System.Config;
import work.mfmii.other.ASM_System.utils.CommandManager;

import java.io.IOException;

public class Test extends CommandManager {
    public Test(String name){
        super(name);
    }

    @Override
    public boolean execute(@NotNull User sender, @NotNull String command, @NotNull String[] args, @NotNull MessageReceivedEvent event) {
        if (true) {
            event.getChannel().sendMessage("This command has disabled because sending SMS needs some costs.").queue();
            return true;
        }
        if(args.length != 0){
            if (args[0] != null && args[0].equalsIgnoreCase("sms_api")){
                OkHttpClient client = new OkHttpClient();
                String host = new Config(Config.ConfigType.JSON).getString("verifies.sms.host");
                String phone = new Config(Config.ConfigType.JSON).getString("verifies.sms.test_target");
                for (int i = 1; i+1 < args.length; i++) {
                    if (args[i] != null && args[i+1] != null) {
                        if (args[i].equalsIgnoreCase("-host")){
                            host = args[++i];
                        }else if (args[i].equalsIgnoreCase("-phone")){
                            phone = args[++i];
                        }
                    }
                }
                JSONObject json = new JSONObject();
                json.put("target", phone);
                json.put("content", "This is a test message from original SMS-API.");
                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf8"), json.toString());
                Request request = new Request.Builder()
                        .url("http://"+host+"/sendsms")
                        .post(requestBody)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    String req_str = null;
                    try {
                        final Request copy = request.newBuilder().build();
                        final Buffer buffer = new Buffer();
                        copy.body().writeTo(buffer);
                        req_str = buffer.readUtf8();
                    } catch (final IOException e) {
                    }
                    event.getChannel().sendMessage(request.toString()+"\n"+req_str+"\n"+response.toString()).queue();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
}
