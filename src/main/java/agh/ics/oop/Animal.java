package agh.ics.oop;

import java.util.ArrayList;

public class Animal {
    private Vector2d position;
    private MapDirection orientation = MapDirection.NORTH;
    private int energy;
    private Genome genome = new Genome();
    private Map map;
    private ArrayList<IPositionObserver> positionObservers = new ArrayList<>();
//POZYCJA MA BYC LOSOWA DOCELOWO
    public Animal(int baseEnergy, Vector2d position,Map map) {
        energy = baseEnergy;
        this.position = position;
        this.map = map;
        orientation = Random.getOrientation();
        map.place(this);
        positionObservers.add(map);
    }
    public Animal(int baseEnergy, Vector2d position,Map map,Genome genome){
        this(baseEnergy,position,map);
        this.genome = genome;
    }

    public Vector2d getPosition() {
        return position;
    }
    public Genome getGenome() {
        return genome;
    }
    public MapDirection getOrientation() {
        return orientation;
    }
    public void setOrientation(MapDirection orientation){
        this.orientation = orientation;
    }
    public boolean isAt(Vector2d position){
        return position.equals(this.position);
    }

    //tu mozesz wywoływac zamieniajac dane na MoveDirection
    public void move(MoveDirection direction){
        switch (direction) {
            case FORWARD -> {
                moveForward();
            }
            case BACKWARD -> {
                //obrót i ruch
                orientation = MapDirection.values()[(orientation.ordinal()+direction.ordinal())%8];
                moveForward();
            }
            default -> {
                //rotate by enum value
                orientation = MapDirection.values()[(orientation.ordinal()+direction.ordinal())%8];
            }
        }
    }

    //method that contacts map and returns actual pos to move
    private Vector2d validateDestination(Vector2d destination) {
        if (map.isWrapped() && map.posOutOfBounds(destination)){
            //nie spradzamy chyba czy slot jest zajety czy cos
            return map.wrapPosition(destination);
        }
        if (!map.isWrapped() && map.posOutOfBounds(destination)){
            return position;//nie ruszamy sie (traci kolejke)
        }
        return destination;
    }

    //moves forward one tile and notifies observers
    private void moveForward(){
        Vector2d destination = position.add(orientation.toVector());
        destination = validateDestination(destination);
        if (!position.equals(destination))
        {
            Vector2d oldPos = position;
//            System.out.println("oldPos"+oldPos);
            position = destination;
//            System.out.println("oldPos"+oldPos);

            for (IPositionObserver observer : positionObservers) {
                observer.positionChanged(oldPos,destination,this);
            }
        }

    }

    public int getEnergy() {
        return energy;
    }

    public void addEnergy(int energy) {
        System.out.println("dodaje energii: "+energy);
        this.energy += energy;
    }

    public Animal reproduce(Animal partner){
        int percentage = (int) ((double)this.getEnergy()/(partner.getEnergy()+this.getEnergy())*100);
        System.out.println(percentage);
        Genome childGenome = genome.mixGenes(partner.getGenome(),percentage);
        Animal child = new Animal((int)(getEnergy()*0.25)+(int)(partner.getEnergy()*0.25),position.copy(),map,childGenome);
        //TODO add random orientation, add child to cluster (do that in cluster), czy sam sie doda w sumie?
//        child.setOrientation(Random.getOrientation());
        this.addEnergy(-(int)(getEnergy()*0.25));
        partner.addEnergy(-(int) (partner.getEnergy()*0.25));
        return child;
    }
}
