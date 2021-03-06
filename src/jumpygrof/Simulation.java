package jumpygrof;

import jumpygrof.datastructure.Graph;
import jumpygrof.datastructure.LinkedList;

import javax.swing.*;

public class Simulation extends JFrame {

    private Graph<Point, Integer> graph;
    private LinkedList<Point> pointList;
    private LinkedList<Kangaroo> kangarooList;
    private int COLONY_MAX = 0;
    private boolean hasFormedColony = false;

    Simulation() {
        setup();
        addInput();
        start();
    }

    private void setup() {
        // JFrame
        setSize(600, 600);
        setTitle("Jumpy Grof");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        graph = new Graph<>();
        pointList = new LinkedList();
        kangarooList = new LinkedList();
    }

    private void addInput() {
        //Points and Edges
        pointList.add(new Point("1", 16, 10));
        pointList.add(new Point("2", 5, 3));
        pointList.add(new Point("3", 10, 8));
        pointList.add(new Point("4", 11, 6));

        for (int i = 0; i < pointList.size(); i++) graph.addVertice(pointList.get(i));

        graph.addEdge(pointList.get(0), pointList.get(1), 4);
        graph.addEdge(pointList.get(0), pointList.get(2), 1);
        graph.addEdge(pointList.get(0), pointList.get(3), 5);
        graph.addEdge(pointList.get(2), pointList.get(3), 3);


        // Kangaroos
        kangarooList.add(new Kangaroo(true, 6));
        kangarooList.add(new Kangaroo(false, 5));
        kangarooList.add(new Kangaroo(false, 3));
        kangarooList.add(new Kangaroo(true, 10));
        kangarooList.add(new Kangaroo(true, 6));

        pointList.get(0).addKangaroo(kangarooList.get(0));
        pointList.get(1).addKangaroo(kangarooList.get(1));
        pointList.get(3).addKangaroo(kangarooList.get(2));
        pointList.get(0).addKangaroo(kangarooList.get(3));
        pointList.get(2).addKangaroo(kangarooList.get(4));

        COLONY_MAX = 3;
    }

    private void start() {
        // Kangaroo starts picking up the available food into their pouches
        System.out.println("Current Food Available");
        for (int i = 0; i < pointList.size(); i++) {
            Point currentPoint = pointList.get(i);
            currentPoint.startPickupFood();
            System.out.println("Point " + currentPoint.getID() + " : " + currentPoint.getCurrentFoodAmount());
        }
        for (int i = 0; i < kangarooList.size(); i++) {
            Kangaroo currentKangaroo = kangarooList.get(i);
            System.out.println("Kangaroo " + currentKangaroo.getID() + " is now at Point " + currentKangaroo.getCurrentPoint().getID() +
                    " with food amount of " + currentKangaroo.getCurrentFoodAmount());
        }
        while (!hasFormedColony) {
            for (int i = 0; i < kangarooList.size(); i++) {
                Kangaroo currentKangaroo = kangarooList.get(i);
                if (!currentKangaroo.isMale()) continue; // Only males allowed to hop
                Point currentPoint = currentKangaroo.getCurrentPoint();
                Point nextPoint = whereToMove(currentKangaroo, currentPoint);
                if (nextPoint != null) move(currentKangaroo, currentPoint, nextPoint);
            }
            break;
        }
    }

    /*
    Determines next point for kangaroo to hop
    Returns null if kangaroo cannot move anywhere
     */
    private Point whereToMove(Kangaroo kangaroo, Point point) {
        LinkedList<Point> nodes = graph.getAdjascent(point);
        if (nodes.size() != 0) {
            int max = 0;
            Point to = null; // initialise variable
            for (int i = 0; i < nodes.size(); i++) {
                Point possiblePoint = nodes.get(i);
                if (possiblePoint.compareTo(point) == 0) continue; // No need to compare between same points
                int worth = getPointWorth(kangaroo, possiblePoint);
                System.out.println(kangaroo.toString() + " is considering " + possiblePoint.toString() + " with worth of " + worth);
                if (worth > max) {
                    max = worth;
                    to = possiblePoint;
                }
            }
            return to;
        }
        return null;
    }

    private int getFoodNeededToHop(Kangaroo kangaroo, Point to) {
        int height = graph.getWeight(kangaroo.getCurrentPoint(), to);
        return height + (kangaroo.getCurrentFoodAmount()/2);
    }

    private boolean hasEnoughFoodToHop(Kangaroo kangaroo, Point to) {
        return kangaroo.getCurrentFoodAmount() >= getFoodNeededToHop(kangaroo, to);
    }

    private int getPossibleExtraFood(Kangaroo kangaroo, Point to) {
        return to.getCurrentFoodAmount() - getFoodNeededToHop(kangaroo, to);
    }

    // TODO Figure out the best day to determine a point's worthiness
    private int getPointWorth(Kangaroo kangaroo, Point to) {
        // If has more females
        // If has more food
        // If has enough food
        int worth = 0;
        int foodNeeded = getFoodNeededToHop(kangaroo, to);
        int extraFood = getPossibleExtraFood(kangaroo, to);
        if (extraFood > 0)  {
            worth += extraFood;
        } else if (extraFood <= 0 && foodNeeded < kangaroo.getCurrentFoodAmount()) {
            return 0; // not extra food and not enough food to hop, bail out
        }

        worth += to.getCurrentFemaleKangaroo();
        worth += to.getCurrentFoodAmount();

        return worth;
    }

    public void move(Kangaroo kangaroo, Point from, Point to) {
        System.out.println("Moving " + kangaroo.toString() + " from " + from.toString() + " to " + to.toString());
        int foodInPouch = kangaroo.getCurrentFoodAmount();
        int foodInPoint = to.getCurrentFoodAmount();
        int foodNeeded = getFoodNeededToHop(kangaroo, to);

        foodInPoint -= foodNeeded;
        if (foodInPoint < 0) {
            foodInPouch += foodInPoint; // Use up the remaining food needed in their pouch
            foodInPoint = 0;
        } else if (foodInPoint > 0) { // Kangaroo picks up the extra foods at destination point to its capacity
            int extraFood = getPossibleExtraFood(kangaroo, to);
            int capableTotalFood = foodInPouch + extraFood;
            int difference = 0;
            if (capableTotalFood > kangaroo.getCapacity()) { // If it exceeds what it can carry, sum it back to the difference
                difference = capableTotalFood - kangaroo.getCapacity();
                foodInPouch = kangaroo.getCapacity();
            }
            foodInPoint =  foodInPoint - extraFood + difference;
            //foodInPoint =  foodInPoint - extraFood + (foodInPouch + extraFood - kangaroo.getCapacity());
        }

        kangaroo.setCurrentFoodAmount(foodInPouch);
        to.setCurrentFoodAmount(foodInPoint);

        from.removeKangaroo(kangaroo);
        to.addKangaroo(kangaroo);
        if (to.getCurrentCapacity() == COLONY_MAX) {
            System.out.println("Point " + to.getID() + " got to form a colony!");
            hasFormedColony = true;
        }
    }
}
