import dto.ProblemDTO;
import ga.DNA;
import ga.Population;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import model.Customer;
import model.Depot;
import model.Route;
import util.FileHandler;
import util.Util;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by stgr99 on 22/01/2019.
 */
public class MainController {
    @FXML
    public Canvas canvas;

    @FXML
    public ChoiceBox<String> choiceBox;

    private FileHandler fileHandler;

    private ArrayList<Customer> customers;
    private ArrayList<Depot> depots;
    private double multiplier = 1.0;
    private int drawWidth = 5;
    private Random random;

    @FXML
    public void initialize() {
        fileHandler = new FileHandler();
        random = new Random();
        setProblems();
    }

    private void setProblems() {
        ArrayList<String> dataFiles = Util.getDataFiles();
        choiceBox.setItems(FXCollections.observableArrayList(dataFiles));
        choiceBox.getSelectionModel().selectFirst();

        choiceBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) ->
                        onProblemSelect(newValue));

        onProblemSelect(dataFiles.get(0));
    }

    private void onProblemSelect(String dataFile) {
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        ProblemDTO problemDTO = fileHandler.getFileInformation(dataFile);
        customers = problemDTO.getCustomers();
        depots = problemDTO.getDepots();

        setAdjustedCoordinates();
        render(null);
    }

    /**
     * Makes every x and y coordinate in model.Customer and model.Depot class adjusted,
     * so that the minimum x or y value starts in 0.
     * <p>
     * This is done because the problem files have both negative and positive coordinates,
     * which causes problems when drawing objects on a JavaFX canvas.
     */
    private void setAdjustedCoordinates() {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        // Find minimum x, minimum y, maximum x and maximum y.
        for (Customer customer : customers) {
            if (customer.getX() < minX) {
                minX = customer.getX();
            }
            if (customer.getY() < minY) {
                minY = customer.getY();
            }
            if (customer.getX() > maxX) {
                maxX = customer.getX();
            }
            if (customer.getY() > maxY) {
                maxY = customer.getY();
            }
        }

        // Find minimum x, minimum y, maximum x and maximum y.
        for (Depot depot : depots) {
            if (depot.getX() < minX) {
                minX = depot.getX();
            }
            if (depot.getY() < minY) {
                minY = depot.getY();
            }
            if (depot.getX() > maxX) {
                maxX = depot.getX();
            }
            if (depot.getY() > maxY) {
                maxY = depot.getY();
            }
        }

        // Set difference from 0.
        int difference = 0;
        int min = Math.min(minX, minY);
        if (min < 0) {
            difference = Math.abs(min);
        } else if (min > 0) {
            difference = -min;
        }

        // Set adjusted X and Y values.
        for (Customer customer : customers) {
            customer.setAdjustedX(customer.getX() + difference);
            customer.setAdjustedY(customer.getY() + difference);
        }

        // Set adjusted X and Y values.
        for (Depot depot : depots) {
            depot.setAdjustedX(depot.getX() + difference);
            depot.setAdjustedY(depot.getY() + difference);
        }

        // Set multiplier variable to stretch points to fill JavaFX canvas.
        multiplier = 320.0 / (float) Math.max(maxX + difference, maxY + difference);
    }

    private void render(DNA dna) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        renderCustomers(gc);
        renderDepots(gc);
        renderRoute(gc, dna);
    }

    private void renderRoute(GraphicsContext gc, DNA dna) {
        if (dna != null) {
            gc.setLineWidth(1);
            int colorNumber = 0;

            ArrayList<Route> routes = dna.getRoutes();
            for (Route route : routes) {
                ArrayList<Customer> customers = route.getCustomers();

                gc.setStroke(getRandomColor(colorNumber++));

                // Render a line from start depot to first customer
                gc.strokeLine(
                        route.getStartDepot().getAdjustedX() * multiplier + (drawWidth / 2),
                        route.getStartDepot().getAdjustedY() * multiplier + (drawWidth / 2),
                        customers.get(0).getAdjustedX() * multiplier + (drawWidth / 2),
                        customers.get(0).getAdjustedY() * multiplier + (drawWidth / 2)
                );

                // Render a line from customer (i) to customer (i + 1)
                for (int i = 0; i < customers.size() - 1; i++) {
                    gc.strokeLine(
                            customers.get(i).getAdjustedX() * multiplier + (drawWidth / 2),
                            customers.get(i).getAdjustedY() * multiplier + (drawWidth / 2),
                            customers.get(i + 1).getAdjustedX() * multiplier + (drawWidth / 2),
                            customers.get(i + 1).getAdjustedY() * multiplier + (drawWidth / 2)
                    );
                }

                // Render a line from last customer to end depot
                gc.strokeLine(
                        customers.get(customers.size() - 1).getAdjustedX() * multiplier + (drawWidth / 2),
                        customers.get(customers.size() - 1).getAdjustedY() * multiplier + (drawWidth / 2),
                        route.getEndDepot().getAdjustedX() * multiplier + (drawWidth / 2),
                        route.getEndDepot().getAdjustedY() * multiplier + (drawWidth / 2)
                );


            }
        }
    }

    private void renderCustomers(GraphicsContext gc) {
        if (customers != null) {
            gc.setFill(Color.BLACK);
            for (Customer customer : customers) {
                gc.fillOval(
                        customer.getAdjustedX() * multiplier,
                        customer.getAdjustedY() * multiplier,
                        drawWidth,
                        drawWidth);
            }
        }
    }

    private void renderDepots(GraphicsContext gc) {
        if (depots != null) {
            gc.setFill(Color.DEEPSKYBLUE);
            for (Depot depot : depots) {
                gc.fillOval(
                        depot.getAdjustedX() * multiplier,
                        depot.getAdjustedY() * multiplier,
                        drawWidth,
                        drawWidth
                );
            }
        }
    }

    public void canvasClicked(MouseEvent mouseEvent) {
        canvas.getGraphicsContext2D().scale(1.2, 1.2);

        System.out.println(mouseEvent.getX());
        System.out.println(mouseEvent.getY());
    }

    private Color getRandomColor(int colorNumber) {
        Color[] colors = {
                Color.GREEN, Color.YELLOW, Color.RED, Color.HOTPINK, Color.DARKORANGE, Color.YELLOWGREEN,
                Color.SADDLEBROWN, Color.PURPLE, Color.DEEPPINK, Color.DARKOLIVEGREEN, Color.DARKSEAGREEN,
                Color.DARKRED, Color.INDIANRED, Color.MEDIUMVIOLETRED, Color.ORANGERED, Color.PALEVIOLETRED,
                Color.BROWN, Color.PINK, Color.ORANGE, Color.MEDIUMPURPLE};

        if (colorNumber > colors.length) {
            return Color.color(random.nextFloat(), random.nextFloat(), random.nextFloat());
        } else {
            return colors[colorNumber];
        }
    }

    public void startAlgorithm() {
        ProblemDTO problemDTO = new ProblemDTO(customers, depots);
        Population population = new Population(problemDTO, 100, 0.01);

        population.setPopulation();
        population.calculateFitness();

        DNA bestDNA = population.getBestDNA(population.getPopulation());
        System.out.println("Best fitness: " + bestDNA.getFitness());
        render(bestDNA);

        population.naturalSelection(3);
        population.crossover();

    }
}
