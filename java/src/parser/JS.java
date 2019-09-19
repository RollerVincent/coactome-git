package parser;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JS {
    HashMap<String, List<HashMap<String, String>>> data = new HashMap<>();

    public void addStructure(String title){
        data.put(title, new ArrayList<>());
    }

    public void updateStructure(String title, HashMap<String , String> entry){
        data.get(title).add(entry);
    }

    public void save(String title, String path){
        try{

            BufferedWriter writer = Parser.Writer(path);

            writer.write(title + " = {\n");


            boolean firstStructure = true;
            boolean firstEntry = true;
            boolean firstAttribute = true;

            for (String s : data.keySet()){
                if(firstStructure) {
                    writer.write("\"" + s + "\" : [\n");
                    firstStructure = false;
                }else {
                    writer.write(",\n\"" + s + "\" : [\n");
                }
                firstEntry = true;
                for(HashMap<String, String> entry : data.get(s)){
                    firstAttribute = true;
                    if(firstEntry) {
                        writer.write("{");
                        firstEntry = false;
                    }else{
                        writer.write(",\n{");
                    }
                    for(String a : entry.keySet()){
                        if(firstAttribute) {
                            writer.write("\"" + a + "\" : " + entry.get(a));
                            firstAttribute = false;
                        }else{
                            writer.write(", \"" + a + "\" : " + entry.get(a));
                        }
                    }
                    writer.write("}");
                }
                writer.write("\n]");

            }
            writer.write("};");

            writer.close();


        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
