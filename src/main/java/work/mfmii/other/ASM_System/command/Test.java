package work.mfmii.other.ASM_System.command;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import okhttp3.*;
import okio.Buffer;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.Config;
import work.mfmii.other.ASM_System.utils.CommandManager;
import work.mfmii.other.ASM_System.utils.MessageGenerate;
import work.mfmii.other.ASM_System.utils.VerifyUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Test extends CommandManager {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    public Test(String name) {
        super(name);
    }

    @Override
    public boolean execute(@NotNull User sender, @NotNull String command, @NotNull String[] args, @NotNull MessageReceivedEvent event) {

        if (args.length != 0 && args[0] != null) {
            if (args[0].equalsIgnoreCase("sms_api")) {
                if (true) {
                    event.getChannel().sendMessage("This command has disabled because sending SMS needs some costs.").queue();
                    return true;
                }
                OkHttpClient client = new OkHttpClient();
                String host = new Config(Config.ConfigType.JSON).getString("verifies.sms.host");
                String phone = new Config(Config.ConfigType.JSON).getString("verifies.sms.test_target");
                for (int i = 1; i + 1 < args.length; i++) {
                    if (args[i] != null && args[i + 1] != null) {
                        if (args[i].equalsIgnoreCase("-host")) {
                            host = args[++i];
                        } else if (args[i].equalsIgnoreCase("-phone")) {
                            phone = args[++i];
                        }
                    }
                }
                JSONObject json = new JSONObject();
                json.put("target", phone);
                json.put("content", "This is a test message from original SMS-API.");
                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf8"), json.toString());
                Request request = new Request.Builder()
                        .url("http://" + host + "/sendsms")
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
                    event.getChannel().sendMessage(request.toString() + "\n" + req_str + "\n" + response.toString()).queue();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (args[0].equalsIgnoreCase("verify")) {
                try {
                    event.getChannel().sendMessage(new VerifyUtil().createLink(VerifyUtil.VerifyType.DISCORD, event.getAuthor().getId(), event.getGuild().getId(), null)).queue();
                } catch (Exception exception) {
                    String stack = "";
                    for (StackTraceElement stackTraceElement : exception.getStackTrace()) {
                        if (stack.length() >= 1500) {
                            stack += "[more]...";
                            break;
                        }
                        if (!stack.isEmpty()) stack += "\n        ";
                        stack += stackTraceElement.getClassName() + ".class (" + stackTraceElement.getMethodName() + "() :" + stackTraceElement.getLineNumber() + ")";
                    }
                    event.getChannel().sendMessage("```" + exception.getClass().getName() + ": " + exception.getMessage() + "\n" + stack + "```").queue();
                }
            } else if (args[0].equalsIgnoreCase("langcheck")) {
                if (args.length < 2) {
                    return true;
                }
                List<String> l1 = new ArrayList<>();
                for (int i = 1; i < args.length; i++) {
                    l1.add(args[i]);
                }
                try {
                    event.getChannel().sendMessage(MessageGenerate.checkLanguage(String.join(" ", l1))).queue();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (args[0].equalsIgnoreCase("ttsself")) {
                if (args.length < 2) {
                    return true;
                }
                List<String> l1 = new ArrayList<>();
                for (int i = 1; i < args.length; i++) {
                    l1.add(args[i]);
                }
                try {
                    event.getChannel().sendFile(new MessageGenerate().textToSpeech(String.join(" ", l1), null), "speech.mp3").queue();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (args[0].equalsIgnoreCase("timecheck")) {
                List<String> sl = new ArrayList<>();
                OffsetDateTime now = OffsetDateTime.now();
                sl.add("Now: " + now);
                sl.add("-3s: " + now.minusSeconds(3));
                sl.add("+3s: " + now.plusSeconds(3));
                sl.add("-60s: " + now.minusSeconds(60));
                sl.add("+60s: " + now.plusSeconds(60));
                event.getChannel().sendMessage(String.join("\n", sl)).queue();
                sl.forEach(s -> {
                    logger.debug(s);
                });
            } else if (args[0].equalsIgnoreCase("createguild")) {
                event.getJDA().createGuild("HogeHoge").queue();
            } else if (args[0].equalsIgnoreCase("guildlist")) {
                List<String> guilds = new ArrayList<>();
                for (Guild guild : event.getJDA().getGuilds()) {
                    guilds.add(guild.getName()+"/"+guild.getId());
                }
                event.getChannel().sendMessage(String.join("\n", guilds)).queue();
            } else if (args[0].equalsIgnoreCase("nsfw-test")) {
                if (args.length == 2 || event.getMessage().getAttachments().size()==1){
                    String img_url = null;
                    if (args.length == 2){
                        img_url = args[1];
                    }else {
                        img_url = event.getMessage().getAttachments().get(0).getUrl();
                    }
                    String file_name = img_url.split("/")[img_url.split("/").length-1];
                    try {
                        Request request = new Request.Builder().url(img_url).build();
                        Response response = new OkHttpClient().newCall(request).execute();
                        if (response.isSuccessful()) {
                            Files.createDirectories(new File(".temp").toPath());
                            OutputStream output = new FileOutputStream(new File(".temp", file_name));
                            output.write(response.body().bytes());
                            output.close();
                            response.close();
                        }else {
                            return true;
                        }
                        BufferedImage image = ImageIO.read(new File(".temp", file_name));
                        BufferedImage tmp = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                        Graphics2D off = tmp.createGraphics();
                        off.drawImage(image, 0, 0, Color.WHITE, null);
                        ImageIO.write(tmp, "jpg", new File(".temp", file_name.replaceAll("\\..*$", ".jpg")));
                        File jpeg = new File(".temp", file_name.replaceAll("\\..*$", ".jpg"));
                        RequestBody post = RequestBody.create(jpeg, MediaType.parse("multipart/form-data"));
                        String boundary = String.valueOf(System.currentTimeMillis());

                        RequestBody requestBody = new MultipartBody.Builder(boundary).setType(MultipartBody.FORM)
                                .addFormDataPart("image", file_name, RequestBody.create(MediaType.parse("multipart/form-data"), jpeg))
                                .build();
                        request = new Request.Builder().url("http://192.168.10.222:19000/nsfw").post(requestBody).build();
                        response = new OkHttpClient().newCall(request).execute();
                        ResponseBody responseBody = response.body();
                        if (responseBody!=null) {
                            String res_str = responseBody.string();
                            logger.debug(res_str);
                            JSONArray json = new JSONArray(res_str);
                            EmbedBuilder eb = new EmbedBuilder().setTitle("画像のNSFW判定");
                            for (Object o : json) {
                                JSONObject clazz = new JSONObject(o.toString());
                                eb.addField(clazz.getString("className"), String.valueOf(clazz.getDouble("probability")), true);
                            }
                            event.getMessage().replyEmbeds(eb.build()).mentionRepliedUser(false).queue();
                        }
                        response.close();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }else {
                    event.getMessage().reply("Please select image file!!").queue();
                }
                return true;
            } else if (args[0].equalsIgnoreCase("speak")) {

                if (args.length < 2) {
                    return true;
                }
                List<String> l1 = new ArrayList<>();
                for (int i = 1; i < args.length; i++) {
                    l1.add(args[i]);
                }
                File msg;
                try {
                    msg = new MessageGenerate().textToSpeechAsFile(String.join(" ", l1), null);
                } catch (IOException e) {
                    e.printStackTrace();
                    msg = null;
                    return true;
                }
                if (!event.getGuild().getSelfMember().hasPermission(Permission.VOICE_CONNECT) || !event.getGuild().getSelfMember().hasPermission(Permission.VOICE_SPEAK)){
                    event.getChannel().sendMessage("Please give me the permission to connect or speak or connect and speak permission!").queue();
                    return true;
                }
                VoiceChannel voiceChannel = event.getMember().getVoiceState().getChannel();
                if (voiceChannel == null) {
                    event.getChannel().sendMessage("You have to join any voice channel if you want to use this command.").queue();
                    return true;
                }
                AudioManager audioManager = voiceChannel.getGuild().getAudioManager();
                if (audioManager.isAttemptingToConnect()){
                    event.getChannel().sendMessage("I'm already trying to connect!").queue();
                    return true;
                }
                System.out.println("Connecting to "+voiceChannel);
                audioManager.openAudioConnection(voiceChannel);
                event.getChannel().sendMessage("Connected!").queue();
                //Sending voice message to voice channel with LavaPlayer
                AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

                AudioSourceManagers.registerLocalSource(playerManager);
                AudioPlayer player = playerManager.createPlayer();

                audioManager.setSendingHandler(new AudioPlayerSendHandler(player));
                System.out.println("create player.");

                System.out.println(msg.getName());
                //playerManager.loadItem("M:/@Movie/Material/mv_sounds/BGM/ニコニコモンズ/みwなwぎwっwてwきwたwwwww.mp3", new AudioLoadResultHandler() {
                playerManager.loadItem(".temp/"+msg.getName(), new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack audioTrack) {
                        System.out.println("Track loaded!");
                        //playTracks.add(audioTrack);


                        System.out.println("Track Info: Title="+audioTrack.getInfo().title+", Author="+audioTrack.getInfo().author+", URI="+audioTrack.getInfo().uri);
                        //AudioSourceManagers.registerLocalSource(playerManager);
                        //AudioPlayer player = playerManager.createPlayer();
                        if (!player.startTrack(audioTrack, true)){
                            System.out.println("Can't play file!");
                        }


                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist audioPlaylist) {

                    }

                    @Override
                    public void noMatches() {

                    }

                    @Override
                    public void loadFailed(FriendlyException e) {

                    }
                });

                /*
                playTracks.forEach(audioTrack -> {
                    System.out.println(audioTrack.getInfo());
                    player.playTrack(audioTrack);
                });

                 */
            }
            /*
            else if (args[0].equalsIgnoreCase("sendmail")){
                if (args.length < 2) {
                    event.getChannel().sendMessage("Please set e-mail address").queue();
                    return true;
                }
                List<String> list = new ArrayList<>();
                for (int i = 1; i < args.length; i++) {
                    list.add(args[i]);
                }
                list.forEach(s -> {
                    new MailUtil().sendMail(s, "Test Mail", "This Mail sent from DiscordBot.");
                });
            }
             */
            else if(args[0].equalsIgnoreCase("user")){
                if (args.length!=2){
                    event.getMessage().reply("Usage: a#test user <ID>").queue();
                    return true;
                }
                String targetId = args[1];
                User target = event.getJDA().retrieveUserById(targetId).complete();
                if (target==null) {
                    event.getChannel().sendMessage("Unknown User").queue();
                    return true;
                }
                List<String> li = new ArrayList<>();
                li.add("Name: "+target.getName());
                li.add("Discriminator: "+target.getDiscriminator());
                li.add("AvatarURL: "+target.getAvatarUrl());
                li.add("AvatarID: "+target.getAvatarId());
                li.add("hasPrivateChannel: "+target.hasPrivateChannel());
                li.add("createdDate: "+target.getTimeCreated().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS")));
                li.add("isBot: "+target.isBot());
                li.add("isSystem: "+target.isSystem());
                event.getChannel().sendMessage(String.join("\n", li)).queue();
            }else if (args[0].equalsIgnoreCase("getrich")){
                Member member = event.getMember();
                List<MessageEmbed> embeds = new ArrayList<>();
                for (Activity activity : member.getActivities()) {
                    EmbedBuilder eb = new EmbedBuilder().setTitle("RichPresenceInfo").setColor(Color.MAGENTA);
                    eb.addField("Activity.getName();",activity.getName(), true);
                    eb.addField("Activity.getType().name();", activity.getType().name(), true);
                    if (activity.asRichPresence()!=null && activity.isRich()) {
                        eb.addField("Activity.asRichPresence().getApplicationId();", activity.asRichPresence().getApplicationId()==null?"null": Objects.requireNonNull(activity.asRichPresence()).getApplicationId(), false);
                        eb.addField("Activity.asRichPresence().getName();", activity.asRichPresence().getName()==null?"null":activity.asRichPresence().getName(), false);
                        eb.addField("Activity.asRichPresence().getDetails();", activity.asRichPresence().getDetails()==null?"null": Objects.requireNonNull(activity.asRichPresence()).getDetails(), false);
                        eb.addField("Activity.asRichPresence().getSessionId();", activity.asRichPresence().getSessionId()==null?"null": Objects.requireNonNull(activity.asRichPresence()).getSessionId(), true);
                        eb.addField("Activity.asRichPresence().getState();", activity.asRichPresence().getState()==null?"null": Objects.requireNonNull(activity.asRichPresence()).getState(), false);
                        eb.addField("Activity.asRichPresence().getSyncId();", activity.asRichPresence().getSyncId()==null?"null": Objects.requireNonNull(activity.asRichPresence()).getSyncId(), false);
                        eb.addField("Activity.asRichPresence().getUrl();", activity.asRichPresence().getUrl()==null?"null": Objects.requireNonNull(activity.asRichPresence()).getUrl(), false);
                        eb.addField("Activity.asRichPresence().getLargeImage().getUrl();", activity.asRichPresence().getLargeImage()==null?"null": Objects.requireNonNull(activity.asRichPresence().getLargeImage()).getUrl(), false);
                        eb.addField("Activity.asRichPresence().getLargeImage().getText();", activity.asRichPresence().getLargeImage()==null?"null": Objects.requireNonNull(activity.asRichPresence().getLargeImage()).getText(), false);
                        eb.addField("Activity.asRichPresence().getSmallImage().getUrl();", activity.asRichPresence().getSmallImage()==null?"null": Objects.requireNonNull(activity.asRichPresence().getSmallImage()).getUrl(), false);
                        eb.addField("Activity.asRichPresence().getSmallImage().getText();", activity.asRichPresence().getSmallImage()==null?"null": Objects.requireNonNull(activity.asRichPresence().getSmallImage()).getText(), false);
                        eb.addField("Activity.asRichPresence().getParty().getMax()", activity.asRichPresence().getParty()==null?"null": String.valueOf(Objects.requireNonNull(activity.asRichPresence().getParty()).getMax()), false);
                        eb.addField("Activity.asRichPresence().getParty().getSize()", activity.asRichPresence().getParty()==null?"null": String.valueOf(Objects.requireNonNull(activity.asRichPresence().getParty()).getSize()), false);
                        eb.addField("Activity.asRichPresence().getParty().getId()", activity.asRichPresence().getParty()==null?"null": String.valueOf(Objects.requireNonNull(activity.asRichPresence().getParty()).getId()), false);
                        eb.addField("Activity.asRichPresence().toString();", activity.asRichPresence().toString(), false);
                        eb.addField("Activity.asRichPresence().getTimestamps().getStartTime()...", activity.asRichPresence().getTimestamps()==null?"null": Objects.requireNonNull(activity.asRichPresence().getTimestamps()).getStartTime()==null?"null": Objects.requireNonNull(activity.asRichPresence().getTimestamps()).getStartTime().atZone(ZoneId.of("JST", ZoneId.SHORT_IDS)).format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS")), false);
                        eb.addField("Activity.asRichPresence().getTimestamps().getEndTime()...", activity.asRichPresence().getTimestamps()==null?"null": Objects.requireNonNull(activity.asRichPresence().getTimestamps()).getEndTime()==null?"null": Objects.requireNonNull(activity.asRichPresence().getTimestamps()).getEndTime().atZone(ZoneId.of("JST", ZoneId.SHORT_IDS)).format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS")), false);
                        eb.addField("Activity.asRichPresence().getTimestamps().getElapsedTime(ChronoUnit.SECONDS);", activity.asRichPresence().getTimestamps()==null?"null": String.valueOf(Objects.requireNonNull(activity.asRichPresence().getTimestamps()).getElapsedTime(ChronoUnit.SECONDS)), false);
                        eb.addField("Activity.asRichPresence().getTimestamps().getRemainingTime(ChronoUnit.SECONDS);", activity.asRichPresence().getTimestamps()==null?"null": String.valueOf(Objects.requireNonNull(activity.asRichPresence().getTimestamps()).getRemainingTime(ChronoUnit.SECONDS)), false);
                    }
                    embeds.add(eb.build());
                }
                if (embeds.size()==0)
                    event.getMessage().reply("None").mentionRepliedUser(false).queue();
                else
                    event.getMessage().reply(new MessageBuilder().setEmbeds(embeds).build()).mentionRepliedUser(false).queue();
            }
        }
        return true;
    }

    @Override
    public boolean executeSlash(@NotNull User sender, @NotNull String command, @NotNull SlashCommandEvent event) {
        return false;
    }

    public class AudioPlayerSendHandler implements AudioSendHandler {
        private final AudioPlayer audioPlayer;
        private final ByteBuffer buffer;
        private final MutableAudioFrame frame;

        /**
         * @param audioPlayer Audio player to wrap.
         */
        public AudioPlayerSendHandler(AudioPlayer audioPlayer) {
            this.audioPlayer = audioPlayer;
            this.buffer = ByteBuffer.allocate(1024);
            this.frame = new MutableAudioFrame();
            this.frame.setBuffer(buffer);
        }

        @Override
        public boolean canProvide() {
            // returns true if audio was provided
            return audioPlayer.provide(frame);
        }

        @Override
        public ByteBuffer provide20MsAudio() {
            // flip to make it a read buffer
            ((java.nio.Buffer) buffer).flip();
            return buffer;
        }

        @Override
        public boolean isOpus() {
            return true;
        }
    }
}