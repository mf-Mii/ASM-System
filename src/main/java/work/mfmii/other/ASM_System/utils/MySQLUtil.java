package work.mfmii.other.ASM_System.utils;

import work.mfmii.other.ASM_System.Config;

public class MySQLUtil {
    public MySQLUtil(){}

    public String getUrl(){
        StringBuilder sb = new StringBuilder();
        sb.append("jdbc:mysql://").append(new Config(Config.ConfigType.JSON).getString("mysql.host")).append(":")
                .append(new Config(Config.ConfigType.JSON).getInt("mysql.port")).append("/")
                .append(new Config(Config.ConfigType.JSON).getString("mysql.database"))
                .append("?serverTimezone=JST&useUnicode=true&characterEncoding=utf8&autoReconnect=true&useSSL=false");
        return sb.toString();
    }

    public String getUser(){
        return new Config(Config.ConfigType.JSON).getString("mysql.user");
    }

    public String getPassword(){
        return new Config(Config.ConfigType.JSON).getString("mysql.pass");
    }
}
