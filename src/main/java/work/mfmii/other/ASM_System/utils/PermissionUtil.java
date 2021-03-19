package work.mfmii.other.ASM_System.utils;

import org.jetbrains.annotations.NotNull;
import work.mfmii.other.ASM_System.Config;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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
        Map<String, Boolean> result = new HashMap<>();

        if((guildId == null || guildId.length() != 18) && (channelId == null || channelId.length() != 18) && (userId == null || userId.length() != 18)){
            return result;
        }
        try {
            Connection con = new MySQLUtil().getConnection();
            /*
            StringBuilder sql_build = new StringBuilder();
            sql_build.append("SELECT * FROM `dc_perm_each` WHERE ");
            List<String> value_list = new ArrayList<>();
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

            PreparedStatement pstmt = con.prepareStatement(sql_build.toString());
            for (int i = 0; i < value_list.size(); i++) {
                pstmt.setString(i+1, value_list.get(i));
            }
             */
            String sql = "SELECT * FROM `dc_perm_each` WHERE `guild_id`=? AND `channel_id`=? AND `user_id`=?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, (guildId == null || guildId.length() != 18) ? "global" : guildId);
            pstmt.setString(2, (channelId == null || channelId.length() != 18) ? "global" : channelId);
            pstmt.setString(3, (userId == null || userId.length() != 18) ? "global" : channelId);
            ResultSet rs = pstmt.executeQuery();
            Map<String, Integer> permsLev = new HashMap<>();
            while (rs.next()){
                int level = 0;
                boolean _isgglobal = rs.getString("guild_id").equalsIgnoreCase("global");
                boolean _iscglobal = rs.getString("channel_id").equalsIgnoreCase("global");
                boolean _isuglobal = rs.getString("user_id").equalsIgnoreCase("global");
                boolean _isgroup = false;
                if (rs.getString("permission").startsWith("group.")) {
                    _isgroup = true;
                }
                //権限のレベルを設定
                if (!_isgglobal && _iscglobal && _isuglobal && _isgroup) level=1;
                if (!_isgglobal && _iscglobal && _isuglobal && !_isgroup) level=2;
                if (!_iscglobal && _isuglobal && _isgroup) level = 3;
                if (!_iscglobal && _isuglobal && !_isgroup) level = 4;
                if (!_isgglobal && _iscglobal && !_isuglobal && _isgroup) level = 5;
                if (!_isgglobal && _iscglobal && !_isuglobal && !_isgroup) level = 6;
                if (!_iscglobal && !_isuglobal && _isgroup) level = 7;
                if (!_iscglobal && !_isuglobal && !_isgroup) level = 8;
                if (_isgglobal && _iscglobal && !_isuglobal && _isgroup) level = 9;
                if (_isgglobal && _iscglobal && !_isuglobal && !_isgroup) level = 10;


                if(!result.containsKey(rs.getString("permission"))){
                    result.put(rs.getString("permission"), rs.getBoolean("value"));
                    permsLev.put(rs.getString("permission"), level);
                }else {//すでに権限がある場合、レベルが高ければ上書きする
                    if(permsLev.get(rs.getString("permission")) < level){
                        result.put(rs.getString("permission"), rs.getBoolean("value"));
                        permsLev.put(rs.getString("permission"), level);
                    }
                }
            }
            rs.close();
            pstmt.close();
            con.close();
            return result;


        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    public boolean hasPermission(String guildId, String channelId, String userId, @NotNull String permission){
        if((guildId == null || guildId.length() != 18) && (channelId == null || channelId.length() != 18) && (userId == null || userId.length() != 18)){
            return false;
        }
        try {
            boolean addGid = true;
            boolean addUid = false;
            String _gid = (guildId == null || guildId.length() != 18) ? "global" : guildId;
            String _cid = (channelId == null || channelId.length() != 18) ? "global" : channelId;
            String _uid = (userId == null || userId.length() != 18) ? "global" : userId;
            if (!_cid.equals("global")) addGid = false;
            if (!_uid.equals("global")) addUid = true;
            Connection con = new MySQLUtil().getConnection();
            StringBuilder sql = new StringBuilder()
                    .append("SELECT * FROM `dc_perm_each` WHERE ");
            if(addGid) sql.append("(`guild_id`=? OR `guild_ud`='global')AND ");
            if(addUid) sql.append("(`user_id`='global' OR `user_id`=?) AND ");
            else sql.append("`user_id`='global' AND ");
            sql.append("(`channel_id`=? OR `channel_id`='global') AND (`permission`=? OR `permission` LIKE 'group.%' OR `permission` LIKE '%.*')");
            PreparedStatement pstmt = con.prepareStatement(sql.toString());
            int added = 0;
            if (addGid) pstmt.setString(++added, _gid);
            if (addUid) pstmt.setString(++added, _uid);
            pstmt.setString(++added, _cid);
            pstmt.setString(++added, permission);
            /*
            if(addGid) {
                pstmt.setString(1, _gid);
                pstmt.setString(2, _cid);
                pstmt.setString(3, permission);
            }else{
                pstmt.setString(1, _cid);
                pstmt.setString(2, permission);
            }

             */
            ResultSet rs = pstmt.executeQuery();

            AtomicBoolean result = new AtomicBoolean(false);
            boolean _uglobal = true;
            boolean _cglobal = true;
            boolean _gglobal = true;
            String[] _ids = {"","",""};
            List<String> groups = new ArrayList<>();
            int last_lev = 0;
            while (rs.next()){

                int level = 0;
                boolean _isgglobal = rs.getString("guild_id").equalsIgnoreCase("global");
                boolean _iscglobal = rs.getString("channel_id").equalsIgnoreCase("global");
                boolean _isuglobal = rs.getString("user_id").equalsIgnoreCase("global");
                boolean _isgroup = false;
                if (rs.getString("permission").startsWith("group.")) _isgroup = true;

                //権限のレベルを設定
                if (!_isgglobal && _iscglobal && _isuglobal && _isgroup) level=1;
                if (!_isgglobal && _iscglobal && _isuglobal && !_isgroup) level=2;
                if (!_iscglobal && _isuglobal && _isgroup) level = 3;
                if (!_iscglobal && _isuglobal && !_isgroup) level = 4;
                if (!_isgglobal && _iscglobal && !_isuglobal && _isgroup) level = 5;
                if (!_isgglobal && _iscglobal && !_isuglobal && !_isgroup) level = 6;
                if (!_iscglobal && !_isuglobal && _isgroup) level = 7;
                if (!_iscglobal && !_isuglobal && !_isgroup) level = 8;
                if (_isgglobal && _iscglobal && !_isuglobal && _isgroup) level = 9;
                if (_isgglobal && _iscglobal && !_isuglobal && !_isgroup) level = 10;

                if (last_lev < level){
                    if(rs.getString("permission").equalsIgnoreCase(permission)) {
                        result.set(rs.getBoolean("value"));
                        last_lev = level;
                    }
                    if (rs.getString("permission").startsWith("group.")) {//グループの場合、groupsに追加
                        if(hasPermissionInGroup(rs.getString("permission").replaceFirst("group\\.", ""), permission)){
                            result.set(rs.getBoolean("value"));
                            last_lev = level;
                        }
                    }
                    if(rs.getString("permission").endsWith(".*")){//*があった場合の一致確認
                        String[] permissions = permission.split("\\.");
                        String[] _perms = rs.getString("permission").split("\\.");
                        for (int i = 0; i < permissions.length; i++) {
                            if(permissions[i] != _perms[i]){
                                if(_perms.length-1 == i && _perms[i].equals("*")){
                                    result.set(rs.getBoolean("value"));
                                    last_lev = level;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            rs.close();
            pstmt.close();
            con.close();

            return result.get();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public Map<String, Map<String, Boolean>> getGroupsPermissions(List<String> names){
        Map<String, Map<String, Boolean>> result = new HashMap<>();
        for (int i = 0; i < names.size(); i++) {
            result.put(names.get(i), getGroupPermissions(names.get(i)));
        }
        return result;
    }

    public Map<String, Boolean> getGroupPermissions(@NotNull String name){
        Map<String, Boolean> result = new HashMap<>();
        if(name.isEmpty()){
            return result;
        }
        try {
            Connection con = new MySQLUtil().getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT * FROM `dc_perm_group_perms` WHERE `name`=?");
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            Map<String, Integer> permsLev = new HashMap<>();
            while (rs.next()){
                int level = 0;
                boolean _isgglobal = rs.getString("guild_id").equalsIgnoreCase("global");
                boolean _iscglobal = rs.getString("channel_id").equalsIgnoreCase("global");
                boolean _isuglobal = rs.getString("user_id").equalsIgnoreCase("global");
                boolean _isgroup = false;
                if (rs.getString("permission").startsWith("group.")) {
                    _isgroup = true;
                }
                //権限のレベルを設定
                if (!_isgglobal && _iscglobal && _isuglobal && _isgroup) level=1;
                if (!_isgglobal && _iscglobal && _isuglobal && !_isgroup) level=2;
                if (!_iscglobal && _isuglobal && _isgroup) level = 3;
                if (!_iscglobal && _isuglobal && !_isgroup) level = 4;
                if (!_isgglobal && _iscglobal && !_isuglobal && _isgroup) level = 5;
                if (!_isgglobal && _iscglobal && !_isuglobal && !_isgroup) level = 6;
                if (!_iscglobal && !_isuglobal && _isgroup) level = 7;
                if (!_iscglobal && !_isuglobal && !_isgroup) level = 8;
                if (_isgglobal && _iscglobal && !_isuglobal && _isgroup) level = 9;
                if (_isgglobal && _iscglobal && !_isuglobal && !_isgroup) level = 10;


                if(!result.containsKey(rs.getString("permission"))){
                    result.put(rs.getString("permission"), rs.getBoolean("value"));
                    permsLev.put(rs.getString("permission"), level);
                }else {//すでに権限がある場合、レベルが高ければ上書きする
                    if(permsLev.get(rs.getString("permission")) < level){
                        result.put(rs.getString("permission"), rs.getBoolean("value"));
                        permsLev.put(rs.getString("permission"), level);
                    }
                }
            }
            rs.close();
            pstmt.close();
            con.close();
            return result;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }

    }

    public boolean hasPermissionInGroup(@NotNull String name, @NotNull String permission){
        if(name.isEmpty() || permission.isEmpty()){
            return false;
        }
        try {
            Connection con = new MySQLUtil().getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT * FROM `dc_perm_group_perms` WHERE `name`=? AND (`permission`=? OR `permission` LIKE 'group.%' OR `permission` LIKE '%.*')");
            pstmt.setString(1, name);
            pstmt.setString(2, permission);
            ResultSet rs = pstmt.executeQuery();

            boolean _set = false;
            AtomicBoolean result = new AtomicBoolean(false);
            List<String> groups = new ArrayList<>();
            while (rs.next()){
                if(rs.getString("permission").startsWith("group.")){
                    groups.add(rs.getString("permission"));
                }else{
                    _set = true;
                    result.set(rs.getBoolean("value"));
                }
                if(rs.getString("permission").endsWith(".*")){
                    String[] permissions = permission.split("\\.");
                    String[] _perms = rs.getString("permission").split("\\.");
                    for (int i = 0; i < permissions.length; i++) {
                        if(permissions[i] != _perms[i]){
                            if(_perms.length-1 == i && _perms[i].equals("*")){
                                _set = true;
                                result.set(rs.getBoolean("value"));
                                break;
                            }
                        }
                    }
                }
            }
            rs.close();
            pstmt.close();
            con.close();
            if(!_set) {
                groups.forEach(k -> {
                    if (hasPermissionInGroup(k.replaceFirst("group\\.", ""), permission)) {
                        result.set(true);
                    }
                });
            }
            return result.get();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }
}
