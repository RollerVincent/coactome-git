package parser;

import coactome.TSV;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Parser {


    public static ArgParser ArgParser(String[] args){
        ArgParser a = new ArgParser();
        a.args=args;
        return a;
    }
    public static class ArgParser {
        String[] args;
        HashMap<String,boolean[]> ArgDescriptions = new HashMap<String,boolean[]>();
        HashMap<String,String> Values = new HashMap<String,String>();
        HashMap<String,String> Info = new HashMap<String,String>();

        public void addOption(String flag,boolean hasValue, boolean optional, String info){
            addOption(flag,hasValue,optional);
            Info.put(flag,info);
        }

        public void addOption(String flag,boolean hasValue, boolean optional){
            ArgDescriptions.put(flag,new boolean[]{optional,hasValue});
        }

        public boolean Compile(){
            for (int i = 0; i < args.length-1; i++) {
                if (ArgDescriptions.containsKey(args[i])){
                    if(ArgDescriptions.get(args[i])[1]){
                        Values.put(args[i],args[i+1]);
                    }else{
                        Values.put(args[i],"");
                    }
                }
            }
            for(HashMap.Entry<String, boolean[]> entry : ArgDescriptions.entrySet()) {
                if(!entry.getValue()[0] && !Values.containsKey(entry.getKey())){
                    System.out.println(toString());
                    return false;
                }
            }
            return true;
        }

        public String getArgument(String flag){
            if(ArgDescriptions.containsKey(flag)) {
                boolean[] v = ArgDescriptions.get(flag);
                if (v[1]) {
                    return Values.get(flag);
                }
            }return toString();
        }

        public boolean hasArgument(String flag){
            if(Values.containsKey(flag)) {
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            String out="usage:\n";
            for(HashMap.Entry<String, boolean[]> entry : ArgDescriptions.entrySet()) {
                String k = entry.getKey();
                boolean[] v = entry.getValue();

                if (v[1]) {
                    out += "\t\trequired";
                } else {
                    out += "\t\toptional";
                }

                out+="\t"+k;

                String i = Info.get(k);


                if(i!=null) {
                    out += " ("+i+")";
                }
                out+="\n";

            }
            return out;
        }
    }


    public static void StringToFile(String string,String path){
        try {
            BufferedWriter writer = Writer(path);
            writer.write(string);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static BufferedReader Reader(String path){
        try {
            return new BufferedReader(new FileReader(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static BufferedWriter Writer(String path){
        try {
            String[] s = path.split("/");
            String f = s[s.length-1];
            if(s.length>1) {
                File file = new File(path.substring(0, path.length() - 1 - f.length()));
                file.mkdirs();
            }
            return new BufferedWriter(new FileWriter(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BufferedReader GzipReader(String path){
        try {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(path));
            return new BufferedReader(new InputStreamReader(gzip));
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }


    /***
     * returns reader for the first zip entry
     */

    public static BufferedReader ZipReader(String path){



            try{
                ZipFile zipFile = new ZipFile(path);

                Enumeration<? extends ZipEntry> entries = zipFile.entries();

                while(entries.hasMoreElements()){
                    ZipEntry entry = entries.nextElement();
                    InputStream stream = zipFile.getInputStream(entry);
                    return new BufferedReader(new InputStreamReader(stream));
                }
            }catch (Exception e){
                System.out.println("error in file :    " + path);
              /*  String[] split = path.split("/");
                if(split[split.length-1].startsWith("._")){
                    String retry = "";
                    for (int i = 0; i < split.length-1; i++) {
                        retry += split[i] + "/";
                    }
                    retry += "results.tsv.zip";
                    System.out.println("retrying with " + retry);
                    return ZipReader(retry);
                }*/


            }





        return null;

    }

    public static void Copy(String src, String dst){

        BufferedReader reader = Reader(src);
        BufferedWriter writer = Writer(dst);
        try{

            String line;
            while((line = reader.readLine()) != null){
                writer.write(line + "\n");
            }

            reader.close();
            writer.close();


        }catch(Exception e){
            e.printStackTrace();
        }



    }


    public static void zipFile(String src, String dst) {
        try {
            File file = new File(src);
            String zipFileName = dst;

            FileOutputStream fos = new FileOutputStream(zipFileName);
            ZipOutputStream zos = new ZipOutputStream(fos);

            zos.putNextEntry(new ZipEntry(file.getName()));

            byte[] bytes = Files.readAllBytes(Paths.get(src));
            zos.write(bytes, 0, bytes.length);
            zos.closeEntry();
            zos.close();

        } catch (FileNotFoundException ex) {
            System.err.format("The file %s does not exist", src);
        } catch (IOException ex) {
            System.err.println("I/O error: " + ex);
        }
    }

    public static void Delete(String file){
        File f = new File(file);
        f.delete();
    }

    public static Iterator<Path> FileIterator(String path){
        try {
            Path source = Paths.get(path);
            return Files.walk(source).filter(Files::isRegularFile).iterator();
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }

    }

    public static void SaveJS(JS js, String path){

    }

}
