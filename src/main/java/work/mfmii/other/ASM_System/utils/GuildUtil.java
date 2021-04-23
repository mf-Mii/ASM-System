package work.mfmii.other.ASM_System.utils;

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GuildUtil {
    Logger logger = LoggerFactory.getLogger(this.getClass());
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
                return LanguageUtil.Language.OTHER;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return LanguageUtil.Language.OTHER;
        }
    }

    public double getReputation(){
        try {
            Connection con = new MySQLUtil().getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT `reputation` FROM `dc_guild` WHERE `id`=?");
            pstmt.setString(1, guildId);
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

    public boolean isBanned(){
        try {
            Connection con = new MySQLUtil().getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT `banReason` FROM `dc_guild` WHERE `id`=?;");
            pstmt.setString(1, guildId);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) return rs.getString("banReason") != null;
            else return false;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public String getBanReason(){
        try {
            Connection con = new MySQLUtil().getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT `banReason` FROM `dc_guild` WHERE `id`=?;");
            pstmt.setString(1, guildId);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) return rs.getString("banReason");
            else return "#ERROR GUILD_NOT_FOUND";
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return "#ERROR "+throwables.getMessage();
        }
    }
    public String getModeratorChannelId(){
        try {
            Connection con = new MySQLUtil().getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT `moderatorChannel` FROM `dc_guild` WHERE `id`=?");
            pstmt.setString(1, guildId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("moderatorChannel");
            else return "#ERROR GUILD_NOT_FOUND";
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return "#ERROR "+throwables.getMessage();
        }
    }
    public String getWelcomeChannelId(){
        try {
            Connection con = new MySQLUtil().getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT `moderatorChannel` FROM `dc_guild` WHERE `id`=?");
            pstmt.setString(1, guildId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("moderatorChannel");
            else return "#ERROR GUILD_NOT_FOUND";
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return "#ERROR "+throwables.getMessage();
        }
    }
    public String getWelcomeMessage(GuildMemberJoinEvent event){
        try {
            Connection con = new MySQLUtil().getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT `welcomeMessage` FROM `dc_guild` WHERE `id`=?");
            pstmt.setString(1, guildId);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()) {

                String res = rs.getString("welcomeMessage");
                res = res.replaceAll("%userAsTag%", event.getUser().getAsTag())
                        .replaceAll("%guildName%", event.getGuild().getName())
                        .replaceAll("%userId%", event.getUser().getId())
                        .replaceAll("%memberCount%", String.valueOf(event.getGuild().getMembers().size()));
                return res;
            }
            else return "#ERROR GUILD_NOT_FOUND";
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return "#ERROR "+throwables.getMessage();
        }
    }


}
