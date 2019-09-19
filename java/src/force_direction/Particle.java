package force_direction;

public class Particle {

    public String id;
    public Vector position;
    public String cluster = "0";
    public int degree = 0;
    public double radius = 1;
    public String attributes = "[]";

    public Particle(String id){
        this.id = id;
    }

    public void randomPosition(double max){
        this.position = new Vector(Math.random() * max, Math.random() * max);
    }

    @Override
    public String toString() {
        return this.id;
    }
}
