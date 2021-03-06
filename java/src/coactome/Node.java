package coactome;

import java.util.HashSet;

public class Node {

    public String id;

    public HashSet<Node> links;
    public HashSet<Node> doubles; // bidirectional links

    public Node(String id){
        this.id = id;
        links = new HashSet<>();
        doubles = new HashSet<>();
    }

}
