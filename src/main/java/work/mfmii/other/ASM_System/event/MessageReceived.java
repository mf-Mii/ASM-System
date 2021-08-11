package work.mfmii.other.ASM_System.event;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.Config;
import work.mfmii.other.ASM_System.utils.*;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageReceived extends EventManager {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    public MessageReceived(@Nonnull Class type){
        super(type);
    }

    @Override
    public boolean execute(@NotNull GenericEvent genericEvent) {
        if (genericEvent instanceof MessageReceivedEvent) {
            MessageReceivedEvent event = (MessageReceivedEvent) genericEvent;
            logger.info(String.format("Message U:%s(%s) CH:%s Content:%s",
                    event.getAuthor().getAsTag(),
                    event.getAuthor().getId(),
                    event.getChannel().getId(),
                    event.getMessage().getContentRaw()));
            LanguageUtil.Language lang = new UserUtil(event.getAuthor()).getLanguage();
            //OLD Command
            if (event.getMessage().getContentRaw().startsWith(new Config(Config.ConfigType.JSON).getString("prefix"))) {
                boolean exec_res = new CommandMap().dispatch(event.getAuthor(), event.getMessage().getContentRaw().replaceFirst(new Config(Config.ConfigType.JSON).getString("prefix"), ""), event, false);
                if (!exec_res) {
                    event.getChannel().sendMessage(new LanguageUtil().getMessage(new LanguageUtil().getUserLanguage(event.getAuthor()), "command.error-msg.unknown")).queue();
                }
                return true;
            } else if (event.getMessage().getContentRaw().startsWith(new Config(Config.ConfigType.JSON).getString("admin.prefix"))) {
                boolean exec_res = new CommandMap().dispatch(event.getAuthor(), event.getMessage().getContentRaw().replaceFirst(new Config(Config.ConfigType.JSON).getString("admin.prefix"), ""), event, true);
                if (!exec_res) {
                    event.getChannel().sendMessage(new LanguageUtil().getMessage(new LanguageUtil().getUserLanguage(event.getAuthor()), "command.error-msg.unknown")).queue();
                }
                return true;
            }

            //Image checking
            if (event.isFromGuild()) {
                if (!new ChannelUtil(event.getTextChannel()).isRegistered()) {
                    boolean registering = new ChannelUtil(event.getTextChannel()).register();
                    logger.debug("Channel not registered. Registering channel...  Success:"+registering);
                }
                if (!event.getTextChannel().isNSFW()) {
                    if (event.getMessage().getAttachments().size() > 0) {
                        for (int i = 0; i < event.getMessage().getAttachments().size(); i++) {
                            Message.Attachment attachment = event.getMessage().getAttachments().get(i);
                            if (attachment.isImage()) {
                                List<String> nsfw = checkImageNSFW(event.getTextChannel(), event.getMessageId(), attachment.getUrl(), i + "." + attachment.getFileName());
                                if (nsfw != null && !nsfw.isEmpty()) {
                                    EmbedBuilder eb = new EmbedBuilder();
                                    eb.setTitle(new LanguageUtil().getMessage(lang, "event.MessageReceivedEvent.detectNSFW.embed.title")).setColor(Color.RED);
                                    eb.addField(new LanguageUtil().getMessage(lang, "event.MessageReceivedEvent.detectNSFW.embed.fields.sender"), event.getAuthor().getAsTag(), true);
                                    eb.addField(new LanguageUtil().getMessage(lang, "event.MessageReceivedEvent.detectNSFW.embed.fields.img_size"), String.format("%s x %s", attachment.getWidth(), attachment.getHeight()), true);
                                    eb.addField(new LanguageUtil().getMessage(lang, "event.MessageReceivedEvent.detectNSFW.embed.fields.detects"), String.join("\n", nsfw), true);
                                    String raw_link = String.format("[Click here!](%s)", new Config(Config.ConfigType.JSON).getString("webUrls.view_nsfw_raw")
                                            .replaceAll("\\$\\{url\\.bot\\}", new Config(Config.ConfigType.JSON).getString("webUrls.bot"))
                                            .replaceAll("\\$\\{file\\}", String.format("nsfw.%s.%s.", event.getChannel().getId(), event.getMessageId()) + attachment.getFileName()));
                                    eb.addField(new LanguageUtil().getMessage(lang, "event.MessageReceivedEvent.detectNSFW.embed.fields.view"), raw_link, true);
                                    event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "event.MessageReceivedEvent.detectNSFW.text").replaceAll("\\$\\{mention\\}", event.getAuthor().getAsMention()))
                                            .setEmbeds(eb.build()).setActionRow(
                                            new MessageGenerate().closeButton(lang),
                                            Button.link(
                                                    new Config(Config.ConfigType.JSON).getString("webUrls.report.nsfw_false_positive")
                                                            .replaceAll("\\$\\{url\\.bot\\}", new Config(Config.ConfigType.JSON).getString("webUrls.bot"))
                                                            .replaceAll("\\$\\{file\\}", String.format("nsfw.%s.%s.", event.getChannel().getId(), event.getMessageId()) + attachment.getFileName())
                                                    , new LanguageUtil().getMessage(lang, "event.MessageReceivedEvent.detectNSFW.buttons.report_nsfw_false_positive")
                                            )
                                    ).complete().delete().queueAfter(30, TimeUnit.SECONDS);
                                    event.getMessage().delete().queue();
                                }
                            }
                        }
                    }
                    final Pattern urlPattern = Pattern.compile("(http://|https://)?[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+", Pattern.CASE_INSENSITIVE);
                    if (urlPattern.matcher(event.getMessage().getContentRaw()).find()) {
                        final Matcher matcher = urlPattern.matcher(event.getMessage().getContentRaw());
                        int i = 0;
                        while (matcher.find()) {
                            String url = matcher.group();
                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder().url(url).get().build();
                            try {
                                Response response = client.newCall(request).execute();
                                if (response.isSuccessful()){
                                    ResponseBody responseBody = response.body();
                                    response.close();
                                    if (responseBody!=null) {
                                        Document doc = Jsoup.parse(responseBody.string());
                                        Elements ogp_img = doc.select("meta[name=\"og:image\"],meta[name=\"thumbnail\"]");
                                        for (Element element : ogp_img) {
                                            String file_name = element.attr("content").split("/")[element.attr("content").split("/").length-1];
                                            List<String> nsfw = checkImageNSFW(event.getTextChannel(), event.getMessageId(), element.attr("content"), i + "." + file_name);
                                            if (nsfw != null && !nsfw.isEmpty()) {
                                                EmbedBuilder eb = new EmbedBuilder();
                                                eb.setTitle(new LanguageUtil().getMessage(lang, "event.MessageReceivedEvent.detectNSFW.embed.title")).setColor(Color.RED);
                                                eb.addField(new LanguageUtil().getMessage(lang, "event.MessageReceivedEvent.detectNSFW.embed.fields.sender"), event.getAuthor().getAsTag(), true);
                                                eb.addField(new LanguageUtil().getMessage(lang, "event.MessageReceivedEvent.detectNSFW.embed.fields.img_size"), "? x ?(URL)", true);
                                                eb.addField(new LanguageUtil().getMessage(lang, "event.MessageReceivedEvent.detectNSFW.embed.fields.detects"), String.join("\n", nsfw), true);
                                                String raw_link = String.format("[Click here!](%s)", new Config(Config.ConfigType.JSON).getString("webUrls.view_nsfw_raw")
                                                        .replaceAll("\\$\\{url\\.bot\\}", new Config(Config.ConfigType.JSON).getString("webUrls.bot"))
                                                        .replaceAll("\\$\\{file\\}", String.format("nsfw.%s.%s.", event.getChannel().getId(), event.getMessageId()) + file_name));
                                                eb.addField(new LanguageUtil().getMessage(lang, "event.MessageReceivedEvent.detectNSFW.embed.fields.view"), raw_link, true);
                                                event.getChannel().sendMessage(new LanguageUtil().getMessage(lang, "event.MessageReceivedEvent.detectNSFW.text").replaceAll("\\$\\{mention\\}", event.getAuthor().getAsMention()))
                                                        .setEmbeds(eb.build()).setActionRow(
                                                        new MessageGenerate().closeButton(lang),
                                                        Button.link(
                                                                new Config(Config.ConfigType.JSON).getString("webUrls.report.nsfw_false_positive")
                                                                        .replaceAll("\\$\\{url\\.bot\\}", new Config(Config.ConfigType.JSON).getString("webUrls.bot"))
                                                                        .replaceAll("\\$\\{file\\}", String.format("nsfw.%s.%s.", event.getChannel().getId(), event.getMessageId()) + file_name)
                                                                , new LanguageUtil().getMessage(lang, "event.MessageReceivedEvent.detectNSFW.buttons.report_nsfw_false_positive")
                                                        )
                                                ).complete().delete().queueAfter(30, TimeUnit.SECONDS);
                                                event.getMessage().delete().queue();
                                            }
                                        }
                                    }
                                }else if (response.isRedirect()){
                                    url = response.request().url().toString();
                                    response.close();
                                    while (true) {
                                        request = new Request.Builder().url(url).build();
                                        response = client.newCall(request).execute();
                                        if (response.isSuccessful()) {

                                            response.close();
                                            break;
                                        }else if (response.isRedirect()) {
                                            url = response.request().url().toString();
                                        }else {
                                            response.close();
                                            break;
                                        }
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            i++;
                        }
                    }
                }
            }
            return true;
        }else return false;
    }

    private List<String> checkImageNSFW(GuildChannel channel, String msgId ,String img_url, String file_name) {
        String out_file_name = String.format("nsfw.%s.%s.", channel.getId(), msgId)+file_name;
        try {
            Request request = new Request.Builder().url(img_url).build();
            Response response = new OkHttpClient().newCall(request).execute();
            if (response.isSuccessful()) {
                Files.createDirectories(new File(".temp").toPath());
                OutputStream output = new FileOutputStream(new File(".temp", out_file_name));
                output.write(response.body().bytes());
                output.close();
                response.close();
            }else {
                return null;
            }
            BufferedImage image = ImageIO.read(new File(".temp", out_file_name));
            BufferedImage tmp = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D off = tmp.createGraphics();
            off.drawImage(image, 0, 0, Color.WHITE, null);
            ImageIO.write(tmp, "jpg", new File(".temp", out_file_name.replaceAll("\\..*$", ".jpg")));
            File jpeg = new File(".temp", out_file_name.replaceAll("\\..*$", ".jpg"));
            RequestBody post = RequestBody.create(jpeg, MediaType.parse("multipart/form-data"));
            String boundary = String.valueOf(System.currentTimeMillis());

            RequestBody requestBody = new MultipartBody.Builder(boundary).setType(MultipartBody.FORM)
                    .addFormDataPart("image", out_file_name, RequestBody.create(MediaType.parse("multipart/form-data"), jpeg))
                    .build();
            request = new Request.Builder().url("http://192.168.10.222:19000/nsfw").post(requestBody).build();
            response = new OkHttpClient().newCall(request).execute();
            ResponseBody responseBody = response.body();
            if (responseBody!=null) {
                String res_str = responseBody.string();
                logger.debug(res_str);
                JSONArray nsfw_json = new JSONArray(res_str);
                double nsfw_porn = 0;
                double nsfw_hentai = 0;
                double nsfw_sexy = 0;
                for (Object o : nsfw_json) {
                    JSONObject _j = new JSONObject(o.toString());
                    if (_j.getString("className").equalsIgnoreCase("Porn")){
                        nsfw_porn = _j.getDouble("probability");
                    }else if (_j.getString("className").equalsIgnoreCase("Hentai")) {
                        nsfw_hentai = _j.getDouble("probability");
                    } else if (_j.getString("className").equalsIgnoreCase("Sexy")) {
                        nsfw_sexy = _j.getDouble("probability");
                    }
                }
                boolean isPorn = false;
                boolean isHentai = false;
                boolean isSexy = false;
                try {
                    isPorn = new ChannelUtil(channel).getNSFWLevel("porn") <= nsfw_porn;
                    isHentai = new ChannelUtil(channel).getNSFWLevel("hentai") <= nsfw_hentai;
                    isSexy = new ChannelUtil(channel).getNSFWLevel("sexy") <= nsfw_sexy;
                } catch (Exception ignored) {
                    if (ignored.getMessage().equalsIgnoreCase("Unregistered channel")){
                        isPorn = new ChannelUtil(channel).getNSFWLevel("porn") <= nsfw_porn;
                        isHentai = new ChannelUtil(channel).getNSFWLevel("hentai") <= nsfw_hentai;
                        isSexy = new ChannelUtil(channel).getNSFWLevel("sexy") <= nsfw_sexy;
                    }
                }
                response.close();
                List<String> res = new ArrayList<>();
                if (isPorn) res.add("Porn");
                if (isHentai) res.add("Hentai");
                if (isSexy) res.add("Sexy");
                return res;
            }else {
                logger.warn("Image "+img_url +" is not supported on api server");
                response.close();
                return null;
            }
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
