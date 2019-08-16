package coactome;

import parser.Parser;

import java.io.BufferedWriter;

public class Matrix {

    public int[][] data;

    public int size;

    public String[] members;

    public Matrix(int d){
        this.data = new int[d][d];
        this.members = new String[d];
        this.size = d;
    }

    public void Save(String path, boolean directed){
        if(!directed){
            try{

                BufferedWriter writer = Parser.Writer(path);
                writer.write("" + this.size +"\n");
                for (String member:this.members) {
                    writer.write(member + "\n");
                }
                for (int i = 0; i < this.size; i++) {
                    for (int j = i+1; j < this.size; j++) {
                        writer.write("" + this.data[i][j] + "\t");
                    }
                    writer.write("\n");
                }
                writer.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }



}
