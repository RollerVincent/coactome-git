package force_direction;

import parser.JS;
import parser.Parser;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.HashSet;

public class Simulation {

    public ParticleSystem system;

    public double conservation = 0.8;
    public double precision = 1;
    public double correction = 10;

    private double sqrCorrection = 100;

    private Vector[] P;
    private Vector[] F;
    private Vector[] V;

    private int N;

    private double FPP;     // squared force per particle

    public String GoTerms = null;

    public void setSystem(ParticleSystem system) {
        this.system = system;
        this.N = system.particles.length;


    }

    public void setCorrection(double correction){
        this.correction = correction;
        this.sqrCorrection = correction * correction;
    }





    public void run(double FPPcutoff){


        P = Vector.Array(N, "random_100");
        V = Vector.Array(N, "zero");


        frame();

        long l = System.nanoTime();
        long c;
        double fps;
        int count = 0;

        FPP = totalEnergy() / N;

        while (FPP > FPPcutoff){

            frame();

            count += 1;
        //    c = System.nanoTime();
        //    fps = 1.0 / ((c - l) / 1000000000.0);
        //    l = c;

            FPP = totalEnergy() / N;


           // System.out.print("\r FPP    :     " + FPP);
            System.out.println(" FPP    :     " + FPP);

        }

        System.out.println("\n Done   :     " + ((System.nanoTime() - l)/1000000000.0) + " s");
        System.out.println(" Frames :     " + count);


    }

    private void frame(){

        Update_F();
        Update_V();
        Update_P();


    }

    private void Update_F(){

        Vector i_position;
        Vector j_position;
        Vector diff;
        Vector force;
        double repulsion;
        double sqrMag;
        double mag;
        double rmf;


        F = Vector.Array(N, "zero");

        for (int i = 0; i < N; i++) {
            i_position = P[i];


            for (int j = i+1; j < N; j++) {
                j_position = P[j];

                repulsion = system.repulsions.get(i, j);




                force = new Vector(0,0);

                diff = i_position.subtract(j_position);
                sqrMag = diff.sqrMagnitude();

                mag = Math.sqrt(sqrMag);

                rmf = repulsion / mag / 10;

                force.x = (diff.x / mag) * rmf;
                force.y = (diff.y / mag) * rmf;



                if(repulsion < 0){
                    force.x = -force.x;
                    force.y = -force.y;

                    force.x += (diff.x / mag) * sqrMag * repulsion / 100000.0 * 8;
                    force.y += (diff.y / mag) * sqrMag * repulsion / 100000.0 * 8;

                }





                F[i].x += force.x;
                F[i].y += force.y;

                F[j].x -= force.x;
                F[j].y -= force.y;

            }
        }

        for (int i = 0; i < N; i++) {
            F[i].x -= P[i].x / 1000.0;
            F[i].y -= P[i].y / 1000.0;
        }



    }

    private void Update_V(){
        double sm;
        for (int i = 0; i < N; i++) {
            V[i].x = V[i].x * conservation + F[i].x / precision;
            V[i].y = V[i].y * conservation + F[i].y / precision;
            sm = V[i].sqrMagnitude();

            if(sm > this.sqrCorrection){
                V[i].x /= this.correction;
                V[i].y /= this.correction;

            }
        }
    }

    private void Update_P(){
        for (int i = 0; i < N; i++) {
            P[i].x = P[i].x + V[i].x;
            P[i].y = P[i].y + V[i].y;
        }
    }

    private double totalEnergy(){
        double s = 0;
        for (int i = 0; i < N; i++) {
            s += F[i].sqrMagnitude();
        }
        return s;
    }

    public void correctCollisions(){

        int corrected = 1;

        Vector[] C;
        double dist;
        double contact;
        Vector diff;

        while(corrected > 0){

            corrected = 0;

            for (int i = 0; i < N; i++) {
                for (int j = i+1; j < N; j++) {

                    if (i != j) {
                        diff = P[i].subtract(P[j]);
                        dist = Math.sqrt(diff.sqrMagnitude());

                        contact = system.particles[i].radius + system.particles[j].radius;

                        if (dist < contact) {
                            corrected += 1;

                            P[i].x += (diff.x / dist) * (contact - dist)*1;
                            P[i].y += (diff.y / dist) * (contact - dist)*1;

                            P[j].x -= (diff.x / dist) * (contact - dist)*1;
                            P[j].y -= (diff.y / dist) * (contact - dist)*1;
                        }
                    }

                }


            }



        }

    }


    public HashMap<String, Vector> result(){
        HashMap<String, Vector> r = new HashMap<>();
        for (int i = 0; i < N; i++) {
            r.put(system.particles[i].id, P[i]);


        }
        return r;
    }


    public void save(String path){
        JS js = new JS();
        js.addStructure("nodes");
        js.addStructure("links");

        HashMap<String, String> map;

        if(GoTerms != null){
            js.addStructure("go");

            try{

                BufferedReader reader = Parser.GzipReader(GoTerms);
                String line = reader.readLine();
                String[] split;
                HashMap<String, String> gos = new HashMap<>();
                while((line = reader.readLine()) != null){




                    split = line.split("\t");

                    if(split.length > 2){
                        if(split[3].equals("biological_process") || split[3].equals("cellullar_component")){
                            gos.put(split[1].substring(3), split[2]);
                        }
                    }
                }


                HashMap<String, String> usedGo = new HashMap<>();

                for (int i = 0; i < N; i++) {
                    if(system.particles[i].attributes.length() > 2) {
                        String attr = "[";
                        for (String go : system.particles[i].attributes.substring(1, system.particles[i].attributes.length() - 1).split(", ")) {

                            if(gos.containsKey(go)) {
                                usedGo.put(go, gos.get(go));
                                attr += "\"" + go + "\",";
                            }

                        }
                        if(attr.length()>1){
                            attr = attr.substring(0, attr.length());
                        }
                        attr += "]";
                        system.particles[i].attributes = attr;
                    }
                }

                for (String go: usedGo.keySet()) {
                    map = new HashMap<>();
                    map.put("id", "\"" + go + "\"");
                    map.put("name", "\"" + usedGo.get(go) + "\"");
                    js.updateStructure("go", map);
                }

                System.out.println("\n " + usedGo.size() + " related GO terms. Total: " + gos.size());


            }catch (Exception e){
                e.printStackTrace();
            }

        }


        for (int i = 0; i < N; i++) {
            map = new HashMap<>();
            map.put("id", "\"" + system.particles[i].id + "\"");
            map.put("x", String.valueOf(P[i].x));
            map.put("y", String.valueOf(P[i].y));
            map.put("cluster", system.particles[i].cluster);
            map.put("radius", String.valueOf(system.particles[i].radius));
            map.put("attributes", system.particles[i].attributes);
            js.updateStructure("nodes", map);
        }

        for (int i = 0; i < N; i++) {
            for (int j = i+1; j < N; j++) {
                if(system.repulsions.get(i, j) < 0){
                    map = new HashMap<>();
                    map.put("src", String.valueOf(i));
                    map.put("dst", String.valueOf(j));
                    js.updateStructure("links", map);
                }
            }
        }



        js.save("network", path);
    }

}
