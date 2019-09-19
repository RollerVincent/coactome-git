package force_direction;

public class Vector {

    public double x;
    public double y;

    public Vector(double x, double y){
        this.x = x;
        this.y = y;
    }

    public double sqrMagnitude(){
        return (x * x) + (y * y);
    }

    public Vector subtract(Vector other){
        return new Vector(x-other.x, y-other.y);
    }

    public Vector add(Vector other){
        return new Vector(x+other.x, y+other.y);
    }

    @Override
    public String toString() {
        return "Vector(" + x + ", " + y + ")";
    }

    public static Vector[] Array(int n, String mode){
        Vector[] v = new Vector[n];

        if(mode.equals("zero")){
            for (int i = 0; i < n; i++) {
                v[i] = new Vector(0,0);
            }
        }else if(mode.equals("random")){
            for (int i = 0; i < n; i++) {
                v[i] = new Vector((Math.random() - 0.5) * 2, (Math.random() - 0.5) * 2);
            }
        }else if(mode.equals("random_100")){
            for (int i = 0; i < n; i++) {
                v[i] = new Vector((Math.random() - 0.5) * 2 * 100, (Math.random() - 0.5) * 2 * 100);
            }
        }

        return  v;

    }




}
