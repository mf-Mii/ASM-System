package work.mfmii.other.ASM_System;

public class Config {
    private String type = null;
    public Config(ConfigType configType){
        type = configType.getKey();
    }

    public Object get(String key){
        Object res;
        return res;
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