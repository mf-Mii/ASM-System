package work.mfmii.other.ASM_System.utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GuildUtil {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    private String guildId;
    public GuildUtil(String id){
        this.guildId = id;
    }
    public GuildUtil(Guild guild){
        this.guildId = guild.getId();
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
                pstmt = con.prepareStatement("INSERT INTO `dc_guild` (`id`, `joined`, `lang`) VALUES (?, ?, ?)");
                pstmt.setString(1, guildId);
                pstmt.setBoolean(2, true);
                pstmt.setString(3, lang);
            }else {
                pstmt = con.prepareStatement("UPDATE `dc_guild` SET `joined`=? WHERE `id`=?");
                pstmt.setBoolean(1, true);
                pstmt.setString(2, guildId);
            }
            boolean res = pstmt.execute();
            pstmt.close();
            con.close();
            return res;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public LanguageUtil.Language getLanguage() throws Exception {
        Connection con = new MySQLUtil().getConnection();
        PreparedStatement pstmt = con.prepareStatement("SELECT `lang` FROM `dc_guild` WHERE `id`=?");
        pstmt.setString(1, guildId);
        ResultSet rs = pstmt.executeQuery();
        LanguageUtil.Language res = LanguageUtil.Language.OTHER;
        if (rs.next()) {
            res = LanguageUtil.Language.fromKey(rs.getString("lang"));
            rs.close();
            pstmt.close();
            con.close();
            return res;
        }else {
            rs.close();
            pstmt.close();
            con.close();
            throw new Exception("Unregistered guild");
        }
    }

    public double getReputation() throws Exception {
        Connection con = new MySQLUtil().getConnection();
        PreparedStatement pstmt = con.prepareStatement("SELECT `reputation` FROM `dc_guild` WHERE `id`=?");
        pstmt.setString(1, guildId);
        ResultSet rs = pstmt.executeQuery();
        double result;
        if(rs.next()){
            result = rs.getDouble("reputation");
            rs.close();
            pstmt.close();
            con.close();
            return result;
        } else {
            rs.close();
            pstmt.close();
            con.close();
            throw new Exception("Unregistered guild");
        }
    }

    public double getMinMemberReputation() throws Exception {
        Connection con = new MySQLUtil().getConnection();
        PreparedStatement pstmt = con.prepareStatement("SELECT `member_reputation` FROM `dc_guild` WHERE `id`=?;");
        pstmt.setString(1, guildId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) return rs.getDouble("member_reputation");
        else {
            rs.close();
            pstmt.close();
            con.close();
            throw new Exception("Unregistered guild");
        }
    }

    public boolean isBanned() throws Exception {
        Connection con = new MySQLUtil().getConnection();
        PreparedStatement pstmt = con.prepareStatement("SELECT `banReason` FROM `dc_guild` WHERE `id`=?;");
        pstmt.setString(1, guildId);
        ResultSet rs = pstmt.executeQuery();
        if(rs.next()) {
            rs.close();
            pstmt.close();
            con.close();
            return rs.getString("banReason") != null;
        }
        else {
            rs.close();
            pstmt.close();
            con.close();
            throw new Exception("Unregistered guild");
        }
    }

    public String getBanReason() throws Exception {
        Connection con = new MySQLUtil().getConnection();
        PreparedStatement pstmt = con.prepareStatement("SELECT `banReason` FROM `dc_guild` WHERE `id`=?;");
        pstmt.setString(1, guildId);
        ResultSet rs = pstmt.executeQuery();
        if(rs.next()) {
            rs.close();
            pstmt.close();
            con.close();
            return rs.getString("banReason");
        }
        else {
            rs.close();
            pstmt.close();
            con.close();
            throw new Exception("Unregistered guild");
        }
    }
    public List<String> getModeratorChannelIds() throws Exception {
        Connection con = new MySQLUtil().getConnection();
        PreparedStatement pstmt = con.prepareStatement("SELECT * FROM `dc_channels` WHERE `guild_id`=? AND `isModeration`=?");
        pstmt.setString(1, guildId);
        pstmt.setBoolean(2, true);
        ResultSet rs = pstmt.executeQuery();
        List<String> res = new ArrayList<>();
        while (rs.next()) {
            res.add(rs.getString("ch_id"));
        }
        rs.close();
        pstmt.close();
        con.close();
        return res;
    }

    public List<String> getWelcomeChannelIds() throws Exception {
        Connection con = new MySQLUtil().getConnection();
        PreparedStatement pstmt = con.prepareStatement("SELECT * FROM `dc_channels` WHERE `guild_id`=? AND `showWelcome`=?");
        pstmt.setString(1, guildId);
        pstmt.setBoolean(2, true);
        ResultSet rs = pstmt.executeQuery();
        List<String> res = new ArrayList<>();
        while (rs.next()) {
            res.add(rs.getString("ch_id"));
        }
        rs.close();
        pstmt.close();
        con.close();
        return res;
    }

    public List<String> getByeChannelIds() throws Exception {
        Connection con = new MySQLUtil().getConnection();
        PreparedStatement pstmt = con.prepareStatement("SELECT * FROM `dc_channels` WHERE `guild_id`=? AND `shoBye`=?");
        pstmt.setString(1, guildId);
        pstmt.setBoolean(2, true);
        ResultSet rs = pstmt.executeQuery();
        List<String> res = new ArrayList<>();
        while (rs.next()) {
            res.add(rs.getString("ch_id"));
        }
        rs.close();
        pstmt.close();
        con.close();
        return res;

    }

    public String getWelcomeMessage(GuildMemberJoinEvent event) throws Exception {
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
            rs.close();
            pstmt.close();
            con.close();
            return res;
        } else {
            rs.close();
            pstmt.close();
            con.close();
            throw new Exception("Unregistered guild");
        }
    }

    public String getByeMessage(GuildMemberJoinEvent event) throws Exception {
        Connection con = new MySQLUtil().getConnection();
        PreparedStatement pstmt = con.prepareStatement("SELECT `byeMessage` FROM `dc_guild` WHERE `id`=?");
        pstmt.setString(1, guildId);
        ResultSet rs = pstmt.executeQuery();
        if(rs.next()) {

            String res = rs.getString("byeMessage");
            res = res.replaceAll("%userAsTag%", event.getUser().getAsTag())
                    .replaceAll("%guildName%", event.getGuild().getName())
                    .replaceAll("%userId%", event.getUser().getId())
                    .replaceAll("%memberCount%", String.valueOf(event.getGuild().getMembers().size()));
            rs.close();
            pstmt.close();
            con.close();
            return res;
        } else {
            rs.close();
            pstmt.close();
            con.close();
            throw new Exception("Unregistered guild");
        }
    }

    public double getNSFWLevel(String type) throws Exception {
        if (type.equalsIgnoreCase("porn") || type.equalsIgnoreCase("hentai") || type.equalsIgnoreCase("sexy")){
            Connection con = new MySQLUtil().getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT * FROM `dc_guild` WHERE `id`=?");
            pstmt.setString(1, guildId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double res = rs.getDouble(String.format("nsfw_lev_%s", type.toLowerCase()));
                rs.close();
                pstmt.close();
                con.close();
                return res;
            }else {
                rs.close();
                pstmt.close();
                con.close();
                throw new Exception("Unregistered guild");
            }
        }else {
            throw new Exception("Unsupported type");
        }
    }


}
