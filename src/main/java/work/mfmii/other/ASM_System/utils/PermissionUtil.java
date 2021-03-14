package work.mfmii.other.ASM_System.utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionUtil {
    public PermissionUtil(){}

    public Map<String, Boolean> getPermissionsByGuildId(String guildId){
        return getPermissions(guildId, null, null);
    }

    public Map<String, Boolean> getPermissionsByChannelId(String channelId){
        return getPermissions(null, channelId, null);
    }

    public Map<String, Boolean> getPermissionsByUserId(String userId){
        return getPermissions(null, null, userId);
    }

    public Map<String, Boolean> getPermissions(String guildId, String channelId, String userId){
        try {
            Connection con = DriverManager.getConnection(new MySQLUtil().getUrl(), new MySQLUtil().getUser(), new MySQLUtil().getPassword());
            StringBuilder sql_build = new StringBuilder();
            sql_build.append("SELECT * FROM `dc_perm_each`");
            List<String> value_list = new ArrayList<>();
            if(!(guildId.isEmpty() && channelId.isEmpty() && userId.isEmpty())){
                sql_build.append(" WHERE ");
                boolean isFirstArg = true;
                if(!guildId.isEmpty()){
                    isFirstArg = false;
                    sql_build.append("`guild_id`=?");
                    value_list.add(guildId);
                }
                if(!channelId.isEmpty()){
                    if (!isFirstArg) sql_build.append(" AND ");
                    else isFirstArg = false;
                    sql_build.append("`channel_id`=?");
                    value_list.add(channelId);
                }
                if(!userId.isEmpty()){
                    if (!isFirstArg) sql_build.append(" AND ");
                    else isFirstArg = false;
                    sql_build.append("`user_id`=?");
                    value_list.add(userId);
                }
            }
            PreparedStatement pstmt = con.prepareStatement(sql_build.toString());
            for (int i = 0; i < value_list.size(); i++) {
                pstmt.setString(i+1, value_list.get(i));
            }
            ResultSet rs = pstmt.executeQuery();
            Map<String, Boolean> result = new HashMap<>();
            while (rs.next()){
                result.put(rs.getString("permission"), rs.getBoolean("value"));
            }
            return result;


        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }

    }
}
