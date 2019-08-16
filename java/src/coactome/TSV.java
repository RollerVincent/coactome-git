package coactome;

import parser.Parser;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class TSV {

    HashMap<Gene, List<String>> data;
    List<String> descriptions;
    List<String> comments;



    public TSV(Collection<Gene> rows, String description){
        data = new HashMap<>();
        descriptions = new ArrayList<>();
        comments = new ArrayList<>();
        for (Gene gene : rows) {
            data.put(gene, new ArrayList<>());
        }
        descriptions.add(description);
    }

    public void addComment(String c){
        comments.add(c);
    }

    public void addColumn(HashMap<Gene, Double> values, String description){
        for (Gene r: data.keySet()) {
            if(values.get(r) == null){
                data.get(r).add("NN");
            }else{
                data.get(r).add(String.valueOf(values.get(r)));
            }
        }
        descriptions.add(description);
    }

    public void save(String file){
        BufferedWriter bw = Parser.Writer(file);
        try{

            for (String c : comments) {
                bw.write("#  " + c + "\n");
            }

            for (String d : descriptions) {
                bw.write(d + "\t");
            }
            bw.write("\n");
            for (Gene g : data.keySet()) {
                bw.write(g.id + "\t");
                for (String v : data.get(g)) {
                    bw.write(v + "\t");
                }
                bw.write("\n");
            }
            bw.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void saveAsZip(String file){
        save(file);
        Parser.zipFile(file, file + ".zip");
        Parser.Delete(file);
    }

}
