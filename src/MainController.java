import dto.ProblemDTO;
import ga.Chromosome;
import ga.Population;
import javafx.animation.AnimationTimer;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import model.Customer;
import model.Depot;
import model.Vehicle;
import util.FileHandler;
import util.Util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by stgr99 on 22/01/2019.
 */
public class MainController {

    @FXML
    public CheckBox optimizerCheckBox;

    @FXML
    public CheckBox renderCheckbox;

    @FXML
    public Button startButton;

    @FXML
    public Canvas canvas;

    @FXML
    public ChoiceBox<String> choiceBox;

    @FXML
    public TextField populationSizeField;

    @FXML
    public TextField mutationRateField;

    @FXML
    public TextField crossoverRateField;

    @FXML
    public Text averageFitnessText;

    @FXML
    public Text fitnessText;

    @FXML
    public Text generationsText;

    @FXML
    public Text timeText;

    private AnimationTimer animationTimer;
    private FileHandler fileHandler;
    private Population currentPopulation;
    private ArrayList<Customer> customers;
    private ArrayList<Depot> depots;
    private ArrayList<Depot> coloredDepots;
    private double multiplier = 1.0;
    private int drawWidth = 5;
    private int populationSize = 100;
    private double mutationRate = 0.05;
    private double crossoverRate = 0.8;
    private boolean algorithmRunning = false;
    private long startTime = System.nanoTime();

    @FXML
    public void initialize() {
        fileHandler = new FileHandler();
        coloredDepots = new ArrayList<>();
        setFXMLVariables();
        setProblems();
    }

    private void setFXMLVariables() {
        populationSizeField.setPromptText(Integer.toString(populationSize));
        populationSizeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals("")) {
                try {
                    populationSize = Integer.parseInt(newValue);
                } catch (NumberFormatException e) {
                    populationSizeField.setText(oldValue);
                }
            }
        });
        mutationRateField.setPromptText(Double.toString(mutationRate));
        mutationRateField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals("")) {
                mutationRate = 0.01;
            } else if (newValue.matches("[-+]?[0-9]*\\.?[0-9]*")) {
                mutationRate = Double.parseDouble(newValue);
                if (mutationRate > 1) {
                    mutationRate = 1;
                    mutationRateField.setText(Double.toString(mutationRate));
                }
            } else {
                mutationRateField.setText(Double.toString(mutationRate));
            }
        });
        crossoverRateField.setPromptText(Double.toString(crossoverRate));
        crossoverRateField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals("")) {
                crossoverRate = 0.08;
            } else if (newValue.matches("[-+]?[0-9]*\\.?[0-9]*")) {
                crossoverRate = Double.parseDouble(newValue);
                if (crossoverRate > 1) {
                    crossoverRate = 1;
                    crossoverRateField.setText(Double.toString(crossoverRate));
                }
            } else {
                crossoverRateField.setText(Double.toString(crossoverRate));
            }
        });
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

        if (depots.get(0).getMaxVehicleDuration() != 0) {
            optimizerCheckBox.setSelected(true);
        } else {
            optimizerCheckBox.setSelected(false);
        }

        setAdjustedCoordinates();
        render(null);
    }

    /**
     * Makes every x and y coordinate in model.Customer and model.Depot class adjusted,
     * so that the minimum x or y value starts in 0.
     * <p>
     * This is done because the problem files have both negative and positive coordinates,
     * which causes problems when drawing objects on a JavaFX canvas where the min value is  (0, 0).
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

    private void render(Chromosome chromosome) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        renderCustomers(gc);
        renderDepots(gc);
        renderVehicles(gc, chromosome);
    }

    private void renderVehicles(GraphicsContext gc, Chromosome chromosome) {
        if (chromosome != null) {
            gc.setLineWidth(1);
            int colorNumber = 0;
            coloredDepots.clear();

            ArrayList<Vehicle> vehicles = chromosome.getVehicles();
            for (Vehicle vehicle : vehicles) {
                ArrayList<Customer> customers = vehicle.getCustomers();


                Color color;
                boolean renderAll = renderCheckbox.isSelected();
                if (renderAll) {
                    color = Util.getRandomColor(colorNumber++);
                } else {
                    color = getDepotColor(vehicle);
                }

                gc.setStroke(color);

                // Render a line from start depot to first customer
                gc.strokeLine(
                        vehicle.getStartDepot().getAdjustedX() * multiplier + (drawWidth / 2),
                        vehicle.getStartDepot().getAdjustedY() * multiplier + (drawWidth / 2),
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
                        vehicle.getEndDepot().getAdjustedX() * multiplier + (drawWidth / 2),
                        vehicle.getEndDepot().getAdjustedY() * multiplier + (drawWidth / 2)
                );


            }
        }
    }

    private Color getDepotColor(Vehicle vehicle) {
        Depot startDepot = vehicle.getStartDepot();

        for (int i = 0; i < coloredDepots.size(); i++) {
            if (coloredDepots.get(i) == startDepot) {
                return Util.getRandomColor(i);
            }
        }

        coloredDepots.add(startDepot);
        return Util.getRandomColor(coloredDepots.size() - 1);
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

    private void changeStartButton() {
        if (algorithmRunning) {
            startButton.setText("Stop");
            startButton.setStyle("-fx-background-color: red;");
        } else {
            startButton.setText("Start");
            startButton.setStyle("-fx-background-color: green;");
        }
    }

    public void startAlgorithm() {
        algorithmRunning = !algorithmRunning;
        changeStartButton();
        if (!algorithmRunning) {
            if (animationTimer != null) {
                animationTimer.stop();
            }
            optimizerCheckBox.setDisable(false);
            produceSolutionFile();
            return;
        }

        fitnessText.setText("Best fitness: ");
        generationsText.setText("Generations: ");
        timeText.setText("Time spent: ");

        startTime = System.nanoTime();
        boolean optimizePopulation = optimizerCheckBox.isSelected();
        optimizerCheckBox.setDisable(true);

        ProblemDTO problemDTO = new ProblemDTO(customers, depots);
        Population population = new Population(problemDTO, populationSize, mutationRate, crossoverRate, optimizePopulation);
        population.setPopulation();
        population.calculateFitness();
        currentPopulation = population;

        renderText(population);
        render(population.getBestChromosome());

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (algorithmRunning) {
                    population.setMutationRate(mutationRate);
                    population.setCrossoverRate(crossoverRate);
                    population.naturalSelection(3);
                    population.crossover(true);
                    population.calculateFitness();
                    currentPopulation = population;
                    renderText(population);
                    render(population.getBestChromosome());
                }
            }

        };
        animationTimer.start();

    }

    private void renderText(Population population) {
        double bestFitness = population.getBestChromosome().getFitness();
        double averageFitness = population.getAverageFitness();
        fitnessText.setText("Best fitness:  " + Util.getRoundedDouble(bestFitness, 3));
        averageFitnessText.setText("Average fitness:  " + Util.getRoundedDouble(averageFitness, 3));
        generationsText.setText("Generations:  " + population.getGenerations());

        long currentTime = System.nanoTime();
        long nanoTimeElapsed = currentTime - startTime;
        long secondsElapsed = TimeUnit.SECONDS.convert(nanoTimeElapsed, TimeUnit.NANOSECONDS);

        timeText.setText("Time spent: " + LocalTime.MIN.plusSeconds(secondsElapsed).toString());
    }

    private void produceSolutionFile() {
        try {
            PrintWriter writer = new PrintWriter(
                    "src/resources/ga_solutions/" +
                            choiceBox.getValue().substring(25, choiceBox.getValue().length()) +
                            ".res",
                    "UTF-8");

            Chromosome chromosome = currentPopulation.getBestChromosome();
            writer.println(new BigDecimal(chromosome.getFitness()).setScale(2, RoundingMode.CEILING));

            ArrayList<Vehicle> vehicles = chromosome.getVehicles();
            setOrderForSolutionFile(vehicles);

            for (Vehicle vehicle : vehicles) {
                String startDepot = Integer.toString(vehicle.getStartDepot().getId());
                writer.print(Util.leftPad(startDepot, 2, ' ') + "  ");

                String vehicleNumber = Integer.toString(vehicle.getVehicleNumber());
                writer.print(Util.leftPad(vehicleNumber, 2, ' ') + "  ");

                String duration = Double.toString(Util.getRoundedDouble(vehicle.getDuration(), 2));
                writer.print(Util.leftPad(duration, 7, ' ') + "  ");

                String customerSize = Integer.toString(vehicle.getCustomers().size());
                writer.print(Util.leftPad(customerSize, 2, ' ') + "  ");

                String endDepot = Integer.toString(vehicle.getEndDepot().getId());
                writer.print(Util.leftPad(endDepot, 2, ' ') + "  ");

                ArrayList<Customer> customers = vehicle.getCustomers();
                for (Customer customer : customers) {
                    writer.print(" " + customer.getId());
                }
                writer.println();
            }

            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void setOrderForSolutionFile(ArrayList<Vehicle> vehicles) {
        vehicles.sort((o1, o2) -> {
            if (o1.getStartDepot().getId() == o2.getStartDepot().getId())
                return 0;
            return o1.getStartDepot().getId() < o2.getStartDepot().getId() ? -1 : 1;
        });

        Depot currentDepot = vehicles.get(0).getStartDepot();
        int fixedVehicleNumber = 1;

        for (Vehicle vehicle : vehicles) {
            Depot startDepot = vehicle.getStartDepot();
            if (startDepot != currentDepot) {
                currentDepot = startDepot;
                fixedVehicleNumber = 1;
            }
            vehicle.setVehicleNumber(fixedVehicleNumber++);
        }
    }
}
