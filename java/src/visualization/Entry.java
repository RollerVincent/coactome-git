package visualization;

public class Entry {

    String id;
    Double x = null;
    Double y = null;
    String group = null;

    public Entry(String id){
        this.id = id;
    }

    public void setX(double x){
        this.x = x;
    }

    public void setY(double y){
        this.y = y;
    }

    public void setGroup(String g){
        this.group = g;
    }

}
