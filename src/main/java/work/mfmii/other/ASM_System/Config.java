package work.mfmii.other.ASM_System;

import org.json.JSONArray;
import org.json.JSONObject;
import work.mfmii.other.ASM_System.utils.FileUtil;

import java.util.*;

public class Config {
    private String type;
    public Config(ConfigType configType){
        type = configType.getKey();
    }

    public Object get(String key){
        List<String> keys = Arrays.asList(key.split("\\.").clone());
        JSONObject temp = new JSONObject(getRaw());
        for (int i = 0; i <= keys.size() - 1; i++) {
            if (i == keys.size() - 1) {
                return temp.get(keys.get(i));
            }
            temp = temp.getJSONObject(keys.get(i));
        }
        return null;
    }

    public String getString(String key){
        List<String> keys = Arrays.asList(key.split("\\.").clone());
        JSONObject temp = new JSONObject(getRaw());
        for (int i = 0; i <= keys.size()-1; i++) {
            if (i == keys.size()-1) {
                return temp.getString(keys.get(i));
            }
            temp = temp.getJSONObject(keys.get(i));
        }
        return null;
    }

    public int getInt(String key){
        List<String> keys = Arrays.asList(key.split("\\.").clone());
        JSONObject temp = new JSONObject(getRaw());
        for (int i = 0; i <= keys.size() - 1; i++) {
            if (i == keys.size() - 1) {
                return temp.getInt(keys.get(i));
            }
            temp = temp.getJSONObject(keys.get(i));
        }
        return -1;
    }

    public boolean getBoolean(String key){
        List<String> keys = Arrays.asList(key.split("\\.").clone());
        JSONObject temp = new JSONObject(getRaw());
        for (int i = 0; i <= keys.size() - 1; i++) {
            if (i == keys.size() - 1) {
                return temp.getBoolean(keys.get(i));
            }
            temp = temp.getJSONObject(keys.get(i));
        }
        return false;
    }

    public long getLong(String key){
        List<String> keys = Arrays.asList(key.split("\\.").clone());
        JSONObject temp = new JSONObject(getRaw());
        for (int i = 0; i <= keys.size() - 1; i++) {
            if (i == keys.size() - 1) {
                return temp.getLong(keys.get(i));
            }
            temp = temp.getJSONObject(keys.get(i));
        }
        return -1;
    }

    public JSONObject getJSONObject(String key){
        List<String> keys = Arrays.asList(key.split("\\.").clone());
        JSONObject temp = new JSONObject(getRaw());
        for (int i = 0; i <= keys.size() - 1; i++) {
            if (i == keys.size() - 1) {
                return temp.getJSONObject(keys.get(i));
            }
            temp = temp.getJSONObject(keys.get(i));
        }
        return null;
    }

    public JSONArray getJSONArray(String key){
        List<String> keys = Arrays.asList(key.split("\\.").clone());
        JSONObject temp = new JSONObject(getRaw());
        for (int i = 0; i <= keys.size() - 1; i++) {
            if (i == keys.size() - 1) {
                return temp.getJSONArray(keys.get(i));
            }
            temp = temp.getJSONObject(keys.get(i));
        }
        return null;
    }

    public String getRaw(){
        if(type.equals(ConfigType.JSON.getKey())) {
            String conf_raw = new FileUtil().readFile(new FileUtil().getFile("config.json"), "utf8");
            return conf_raw;
        }else if(type.equals(ConfigType.DEFAULT.getKey())){
            String conf_raw = new FileUtil().readFile(new FileUtil().getFile("default.json"), "utf8");
            return conf_raw;
        }else{
            return null;
        }
    }

    public boolean isDebugMode(){
        return new Config(ConfigType.JSON).getBoolean("debug_mode");
    }

    public enum ConfigType{
        JSON("json"),
        DEFAULT("default"),
        UNKNOWN("");

        private final String key;

        ConfigType(String key) {
            this.key = key;
        }

        public String getKey(){
            return key;
        }

        public ConfigType fromKey(String key){
            for (ConfigType type: values()) {
                if(type.equals(key.toLowerCase())){
                    return type;
                }
            }
            return UNKNOWN;
        }
    }
}