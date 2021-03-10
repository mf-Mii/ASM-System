package work.mfmii.other.ASM_System.utils;

import work.mfmii.other.ASM_System.Config;

public class MySQLUtil {
    public MySQLUtil(){}

    public String getUrl(){
        String s1 = String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=utf8",
                new Config(Config.ConfigType.JSON).getString("mysql.host"),
                new Config(Config.ConfigType.JSON).getInt("mysql.port"),
                new Config(Config.ConfigType.JSON).getString("mysql.database"));
        return s1;
    }

    public String getUser(){
        return new Config(Config.ConfigType.JSON).getString("mysql.user");
    }

    public String getPassword(){
        return new Config(Config.ConfigType.JSON).getString("mysql.pass");
    }
}
