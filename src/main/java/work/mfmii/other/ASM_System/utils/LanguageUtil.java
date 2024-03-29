package work.mfmii.other.ASM_System.utils;

import net.dv8tion.jda.api.entities.User;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.Config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class LanguageUtil {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    public LanguageUtil(){

    }

    public String getMessage(Language lang, String key){
        String message_raw = new FileUtil().readFile(new FileUtil().getFile(String.format("message/%s.json", lang.getKey())), "utf8");

        List<String> keys = Arrays.asList(key.split("\\.").clone());
        JSONObject temp = new JSONObject(message_raw);
        for (int i = 0; i <= keys.size() - 1; i++) {
            if (i == keys.size() - 1) {
                return temp.getString(keys.get(i));
            }
            temp = temp.getJSONObject(keys.get(i));
        }
        return null;
    }

    public Language getUserLanguageById(String userId){
        try {
            Connection con = new MySQLUtil().getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT lang FROM dc_user WHERE id=?");
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            Language res;
            if(rs.next()){
                for (Language l: Language.values()) {
                    if(l.getKey().toLowerCase().equals(rs.getString("lang").toLowerCase())){
                        rs.close();
                        pstmt.close();
                        con.close();
                        return l;
                    }
                }
            }
            rs.close();
            pstmt.close();
            con.close();
            return Language.DEFAULT;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return Language.DEFAULT;
        }
    }

    public Language getUserLanguage(User user){
        return getUserLanguageById(user.getId());
    }

    public enum Language{
        ENGLISH("en"),
        JAPANESE("ja"),
        CHINESE("zh"),
        OTHER(new Config(Config.ConfigType.JSON).getString("default_language")),
        DEFAULT(new Config(Config.ConfigType.JSON).getString("default_language"));

        private final String key;

        Language(String key){
            this.key = key;
        }

        public String getKey(){
            return key;
        }

        public static Language fromKey(String key){
            for (Language type: values()) {
                if(type.equals(key.toLowerCase())){
                    return type;
                }
            }
            return OTHER;
        }
    }

}
