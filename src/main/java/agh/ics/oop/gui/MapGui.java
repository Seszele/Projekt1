package agh.ics.oop.gui;

import agh.ics.oop.*;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class MapGui {
    private final SimulationEngine simulationEngine;
    private final Map map;
    private final GridPane grid;
    private final AnimalFollower animalFollower;
    private final Label title = new Label("Normal map");
    private int magicCooldown = 0;
    private int lastMana = 0;
    private String borderWidth = "1";

    public MapGui(SimulationEngine simulationEngine) {
        this.simulationEngine = simulationEngine;
        animalFollower = new AnimalFollower(simulationEngine);
        this.map = simulationEngine.getMap();
        if (SimulationData.width >= 20 || SimulationData.height>= 20){
            borderWidth = "0";
        }
        grid = setUpGrid();
        if (map.isWrapped()){
            title.setText("Wrapped map"+(map.isMagical()?", magical variant, used: 0/3":""));
        }
        else{
            title.setText("Solid map"+(map.isMagical()?", magical variant, used: 0/3":""));
        }
    }

    private GridPane setUpGrid(){
        GridPane grid = new GridPane();
        for (int y = 0; y <= map.getDimension().y; y++) {
            for (int x = 0; x <= map.getDimension().x; x++) {
                Button button = new Button();
                int finalX = x;
                int finalY = y;
                button.setOnAction(e->animalSelected(new Vector2d(finalX, finalY)));
                int displayWidth = 240;
                button.setMinWidth((double) displayWidth /(map.getDimension().x+1));
                button.setMaxWidth((double) displayWidth /(map.getDimension().x+1));
                int displayHeight = 190;
                button.setMinHeight((double) displayHeight /(map.getDimension().y+1));
                button.setMaxHeight((double) displayHeight /(map.getDimension().y+1));
                button.setStyle(String.format("-fx-background-color: #ffffff;-fx-border-color: #000000; -fx-border-width: %spx;",borderWidth));
                grid.add(button,x,map.getDimension().y-y,1,1);
            }
        }

        return grid;
    }

    private void animalSelected(Vector2d position) {
        if (map.getObjectAt(position) instanceof Animal selectedAnimal){
            animalFollower.follow(selectedAnimal);
        }
    }

    private Button getButtonAt(int x, int y) {
        for (Node node : grid.getChildren()) {
            if (GridPane.getColumnIndex(node) == x && GridPane.getRowIndex(node) == y) {
                return (Button)node;
            }
        }
        return null;
    }
    private Button getButtonAt(Vector2d position) {
        for (Node node : grid.getChildren()) {
            if (GridPane.getColumnIndex(node) == position.x && GridPane.getRowIndex(node) == position.y) {
                return (Button)node;
            }
        }
        return null;
    }

    public void redraw(){
        for (int y = 0; y <= map.getDimension().y; y++) {
            for (int x = 0; x <= map.getDimension().x; x++) {
                Button button = getButtonAt(x,map.getDimension().y-y);
                Object mapObject = map.getObjectAt(new Vector2d(x,y));
                Color backgroundColour = Color.HONEYDEW;
                String borderColour = "000000";
                if(mapObject instanceof Animal){
                    backgroundColour = Color.YELLOW;
                    backgroundColour = backgroundColour.interpolate(Color.BROWN, (double)SimulationData.moveEnergy/((Animal) mapObject).getEnergy());
                }
                else if(mapObject instanceof Plant){
                    backgroundColour = Color.LIME;
                }

                if (map.isJungle(new Vector2d(x,y))){
                    borderColour = "FFA500";
                }
                button.setStyle(String.format("-fx-background-color: rgb(%s,%s,%s);-fx-border-color: #%s; -fx-border-width: %spx;", (int)(backgroundColour.getRed()*255),(int)(backgroundColour.getGreen()*255),(int)(backgroundColour.getBlue()*255), borderColour,borderWidth));
            }
        }
        if (map.isMagical()){
            title.setStyle(("-fx-background-color: #f8f4f4;"));
            title.setText(title.getText().substring(0, title.getText().length() - 3)+simulationEngine.getUsedMana()+"/3");
            if (lastMana!= simulationEngine.getUsedMana()){
                lastMana = simulationEngine.getUsedMana();
                //[ms]
                int notificationDuration = 2000;
                magicCooldown = notificationDuration /SimulationData.epochInterval;
            }
            if (magicCooldown>0){
                title.setStyle(("-fx-background-color: #B0E0E6;"));
                magicCooldown--;
            }
        }
        animalFollower.redraw();
    }

    public void highlightPositions(ArrayList<Vector2d> positions){
        for (Vector2d position : positions) {
            if ( getButtonAt(position) != null)
                getButtonAt(position.x,map.getDimension().y-position.y).setStyle("-fx-background-color: #0000FF;-fx-border-color: #000000; -fx-border-width: 0px;");
        }
    }

    public VBox getFollowerGUI(){
        return animalFollower.getGUI();
    }

    public Node getRoot(){
        VBox root = new VBox(5);
        root.getChildren().addAll(title,grid);
        return root;
    }
}
