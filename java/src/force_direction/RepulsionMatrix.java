package force_direction;

public class RepulsionMatrix {


    double[][] matrix;

    public RepulsionMatrix(int n){
        this.matrix = new double[n][n];
    }

    public void set(int x, int y, double value){
        this.matrix[x][y] = value;
    }

    public double get(int x, int y){
        return this.matrix[x][y];
    }



}
