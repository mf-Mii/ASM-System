package work.mfmii.other.ASM_System.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.Arrays;
import java.util.List;

public class FileUtil {
    public FileUtil(){}


    /**
     * Get the file with root
     *
     * @param path path with program root or null
     * @return File object if one was found, path was null return File object "DataFolder"
     */
    public File getFile(String path) {
        if (path == null || path.isEmpty())
            return new File(new File(".").getAbsoluteFile().getParentFile(), "BotSystemFile/"+path!=null?path:"");
        else {
            if(path.contains("/")){
                String[] paths = path.split("/");
                File file = new File(".").getAbsoluteFile().getParentFile();
                for (int i = 0; i < paths.length; i++) {
                    if(i == paths.length-1){
                        return new File(file, paths[i]);
                    }
                    file=new File(file, paths[i]);
                }
            }
            return new File(new File(".").getAbsoluteFile().getParentFile(), path);

        }
    }



    /**
     *
     * @param file input File object
     * @return String from file
     */
    public String readFile(File file, String charset){
        try {
            return InputStreamToString(new FileInputStream(file), charset);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String InputStreamToString(InputStream is, String charset){
        if(charset == null) charset = "utf8";
        InputStreamReader inputStream = null;
        try {
            inputStream = new InputStreamReader(is, charset);
            BufferedReader br = new BufferedReader(inputStream);

            String res = "";
            String  str = br.readLine();
            while (str != null){
                res += str;
                str = br.readLine();
            }
            br.close();
            return res;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public Object getObjectFromJSON(JSONObject json, String key){
        List<String> keys = Arrays.asList(key.split("\\.").clone());
        JSONObject temp = json;
        for (int i = 0; i <= keys.size() - 1; i++) {
            if (i == keys.size() - 1) {
                return temp.get(keys.get(i));
            }
            temp = temp.getJSONObject(keys.get(i));
        }
        return null;
    }

    public boolean getBooleanFromJSON(JSONObject json, String key){
        List<String> keys = Arrays.asList(key.split("\\.").clone());
        JSONObject temp = json;
        for (int i = 0; i <= keys.size() - 1; i++) {
            if (i == keys.size() - 1) {
                return temp.getBoolean(keys.get(i));
            }
            temp = temp.getJSONObject(keys.get(i));
        }
        return false;
    }

    public String getStringFromJSON(JSONObject json, String key){
        List<String> keys = Arrays.asList(key.split("\\.").clone());
        JSONObject temp = json;
        for (int i = 0; i <= keys.size() - 1; i++) {
            if (i == keys.size() - 1) {
                return temp.getString(keys.get(i));
            }
            temp = temp.getJSONObject(keys.get(i));
        }
        return null;
    }

    public int getIntFromJSON(JSONObject json, String key){
        List<String> keys = Arrays.asList(key.split("\\.").clone());
        JSONObject temp = json;
        for (int i = 0; i <= keys.size() - 1; i++) {
            if (i == keys.size() - 1) {
                return temp.getInt(keys.get(i));
            }
            temp = temp.getJSONObject(keys.get(i));
        }
        return -1;
    }

    public long getLongFromJSON(JSONObject json, String key){
        List<String> keys = Arrays.asList(key.split("\\.").clone());
        JSONObject temp = json;
        for (int i = 0; i <= keys.size() - 1; i++) {
            if (i == keys.size() - 1) {
                return temp.getLong(keys.get(i));
            }
            temp = temp.getJSONObject(keys.get(i));
        }
        return -1;
    }

    public JSONObject getJSONObjectFromJSON(JSONObject json, String key){
        List<String> keys = Arrays.asList(key.split("\\.").clone());
        JSONObject temp = json;
        for (int i = 0; i <= keys.size() - 1; i++) {
            if (i == keys.size() - 1) {
                return temp.getJSONObject(keys.get(i));
            }
            temp = temp.getJSONObject(keys.get(i));
        }
        return null;
    }

    public JSONArray getJSONArrayFromJSON(JSONObject json, String key){
        List<String> keys = Arrays.asList(key.split("\\.").clone());
        JSONObject temp = json;
        for (int i = 0; i <= keys.size() - 1; i++) {
            if (i == keys.size() - 1) {
                return temp.getJSONArray(keys.get(i));
            }
            temp = temp.getJSONObject(keys.get(i));
        }
        return null;
    }


    public boolean writeFile(@Nonnull String file, @Nonnull String content, boolean append){
        System.setProperty("file.encoding","UTF-8");
        try {
            FileWriter fw = new FileWriter(getFile(file),append);
            PrintWriter pw = new PrintWriter(new BufferedWriter(fw));

            pw.println(content);

            pw.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
