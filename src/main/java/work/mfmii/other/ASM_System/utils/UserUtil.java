package work.mfmii.other.ASM_System.utils;

import net.dv8tion.jda.api.entities.User;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import work.mfmii.other.ASM_System.ASMSystem;
import work.mfmii.other.ASM_System.Config;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserUtil {
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

    public JSONObject getUserdataFromHttpApi(){
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

}
