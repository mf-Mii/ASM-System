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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import okhttp3.*;
import okio.Buffer;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.Config;
import work.mfmii.other.ASM_System.utils.CommandManager;
import work.mfmii.other.ASM_System.utils.MessageGenerate;
import work.mfmii.other.ASM_System.utils.SlashCommandUtil;
import work.mfmii.other.ASM_System.utils.VerifyUtil;
import work.mfmii.other.ASM_System.utils.exceptions.SlashCommandException;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

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
            } else if (args[0].equalsIgnoreCase("createserver")){
                event.getJDA().createGuild("HogeHoge").queue();
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
            }else if (args[0].equalsIgnoreCase("slash")){
                if (args.length >= 3) {
                    if (args[1].equalsIgnoreCase("add")) {
                        if (args.length >= 4) {
                            String name = null;
                            String description = null;
                            List<SlashCommandUtil.Option> opts = new ArrayList<>();

                            if (args[2].equalsIgnoreCase("-template") || args[2].equalsIgnoreCase("-t")){
                                if (args[3].equalsIgnoreCase("1")){
                                    name = "EXAMPLE";
                                    description = "This command is example made with template:1";
                                }
                                if (args[3].equalsIgnoreCase("2")){
                                    name = "ARGS_EXAMPLE";
                                    description = "This command is example for args made with template:2";
                                    try {
                                        opts.add(new SlashCommandUtil.Option("Args1", "Example args 1", true, SlashCommandUtil.OptionType.STRING).addChoice("Choice1", "Choice1").addChoice("Choice2", "Choice2"));
                                    } catch (SlashCommandException e) {
                                        e.printStackTrace();
                                    }
                                }
                                else {
                                    event.getChannel().sendMessage("please select template").queue();
                                    return true;
                                }
                            }else {
                                name = args[2];
                                description = args[3];
                                if (args.length >= 5){
                                    for (String s : args[4].split(",")) {
                                        opts.add(new SlashCommandUtil.Option(s.split(":")[0], s.split(":")[1], false, SlashCommandUtil.OptionType.STRING));
                                    }
                                }
                            }
                            SlashCommandUtil.Builder builder = new SlashCommandUtil.Builder(name, description);
                            opts.forEach(option -> {
                                try {
                                    builder.addOption(option);
                                } catch (SlashCommandException e) {
                                    e.printStackTrace();
                                }
                            });
                            logger.debug(builder.build().toString());
                            new SlashCommandUtil().submitToDiscord(builder.build(), "821326948365107200");

                        }
                    }
                }else {
                    event.getChannel().sendMessage("Args length error").queue();
                }
            }
        }
        return true;
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