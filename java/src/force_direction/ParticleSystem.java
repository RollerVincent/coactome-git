package force_direction;

import coactome.Mapping;
import parser.Parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.HashMap;

public class ParticleSystem {

    public Particle[] particles;
    public RepulsionMatrix repulsions;
    public int size;


    public void setParticles(Particle[] particles) {
        this.particles = particles;
        this.size = this.particles.length;
    }

    public void setRepulsions(RepulsionMatrix repulsions) {
        this.repulsions = repulsions;
        for (int i = 0; i < this.size; i++) {
            for (int j = i+1; j < this.size; j++) {
                if(this.repulsions.get(i, j) < 0){
                    this.particles[i].degree += 1;
                    this.particles[j].degree += 1;
                }
            }
        }

    }

    public void loadClustering(String clustering){
        try{

            BufferedReader reader = Parser.Reader(clustering);

            HashMap<String, String> clusters = new HashMap();

            String line;
            String[] split;
            while((line = reader.readLine()) != null){
                split = line.split("\t");
                clusters.put(split[0], split[1]);
            }

            for (Particle p: particles) {
                p.cluster = clusters.get(p.id);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void setSizes(String mode){

        if(mode.equals("degree")){
            for (int i = 0; i < this.size; i++) {
                particles[i].radius = Math.sqrt(particles[i].degree + 1);
            }
        }

    }

    public void setSizes(double radius){

        for (int i = 0; i < this.size; i++) {
            particles[i].radius = radius;
        }


    }

    public void setAttributes(Mapping mapping){
        for (int i = 0; i < size; i++) {
            if(mapping.map.containsKey(particles[i].id)){
                particles[i].attributes = mapping.get(particles[i].id);
            }

        }
    }


    @Override
    public String toString() {
        int min = Math.min(this.particles.length, 15);
        String out = "\nSYSTEM WITH " + this.particles.length + " PARTICLES\n";
        out += "--------------------------\n";
        for (int i = 0; i < min; i++) {
            out +=  " " + this.particles[i].degree + "\t  |";
            for (int j = 0; j < i+1; j++) {
                out += " - ";
                if(j<min-1){
                    out += " \t";
                }
            }
            for (int j = i+1; j < min; j++) {

                out += this.repulsions.get(i, j);
                if(j<min-1){
                    out += " \t";
                }
            }
            out += " |   " + this.particles[i] +"\n";
        }
        if(this.particles.length >= 5){
            out += " ...\n";
        }
        out += "--------------------------\n";
        return out;
    }

    public static ParticleSystem FromNetworkFile(String src){

        ParticleSystem system = new ParticleSystem();

        try{

            BufferedReader reader = Parser.Reader(src);

            int n = Integer.parseInt(reader.readLine());

            Particle[] particles = new Particle[n];
            for (int i = 0; i < n; i++) {
                particles[i] = new Particle(reader.readLine());
            }
            system.setParticles(particles);

            RepulsionMatrix repulsions = new RepulsionMatrix(n);
            String[] split;
            for (int i = 0; i < n - 1; i++) {
                split = reader.readLine().split("\t");
                for (int j = 0; j < split.length; j++) {
                    if(Double.parseDouble(split[j]) > 0){  // link in network
                        repulsions.set(i, j + i +1, -1);          // attract
                    }else{
                        repulsions.set(i, j + i +1, 1);          // repulse

                    }
                }
            }
            system.setRepulsions(repulsions);

        }catch (Exception e){
            e.printStackTrace();
        }

        return system;
    }


}
