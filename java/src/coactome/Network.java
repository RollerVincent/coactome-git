package coactome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Network {

    public HashMap<String, Node> nodes;

    public Network(){
        nodes = new HashMap<>();
    }


    public Matrix toMatrix(){

        System.out.print("Loading directed matrix... ");

        HashMap<String, Integer> indices = new HashMap<>();
        int index = 0;
        for (String key: this.nodes.keySet()) {
            indices.put(key, index);
            index += 1;
        }
        int length = nodes.size();

        Matrix matrix = new Matrix(length);


        for (String key: this.nodes.keySet()) {
            int i = indices.get(key);
            for (Node other: this.nodes.get(key).links) {
                int j = indices.get(other.id);
                matrix.data[i][j] = 1;
            }
        }

        for (String key : indices.keySet()) {
            matrix.members[indices.get(key)] = key;
        }

        System.out.print("Done\n");
        return matrix;
    }

    public void clear(){
        this.nodes = new HashMap<>();
    }




}
