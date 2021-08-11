package work.mfmii.other.ASM_System.utils;

import net.dv8tion.jda.api.entities.User;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.ASMSystem;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class UserUtil {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String userId;
    public UserUtil(@NotNull String userId){
        this.userId = userId;
    }

    public UserUtil(@NotNull User user){
        this.userId = user.getId();
    }

    public LanguageUtil.Language getLanguage(){
        return new LanguageUtil().getUserLanguageById(userId);
    }

    public double getReputation(){
        try {
            Connection con = new MySQLUtil().getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT `reputation` FROM `dc_user` WHERE `id`=?");
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            double result;
            if(rs.next()) result = rs.getDouble("reputation");
            else {
                result = -127;
            }
            return result;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return -125;
        }
    }



    public JSONObject getUserdataFromHttpApi() {
        Request request = new Request.Builder()
                .url(String.format("https://discord.com/api/v8/users/%s", userId))
                .addHeader("Authorization", ASMSystem.jda.getToken())
                .build();
        Response response = null;
        try {
            response = ASMSystem.jda.getHttpClient().newCall(request).execute();
            return new JSONObject(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Reputation> getReputationLog() throws SQLException {
        List<Reputation> list = new ArrayList<>();
        Connection con = new MySQLUtil().getConnection();
        PreparedStatement pstmt = con.prepareStatement("SELECT * FROM `user_reputation_log` WHERE `user_id`=?;");
        pstmt.setString(1, userId);
        ResultSet rs = pstmt.executeQuery();
        while (rs.next()) {
            OffsetDateTime dateTime = OffsetDateTime.ofInstant(rs.getTimestamp("dateTime").toInstant(), ZoneId.systemDefault());
            Reputation rep = new Reputation(userId, rs.getString("reason"), dateTime, rs.getDouble("old_val"), rs.getDouble("now_val"));
            list.add(rep);
        }
        return list;
    }

    public static class Reputation{
        String userid;
        String reason;
        OffsetDateTime dateTime;
        double from_val;
        double val;

        public Reputation(String userId, String reason, OffsetDateTime dateTime, double from_val, double val){
            this.userid = userId;
            this.reason = reason;
            this.dateTime = dateTime;
            this.from_val = from_val;
            this.val = val;
        }

        public String getUserId() {
            return userid;
        }

        public String getReason() {
            return reason;
        }

        public OffsetDateTime getDateTime() {
            return dateTime;
        }

        public double getFrom_val() {
            return from_val;
        }

        public double getVal() {
            return val;
        }
    }

}
