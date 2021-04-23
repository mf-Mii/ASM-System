package work.mfmii.other.ASM_System.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.Config;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VerifyUtil {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    public VerifyUtil(){

    }

    public String createLink(VerifyType type, @Nonnull String userId, @Nonnull String guild_id, OffsetDateTime limit) throws Exception {
        String token = createToken(userId, guild_id, limit);
        addVerify(token, type);
        return new Config(Config.ConfigType.JSON).getString("verifies.url")+token;
    }

    public String createLink(List<VerifyType> types, @Nonnull String userId, @Nonnull String guild_id, OffsetDateTime limit) throws Exception {
        String token = createToken(userId, guild_id, limit);
        addVerifies(token, types);
        return new Config(Config.ConfigType.JSON).getString("verifies.url")+token;
    }

    private String createToken(@Nonnull String userId, @Nonnull String guildId, OffsetDateTime limit) throws SQLException {
        Connection con = new MySQLUtil().getConnection();
        String token;

        while (true) {
            token = RandomStringUtils.randomAlphanumeric(128);
            PreparedStatement pstmt = con.prepareStatement("SELECT count(*) AS cnt FROM `verify_token` WHERE `token`=?");
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            if (rs.getInt("cnt")==0) break;
        }
        PreparedStatement pstmt = con.prepareStatement("INSERT INTO `verify_token` (`token`, `user_id`, `guild_id`, `on_create`, `limit_time`) VALUES(?, ?, ?, ?, ?);");
        pstmt.setString(1, token);
        pstmt.setString(2, userId);
        pstmt.setString(3, guildId);
        pstmt.setString(4, OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));
        pstmt.setString(5, limit!=null ? limit.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")) : null);
        pstmt.execute();
        return token;
}

    private boolean addVerify(@Nonnull String token, @Nonnull VerifyType type) throws Exception {
        Connection con = new MySQLUtil().getConnection();
        PreparedStatement pstmt = con.prepareStatement("SELECT `types` FROM `verify_token` WHERE `token`=?");
        pstmt.setString(1, token);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()){
            //現在DBtにいるtypes(id)を収納、追加するだけの状態に
            String _types = rs.getString("types");
            StringBuilder sb1 = new StringBuilder();
            if (_types != null) {
                sb1.append(_types);
                sb1.append(",");
            }

            //IDを生成
            pstmt = con.prepareStatement("SELECT `secret_id` FROM `verify_content`");
            rs = pstmt.executeQuery();
            List<Integer> _ids = new ArrayList<>();
            while (rs.next()){
                _ids.add(rs.getInt("secret_id"));
            }
            int id;
            System.out.println(_ids);
            while (true) {
                id = new Random().nextInt(1000000000);
                System.out.println("id: "+id);
                if (!_ids.contains(id)) break;
            }
            sb1.append(id);
            //IDをDBに追加
            pstmt = con.prepareStatement("INSERT INTO `verify_content` (`secret_id`, `type`) VALUES (?, ?)");
            pstmt.setInt(1, id);
            pstmt.setString(2, type.name());
            pstmt.execute();

            //idをDBtに追加
            pstmt = con.prepareStatement("UPDATE `verify_token` SET `types` = ? WHERE `token`=?");
            pstmt.setString(1, sb1.toString());
            pstmt.setString(2, token);
            pstmt.execute();

            return true;
        }else {
            throw new Exception("Couldn't find verify token on DataBase.");
        }
    }

    private boolean addVerifies(@Nonnull String token, @Nonnull List<VerifyType> types) throws Exception {
        Connection con = new MySQLUtil().getConnection();
        PreparedStatement pstmt = con.prepareStatement("SELECT `types` FROM `verify_token` WHERE `token`=?");
        pstmt.setString(1, token);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()){
            //現在DBtにいるtypes(id)を収納、追加するだけの状態に
            String _types = rs.getString("types");
            StringBuilder sb1 = new StringBuilder();
            if (_types != null) {
                sb1.append(_types);
                sb1.append(",");
            }
            //現在DBにあるidをすべて取得
            pstmt = con.prepareStatement("SELECT `secret_id` FROM `verify_content`");
            rs = pstmt.executeQuery();
            List<Integer> _ids = new ArrayList<>();
            //idを発行
            List<String> new_ids = new ArrayList<>();
            for (VerifyType type: types) {
                while (rs.next()){
                    _ids.add(rs.getInt("id"));
                }
                int id;
                while (true) {
                    id = new Random().nextInt(1000000000);//generate
                    if (!_ids.contains(id) && !new_ids.contains(id)) break;//new_id -> break
                }
                //IDをDBに追加
                pstmt = con.prepareStatement("INSERT INTO `verify_content` (`secret_id`, `type`) VALUES (?, ?)");
                pstmt.setInt(1, id);
                pstmt.setString(2, type.name());
                pstmt.execute();
                new_ids.add(String.valueOf(id));
            }

            sb1.append(String.join(",", new_ids));
            //idをDBtに追加
            pstmt = con.prepareStatement("UPDATE `verify_token` SET `types` = ? WHERE `token`=?");
            pstmt.setString(1, sb1.toString());
            pstmt.setString(2, token);
            pstmt.execute();

            rs.close();
            pstmt.close();
            con.close();

            return true;
        }else {
            throw new Exception("Verify token not found on DataBase.");
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
