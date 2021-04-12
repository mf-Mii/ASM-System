package work.mfmii.other.ASM_System.utils;

import org.apache.commons.lang3.RandomStringUtils;
import work.mfmii.other.ASM_System.Config;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class VerifyUtil {
    public VerifyUtil(){

    }

    public String createLink(VerifyType type, @Nonnull String userId, @Nonnull String guild_id){
        try {
            Connection con = new MySQLUtil().getConnection();
            PreparedStatement pstmt;
            if (type == VerifyType.EMAIL) {
                pstmt = con.prepareStatement("SELECT `mail` FROM `web_user` WHERE `discord_id`=?");
                pstmt.setString(1, userId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    String mail = rs.getString("mail");
                    if (mail == null) {
                        throw new Exception("Mail Address is null");
                    }
                }
                String token = insertToken(userId, guild_id, type);
                return new Config(Config.ConfigType.JSON).getString("verifies.mail"+token);
            }

            if (type == VerifyType.DISCORD) {

            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return "#ERROR SQL:" + throwables.getMessage();
        } catch (Exception exception) {
            exception.printStackTrace();
            return "#ERROR " + exception.getMessage();
        }
        return null;
    }

    private String insertToken(@Nonnull String userId, @Nonnull String guildId, @Nonnull VerifyType type){
        try {
            Connection con = new MySQLUtil().getConnection();
            String token = RandomStringUtils.randomAlphanumeric(128);
            PreparedStatement pstmt = con.prepareStatement("INSERT INTO `verify_key` (`type`,`token`, `user_id`, `guild_id`, `added`) VALUES(?, ?, ?, ?);");
            pstmt.setString(1, type.getKey());
            pstmt.setString(2, token);
            pstmt.setString(3, userId);
            pstmt.setString(4, guildId);
            pstmt.setString(5, OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
            pstmt.execute();
            return token;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return "#ERROR SQL:"+throwables.getMessage();
        }
    }


    public enum VerifyType{
        EMAIL("email"),
        GOOGLE("google"),
        FACEBOOK("facebook"),
        reCAPTCHA("recaptcha"),
        hCAPTCHA("hcaptcha"),
        DISCORD("discord"),
        PHONE("phone"),
        SMS("sms"),
        KEYWORD("keyword"),
        NONE("none"),
        OTHER("");

        private final String key;

        public String getKey() {
            return key;
        }
        VerifyType(String key){
            this.key = key;
        }


        public String toString() {
            return "VerifyTypes{" +
                    "key='" + key + '\'' +
                    '}';
        }
        public static VerifyType fromKey(String key){
            for (VerifyType type: values()) {
                if(type.equals(key.toLowerCase())){
                    return type;
                }
            }
            return OTHER;
        }
    }
}
