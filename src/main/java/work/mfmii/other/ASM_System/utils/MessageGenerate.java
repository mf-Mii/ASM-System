package work.mfmii.other.ASM_System.utils;

import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.Config;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MessageGenerate {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    public MessageGenerate(){
    }

    public MessageEmbed errorMessageAsEmbed(Exception exception){
        String stack = "";
        for (StackTraceElement stackTraceElement : exception.getStackTrace()) {
            if (stack.length() >= 1500) {
                stack += "[more]...";
                break;
            }
            if (!stack.isEmpty()) stack += "\n        ";
            stack += stackTraceElement.getClassName()+".class ("+stackTraceElement.getMethodName()+"() :"+stackTraceElement.getLineNumber()+")";
        }
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Error: "+exception.getMessage())
                .setDescription("```"+exception.getClass().getName()+""+exception.getMessage()+"\n"+stack+"```")
                .setFooter("ASM Bot by mf_Mii | "+ OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
        return eb.build();
    }

    public String errorMessageAsText(Exception exception){

        String stack = "";
        for (StackTraceElement stackTraceElement : exception.getStackTrace()) {
            if (stack.length() >= 1500) {
                stack += "[more]...";
                break;
            }
            if (!stack.isEmpty()) stack += "\n        ";
            stack += stackTraceElement.getClassName()+".class ("+stackTraceElement.getMethodName()+"() :"+stackTraceElement.getLineNumber()+")";
        }
        StringBuilder sb1 = new StringBuilder();
        sb1.append("**Error: ")
                .append(exception.getMessage())
                .append("**\n")
                .append("```")
                .append(stack)
                .append("```");
        return sb1.toString();
    }
    
    public byte[] textToSpeech(@Nonnull final String text, String lang) throws IOException {
        if (lang == null || lang.isEmpty()){
            lang = checkLanguage(text);
        }
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(new Config(Config.ConfigType.JSON).getString("api.speech").replaceAll("\\$\\{text\\}", text).replaceAll("\\$\\{lang\\}", lang))
                .build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful())
        return response.body().bytes();
        else return null;
    }

    public File textToSpeechAsFile(@Nonnull final String text, String lang) throws IOException {
        if (lang == null || lang.isEmpty()){
            lang = checkLanguage(text);
        }
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(new Config(Config.ConfigType.JSON).getString("api.speech").replaceAll("\\$\\{text\\}", text).replaceAll("\\$\\{lang\\}", lang))
                .build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            InputStream is = response.body().byteStream();
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            while (true) {
                String id = RandomStringUtils.randomAlphabetic(32);
                File target = new File(".temp/" + id + ".mp3");
                if (!target.exists()) {
                    Files.copy(is, target.toPath());
                    response.body().close();
                    return target;
                }
            }
        }else return null;
    }

    public static String checkLanguage(@Nonnull final String text) throws IOException {
        List<LdLocale> list = new ArrayList<>();
        list.add(LdLocale.fromString("ja"));
        list.add(LdLocale.fromString("en"));
        List<LanguageProfile> languageProfiles = new LanguageProfileReader().readBuiltIn(list);
        LanguageDetector languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .withProfiles(languageProfiles)
                .build();
        TextObjectFactory textObjectFactory;
        if (text.length() < 150){
            textObjectFactory = CommonTextObjectFactories.forDetectingShortCleanText();
        }else {
            textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
        }
        TextObject textObject = textObjectFactory.forText(text);
        return languageDetector.getProbabilities(textObject).get(0).getLocale().getLanguage();
    }

    public enum MessageType{
        EMBED("embed"),
        TEXT("text"),
        AUDIO("audio");

        private final String key;

        MessageType(String key){
            this.key = key;
        }

        public String getKey(){
            return this.key;
        }

        public String toString() {
            return super.toString();
        }
    }
}

