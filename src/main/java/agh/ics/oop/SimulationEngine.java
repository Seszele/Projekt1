package agh.ics.oop;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static java.lang.Math.floor;

public class SimulationEngine implements Runnable {
    private volatile boolean running = true;
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();
    private boolean isWrapped;
    Map map;

    private ArrayList<IEpochObserver> epochObservers = new ArrayList<>();

    public SimulationEngine(Map map,IEpochObserver observer) {
        epochObservers.add(observer);
        this.map = map;
        //tu musi byc ladnie w petli robienie zwierzakow
        //i inne dane do konstuktora istone chyba
        for (int i = 0; i < SimulationData.startingAnimals; i++) {
            new Animal(SimulationData.startEnergy,Random.getVector(map.getDimension().x,map.getDimension().y),map);
        }
    }

    @Override
    public void run(){
        while(true){
            synchronized (pauseLock) {
                if (!running) { // may have changed while waiting to
                    // synchronize on pauseLock
                    break;
                }
                if (paused) {
                    try {
                        synchronized (pauseLock) {
                            pauseLock.wait(); // will cause this Thread to block until
                        }
                    } catch (InterruptedException ex) {
                        break;
                    }
                    if (!running) { // running might have changed since we paused
                        break;
                    }
                }
            }
            try {
                Thread.sleep(SimulationData.epochInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            removeDeadAnimals();
            moveAnimals();
            feedClusters();
            reproduce();
            growPlants();
            notifyObservers();
        }

    }

    public void feedClusters(){
        for (AnimalCluster cluster : map.getHungryClusters()) {
            cluster.feed();
        }
    }

    public void reproduce(){
        for (AnimalCluster cluster : map.getClusters()) {
            if (cluster.size() >= 2){
                cluster.reproduce();
            }
        }
    }

    //UWAGA nie mozesz ruszac zwierzetami z clusterow, potrzebujesz to robic z listy zwierzakow np
    public void moveAnimals(){
        for (Animal animal : map.getAnimals()) {
            animal.geneticMove();
        }
    }

    public void growPlants(){
        //normal plant
        growPlant(false);
        growPlant(true);

    }

    private void notifyObservers(){
        for (IEpochObserver epochObserver : epochObservers) {
            epochObserver.epochConcluded(this);
        }
    }

    private boolean growPlant(boolean isJunglePlant){
        Set<Vector2d> alreadyDrawn = new HashSet<>();
        for (int y = 0; y <= map.getDimension().y; y++) {
            for (int x = 0; x <= map.getDimension().x; x++) {
                //musze zdrwawowac ktory nie byl zdrawowany
                Vector2d draw = Random.getVector(map.getDimension().x,map.getDimension().y);
                while(alreadyDrawn.contains(draw)){
                    draw = Random.getVector(map.getDimension().x,map.getDimension().y);
                }
                alreadyDrawn.add(draw);
                if (isJunglePlant){
                    if (map.isJungle(draw) && !map.isOccupied(draw)){
                        //place
                        map.placePlant(draw);
//                        System.out.println("klade trawsko w dzungli");
                        return true;
                    }
                }
                else{
                    if (!map.isJungle(draw) && !map.isOccupied(draw)){
                        //place
                        map.placePlant(draw);
//                        System.out.println("klade trawsko normalne");
                        return true;
                    }
                }
            }
        }
//        System.out.println("nie udalo sie postwic trawy!(pewnie jest pelna mapa) | SimulationEngine");
        return false;
    }
    public void stop() {
        running = false;
        // you might also want to interrupt() the Thread that is
        // running this Runnable, too, or perhaps call:
        resume();
        // to unblock
    }

    public void pause() {
        // you may want to throw an IllegalStateException if !running
        paused = true;
    }

    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll(); // Unblocks thread
        }
    }

    public void removeDeadAnimals(){
        for (AnimalCluster cluster : map.getClusters()) {
            cluster.cullTheWeaklings();
            if (cluster.size() == 0){
                map.removeCluster(cluster);
            }
        }
    }
    public boolean isWrapped() {
        return isWrapped;
    }

    public Map getMap() {
        return map;
    }
    public boolean isPaused() {
        return paused;
    }
}
