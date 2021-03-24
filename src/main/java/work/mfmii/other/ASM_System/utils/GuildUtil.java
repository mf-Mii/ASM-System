package work.mfmii.other.ASM_System.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GuildUtil {
    private String guildId;
    public GuildUtil(String id){
        this.guildId = id;
    }

    public String getGuildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public boolean register(String lang){
        try {
            Connection con = new MySQLUtil().getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT count(*) AS cnt FROM `dc_guild` WHERE `id`=?");
            pstmt.setString(1, guildId);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            if(rs.getInt("cnt") == 0){
                pstmt = con.prepareStatement("INSERT INTO `dc_guild` (`id`, `invited`, `lang`) VALUES (?, ?, ?)");
                pstmt.setString(1, guildId);
                pstmt.setBoolean(2, true);
                pstmt.setString(3, lang);
            }else {
                pstmt = con.prepareStatement("UPDATE `dc_guild` SET `invited`=1 WHERE `id`=?");
                pstmt.setString(1, guildId);
            }
            return pstmt.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public LanguageUtil.Language getLanguage(){
        try {
            Connection con = new MySQLUtil().getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT `lang` FROM `dc_guild` WHERE `id`=?");
            pstmt.setString(1, guildId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
                return LanguageUtil.Language.fromKey(rs.getString("lang"));
            else
                return null;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }
}
