package work.mfmii.other.ASM_System.utils;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import work.mfmii.other.ASM_System.ASMSystem;
import work.mfmii.other.ASM_System.Config;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class PermissionUtil {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    private boolean isDebug = false;
    public PermissionUtil(){
        isDebug = new Config(Config.ConfigType.JSON).isDebugMode();
    }

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
            if (isDebug){
                System.out.println("Some of id's length weren't 18.");
                System.out.println(guildId.length()+"("+guildId+")\n"+channelId.length()+"("+channelId+")\n"+userId.length()+"("+userId+")");
            }
            return false;
        }

        try {
            boolean addGid = false;
            boolean addUid = false;
            String _gid = (guildId == null || guildId.length() != 18) ? "global" : guildId;
            String _cid = (channelId == null || channelId.length() != 18) ? "global" : channelId;
            String _uid = (userId == null) ? "global" : userId;
            if(userId != null && ASMSystem.jda.getUserById(userId) == null){
                if(isDebug) System.out.println("JDA couldn't find user that id is "+userId);
                return false;
            }
            if (!_cid.equals("global")) addGid = true;
            if (!_uid.equals("global")) addUid = true;
            Connection con = new MySQLUtil().getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT * FROM `dc_user` WHERE `id`=?");
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (isDebug){
                System.out.println("GuildID: "+guildId.length()+"("+guildId+")\nChannelID: "+channelId.length()+"("+channelId+")\nUserID: "+userId.length()+"("+userId+")");
                System.out.println("_gid: "+_gid+", _cid: "+_cid+", _uid: "+_uid);
            }
            if(!rs.next()){
                if (isDebug){
                    System.out.println("There is not data of the user in users database");
                }
                pstmt = con.prepareStatement("INSERT INTO `dc_user` (`id`, `isBot`) VALUES (?, ?)");
                pstmt.setString(1, userId);
                pstmt.setBoolean(2, ASMSystem.jda.getUserById(userId).isBot());
                pstmt.execute();
                pstmt = con.prepareStatement("INSERT INTO `dc_perm_each` (`permission`, `user_id`, `value`) VALUES ('group.default', ?, 1)");
                pstmt.setString(1, userId);
                pstmt.execute();
            }
            rs.close();
            pstmt.close();

            StringBuilder sql = new StringBuilder()
                    .append("SELECT * FROM `dc_perm_each` WHERE ");
            if(addGid) sql.append("(`guild_id`=? OR `guild_id`='global')AND ");
            if(addUid) sql.append("(`user_id`='global' OR `user_id`=?) AND ");
            else sql.append("`user_id`='global' AND ");
            sql.append("(`channel_id`=? OR `channel_id`='global') AND (`permission`=? OR `permission` LIKE 'group.%' OR `permission` LIKE '%.*')");
            pstmt = con.prepareStatement(sql.toString());
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
            rs = pstmt.executeQuery();
            if(isDebug) System.out.println(sql.toString());

            AtomicBoolean result = new AtomicBoolean(false);
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

                if (isDebug){
                    System.out.println("last_lev: "+last_lev+", level: "+level);
                }

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
