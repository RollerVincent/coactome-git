package coactome;

import parser.Parser;

import java.io.BufferedReader;
import java.util.*;

public class Mapping {


    public HashMap<String, String> map = new HashMap<>();

    public void put(String id, String value){
        map.put(id, value);
    }

    public String get(String id){
        return map.get(id);
    }

    public static Mapping GO(String source){

        Mapping mapping = new Mapping();


        try{

            BufferedReader reader = Parser.GzipReader(source);
            String line = reader.readLine();
            String[] split;
            HashMap<String, HashSet<String>> gos = new HashMap<>();
            while((line = reader.readLine()) != null){




                split = line.split("\t");

                if(!gos.containsKey(split[0])){
                    gos.put(split[0], new HashSet<>());
                }
                if(split.length > 1){
                    gos.get(split[0]).add(split[1].substring(3));
                }
            }


            for (String gene : gos.keySet()) {
                String goString = "[";
                for (String go :gos.get(gene)){
                    goString += go + ", ";
                }
                if(goString.length() > 1) {
                    goString = goString.substring(0, goString.length() - 2);
                }
                goString += "]";

                mapping.put(gene, goString);
            }



        }catch (Exception e){
            e.printStackTrace();
        }

        return mapping;
    }

}
