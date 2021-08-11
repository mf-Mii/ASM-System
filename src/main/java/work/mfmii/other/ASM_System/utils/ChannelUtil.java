package work.mfmii.other.ASM_System.utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.ASMSystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChannelUtil {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    private String channelId;
    public ChannelUtil(String id) throws Exception {
        if (ASMSystem.jda.getGuildChannelById(id)==null){
            throw new Exception("Channel must be GuildChannel");
        }
        this.channelId = id;
    }
    public ChannelUtil(GuildChannel guildChannel){
        this.channelId = guildChannel.getId();
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public boolean register(){
        try {
            Connection con = new MySQLUtil().getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT count(*) AS cnt FROM `dc_channels` WHERE `ch_id`=?");
            pstmt.setString(1, channelId);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            boolean res;
            if(rs.getInt("cnt") == 0){
                pstmt = con.prepareStatement("INSERT INTO `dc_channels` (`ch_id`, `guild_id`) VALUES (?, ?)");
                pstmt.setString(1, channelId);
                pstmt.setString(2, ASMSystem.jda.getGuildChannelById(channelId).getGuild().getId());
                res = pstmt.execute();
            }else {
                res = true;
            }
            pstmt.close();
            con.close();
            return res;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public boolean isRegistered() {
        try {
            Connection con = new MySQLUtil().getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT count(*) AS cnt FROM `dc_channels` WHERE `ch_id`=?");
            pstmt.setString(1, channelId);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            boolean res = rs.getInt("cnt") != 0;
            rs.close();
            pstmt.close();
            con.close();
            return res;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public Guild getGuild() throws Exception {
        return ASMSystem.jda.getGuildById(getGuildId());
    }

    public String getGuildId() throws Exception {
        Connection con = new MySQLUtil().getConnection();
        PreparedStatement pstmt = con.prepareStatement("SELECT `guild_id` FROM `dc_channels` WHERE `ch_id`=?");
        pstmt.setString(1, channelId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()){
            String res = rs.getString("guild_id");
            rs.close();
            pstmt.close();
            con.close();
            return res;
        }else {
            rs.close();
            pstmt.close();
            con.close();
            throw new Exception("Unregistered channel");
        }
    }

    public int getRateLimit() throws Exception {
        Connection con = new MySQLUtil().getConnection();
        PreparedStatement pstmt = con.prepareStatement("SELECT rate FROM `dc_channels` WHERE `ch_id`=?");
        pstmt.setString(1, channelId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            int rate = rs.getInt("rate");
            rs.close();
            pstmt.close();
            con.close();
            return rate;
        } else {
            rs.close();
            pstmt.close();
            con.close();
            throw new Exception("Unregistered channel");
        }
    }

    public boolean isModerationChannel() throws Exception {
        Connection con = new MySQLUtil().getConnection();
        PreparedStatement pstmt = con.prepareStatement("SELECT isModeration FROM `dc_channels` WHERE `ch_id`=?");
        pstmt.setString(1, channelId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            boolean res = rs.getBoolean("idModeration");
            rs.close();
            pstmt.close();
            con.close();
            return res;
        } else {
            rs.close();
            pstmt.close();
            con.close();
            throw new Exception("Unregistered channel");
        }
    }

    public boolean isShowWelcomeChannel() throws Exception {
        Connection con = new MySQLUtil().getConnection();
        PreparedStatement pstmt = con.prepareStatement("SELECT showWelcome FROM `dc_channels` WHERE `ch_id`=?");
        pstmt.setString(1, channelId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            boolean res = rs.getBoolean("showWelcome");
            rs.close();
            pstmt.close();
            con.close();
            return res;
        } else {
            rs.close();
            pstmt.close();
            con.close();
            throw new Exception("Unregistered channel");
        }
    }

    public boolean isShowByeChannel() throws Exception {
        Connection con = new MySQLUtil().getConnection();
        PreparedStatement pstmt = con.prepareStatement("SELECT showBye FROM `dc_channels` WHERE `ch_id`=?");
        pstmt.setString(1, channelId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            boolean res = rs.getBoolean("showBye");
            rs.close();
            pstmt.close();
            con.close();
            return res;
        } else {
            rs.close();
            pstmt.close();
            con.close();
            throw new Exception("Unregistered channel");
        }
    }



    /////////          NSFW         ///////////
    public double getNSFWLevel(String type) throws Exception {
        if (type.equalsIgnoreCase("porn") || type.equalsIgnoreCase("hentai") || type.equalsIgnoreCase("sexy")){
            Connection con = new MySQLUtil().getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT * FROM `dc_channels` WHERE `ch_id`=?");
            pstmt.setString(1, channelId);
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
                return new GuildUtil(getGuildId()).getNSFWLevel(type);
            }
        }else {
            throw new Exception("Unsupported type");
        }
    }
}
