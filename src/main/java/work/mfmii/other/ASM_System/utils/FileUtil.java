package work.mfmii.other.ASM_System.utils;

import java.io.*;

public class FileUtil {
    public FileUtil(){}


    /**
     * Get the file with root
     *
     * @param path path with program root or null
     * @return File object if one was found, path was null return File object "DataFolder"
     */
    public File getFile(String path) {
        if (path.isEmpty())
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

}
