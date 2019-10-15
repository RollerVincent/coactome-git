package coactome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Network {

    public HashMap<String, Node> nodes;

    public Network(){
        nodes = new HashMap<>();
    }


    public Matrix toMatrix(boolean doubled){

        System.out.print("Loading matrix... ");

        HashMap<String, Integer> indices = new HashMap<>();
        int index = 0;
        for (String key: this.nodes.keySet()) {
            indices.put(key, index);
            index += 1;
        }
        int length = nodes.size();

        Matrix matrix = new Matrix(length);

        HashSet<Node> others;

        for (String key: this.nodes.keySet()) {
            int i = indices.get(key);
            if(doubled){
                others = this.nodes.get(key).doubles;
            }else{
                others = this.nodes.get(key).links;
            }
            for (Node other: others) {
                int j = indices.get(other.id);
                matrix.data[i][j] = 1;
                matrix.data[j][i] = 1;
            }
        }

        for (String key : indices.keySet()) {
            matrix.members[indices.get(key)] = key;
        }

        System.out.print("Done\n");
        return matrix;
    }

    public void setDoubles(){
        System.out.print("Reducing to doubled links... ");
        int count = 0;
        for (Node node : this.nodes.values()) {
            for (Node other : node.links) {
                if(node.id.compareTo(other.id) <= 0) {
                    if (other.links.contains(node)) {
                        node.doubles.add(other);
                        other.doubles.add(node);
                        count += 1;
                    }
                }
            }
        }
        System.out.print("Done, "+ count + " links left\n");

    }

    public void removeUndoubled(){
        System.out.print("Removing unconnected nodes... ");
        HashMap<String, Node> tmp = new HashMap<>();
        for (Node node : this.nodes.values()) {
            if(node.doubles.size() != 0){
                tmp.put(node.id, node);
            }
        }
        System.out.print("Done, "+ tmp.size() + " of "+ this.nodes.size() +" nodes left\n");
        this.nodes = tmp;
    }

    public void removeUnconnected(){
        System.out.print("Removing unconnected nodes... ");
        HashMap<String, Node> tmp = new HashMap<>();
        for (Node node : this.nodes.values()) {
            if(node.links.size() != 0){
                tmp.put(node.id, node);
            }else{
                for (Node o : this.nodes.values()) {
                    if(o.links.contains(node)){
                        tmp.put(node.id, node);
                        break;
                    }
                }
            }
        }








        System.out.print("Done, "+ tmp.size() + " of "+ this.nodes.size() +" nodes left\n");
        this.nodes = tmp;
    }

    public void clear(){
        this.nodes = new HashMap<>();
    }




}
