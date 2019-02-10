package ga;

import dto.PositionDistanceDTO;
import dto.ProblemDTO;
import model.Customer;
import model.Depot;
import model.Vehicle;
import util.Util;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by stgr99 on 23/01/2019.
 */
public class Population {

    private ArrayList<Chromosome> population;
    private ArrayList<Chromosome> matingPool;
    private Chromosome bestChromosome = null;
    private ProblemDTO problemDTO;

    private boolean optimizePopulation;

    private int populationSize;
    private int generations;
    private int maxVehicleLoad;
    private int solutionsCount = 0;

    private double maxVehicleDuration;
    private double mutationRate;
    private double crossoverRate;
    private double averageFitness = 0.0;
    private double bestFitness = Integer.MAX_VALUE;

    public Population(ProblemDTO problemDTO, int populationSize, double mutationRate, double crossoverRate, boolean optimizePopulation) {
        population = new ArrayList<>();
        matingPool = new ArrayList<>();

        this.problemDTO = problemDTO;
        this.populationSize = populationSize;
        this.mutationRate = mutationRate;
        this.crossoverRate = crossoverRate;
        this.optimizePopulation = optimizePopulation;
        this.maxVehicleLoad = problemDTO.getDepots().get(0).getMaxVehicleLoad();
        this.maxVehicleDuration = problemDTO.getDepots().get(0).getMaxVehicleDuration();
    }

    public void setPopulation() {
        for (int i = 0; i < populationSize; i++) {
            Chromosome chromosome = new Chromosome(problemDTO, optimizePopulation);
            population.add(chromosome);
            System.out.println(++solutionsCount + " / " + populationSize);
        }
    }

    public void calculateFitness() {
        averageFitness = 0.0;
        for (Chromosome chromosome : population) {
            double totalFitness = 0.0;

            ArrayList<Vehicle> vehicles = chromosome.getVehicles();
            for (Vehicle vehicle : vehicles) {
                double vehicleFitness = Util.calculateVehicleDistance(vehicle);
                vehicle.setDuration(vehicleFitness);
                totalFitness += vehicleFitness;
            }

            if (totalFitness < bestFitness) {
                bestFitness = totalFitness;
                bestChromosome = chromosome;
            }

            averageFitness += totalFitness;

            chromosome.setFitness(totalFitness);
        }
        averageFitness = averageFitness / population.size();
    }

    public Chromosome getBestChromosome() {
        return this.bestChromosome;
    }

    private Chromosome getBestChromosome(ArrayList<Chromosome> population) {
        double bestFitness = (double) Integer.MAX_VALUE;
        Chromosome bestChromosome = null;

        for (Chromosome chromosome : population) {
            if (chromosome.getFitness() < bestFitness) {
                bestFitness = chromosome.getFitness();
                bestChromosome = chromosome;
            }
        }

        return bestChromosome;
    }

    /**
     * Tournament Selection
     *
     * @param numberOfContestants number of random contestants to be ranked.
     */
    public void naturalSelection(int numberOfContestants) {
        matingPool.clear();
        ArrayList<Chromosome> contestants = new ArrayList<>();

        while (matingPool.size() < populationSize) {
            contestants.clear();
            for (int i = 0; i < numberOfContestants; i++) {
                contestants.add(Util.getRandomChromosome(population));
            }
            matingPool.add(getBestChromosome(contestants));
        }
    }

    public void crossover(boolean mutate) {
        population.clear();

        // Add children to the population, given by the crossover rate.
        // If population is 100 and crossover rate is 0.8, 80 children will be added to the population.
        int childrenSize = (int) (populationSize * crossoverRate);
        while (population.size() < childrenSize) {
            Chromosome parent1 = Util.getRandomChromosome(matingPool);
            Chromosome parent2 = Util.getRandomChromosome(matingPool);

            ArrayList<Chromosome> children = crossover(parent1, parent2);
            Chromosome child1 = children.get(0);
            Chromosome child2 = children.get(1);

            if (mutate && Util.getRandomDouble(0, 1) < mutationRate) {
                mutate(child1);
                mutate(child2);
            }

            population.add(child1);
            population.add(child2);
        }

        matingPool.sort((o1, o2) -> {
            if (o1.getFitness() == o2.getFitness()) {
                return 0;
            }
            return o1.getFitness() < o2.getFitness() ? -1 : 1;
        });

        // Add the remaining top individuals to the population from the parent (mating) pool.
        int i = 0;
        while (population.size() < populationSize) {
            population.add(matingPool.get(i));
            i++;
        }

        generations++;
    }

    private void mutate(Chromosome chromosome) {
        ArrayList<Vehicle> vehicles = chromosome.getVehicles();

        double random = Util.getRandomDouble(0, 1);
        if (random > 0 && random <= 0.3) {
            inverseMutation(vehicles);
        } else if (random > 0.3 && random <= 0.8) {
            swapMutation(vehicles);
        } else if (random > 0.8 && random <= 0.9) {
            reRoutingMutation(vehicles);
        } else if (random > 0.9 && random <= 1) {
            reArrangeMutation(vehicles);
        }
    }

    private void inverseMutation(ArrayList<Vehicle> vehicles) {
        Vehicle randomVehicle = null;
        ArrayList<Customer> customers = null;
        int start = 0;
        int end = 0;

        int tries = 100;
        while (tries > 0) {
            randomVehicle = Util.getRandomVehicle(vehicles);
            customers = randomVehicle.getCustomers();

            double oldDuration = Util.calculateVehicleDistance(randomVehicle);

            start = Util.getRandomNumberInRange(0, customers.size() - 1);
            end = Util.getRandomNumberInRange(start, customers.size() - 1);
            Util.reverseCustomers(customers, start, end);

            if (maxVehicleDuration == 0) {
                break;
            }

            double newDuration = Util.calculateVehicleDistance(randomVehicle);

            if (newDuration <= oldDuration) {
                randomVehicle.setDuration(newDuration);
                break;
            }

            tries--;
        }

        if (tries == 0) {
            Util.reverseCustomers(customers, start, end);
        }

        randomVehicle.setEndDepot(Util.getBestEndDepot(problemDTO.getDepots(), randomVehicle));
    }

    private void swapMutation(ArrayList<Vehicle> vehicles) {
        Vehicle randomVehicle1 = null;
        Vehicle randomVehicle2 = null;

        int tries = 1000;
        boolean validSolution = false;
        while (!validSolution && tries > 0) {
            tries--;
            randomVehicle1 = Util.getRandomVehicle(vehicles);
            randomVehicle2 = Util.getRandomVehicle(vehicles);
            while (randomVehicle1 == randomVehicle2) {
                randomVehicle2 = Util.getRandomVehicle(vehicles);
            }

            ArrayList<Customer> customers1 = randomVehicle1.getCustomers();
            ArrayList<Customer> customers2 = randomVehicle2.getCustomers();
            int index = Util.getRandomNumberInRange(0, Math.min(customers1.size() - 1, customers2.size() - 1));

            Customer c1 = customers1.get(index);
            Customer c2 = customers2.get(index);

            customers1.remove(c1);
            customers2.remove(c2);

            int load1 = Util.calculateLoad(randomVehicle1, c2);
            int load2 = Util.calculateLoad(randomVehicle2, c1);
            if (load1 <= maxVehicleLoad && load2 <= maxVehicleLoad) {

                double duration1 = Util.calculateVehicleDistance(randomVehicle1, c2, index);
                double duration2 = Util.calculateVehicleDistance(randomVehicle2, c1, index);

                if (maxVehicleDuration != 0) {
                    if (duration1 <= maxVehicleDuration && duration2 <= maxVehicleDuration) {
                        validSolution = true;
                    } else {
                        // Reverse
                        customers1.add(index, c1);
                        customers2.add(index, c2);
                    }
                } else {
                    validSolution = true;
                }

                if (validSolution) {
                    // Keep swap
                    customers1.add(index, c2);
                    customers2.add(index, c1);

                    randomVehicle1.setDuration(duration1);
                    randomVehicle2.setDuration(duration2);
                }
            } else {
                // Reverse
                customers1.add(index, c1);
                customers2.add(index, c2);
            }
        }

        randomVehicle1.setEndDepot(Util.getBestEndDepot(problemDTO.getDepots(), randomVehicle1));
        randomVehicle2.setEndDepot(Util.getBestEndDepot(problemDTO.getDepots(), randomVehicle2));
    }

    private void reArrangeMutation(ArrayList<Vehicle> vehicles) {
        // Check if customers overlaps vehicles, and can be rearranged.
        for (Vehicle vehicle : vehicles) {
            ArrayList<Customer> vehicleCustomers = vehicle.getCustomers();
            ArrayList<Customer> customersToBeRemoved = new ArrayList<>();

            for (Customer vehicleCustomer : vehicleCustomers) {
                tryToRearrange(vehicles, vehicle, vehicleCustomer, customersToBeRemoved);
            }
            vehicleCustomers.removeAll(customersToBeRemoved);
        }
        Util.deleteEmptyVehicles(vehicles);
    }

    private void tryToRearrange(ArrayList<Vehicle> vehicles, Vehicle currentVehicle, Customer vehicleCustomer,
                                ArrayList<Customer> customersToBeRemoved) {
        for (Vehicle vehicle : vehicles) {
            if (vehicle == currentVehicle) {
                continue;
            }

            ArrayList<Customer> vehicleCustomers = vehicle.getCustomers();
            double oldDistance = Util.calculateVehicleDistance(vehicle);
            PositionDistanceDTO dto = Util.getBestPositionOnVehicle(vehicle, vehicleCustomer);
            int bestPosition = dto.getPosition();
            double newDistance = dto.getDistance();

            if (newDistance <= oldDistance) {
                if (Util.calculateLoad(vehicle, vehicleCustomer) > maxVehicleLoad) {
                    continue;
                }
                vehicleCustomers.add(bestPosition, vehicleCustomer);
                vehicle.setDuration(newDistance);
                customersToBeRemoved.add(vehicleCustomer);
            }

        }
    }


    private void reRoutingMutation(ArrayList<Vehicle> vehicles) {
        Vehicle randomVehicle = Util.getRandomVehicle(vehicles);
        ArrayList<Customer> vehicleCustomers = randomVehicle.getCustomers();
        ArrayList<Customer> customersToRemove = new ArrayList<>();

        for (Customer vehicleCustomer : vehicleCustomers) {
            for (Vehicle vehicle : vehicles) {
                if (vehicle == randomVehicle) {
                    continue;
                }

                int load = Util.calculateLoad(vehicle, vehicleCustomer);
                if (load > maxVehicleLoad) {
                    continue;
                }

                PositionDistanceDTO dto = Util.getBestPositionOnVehicle(vehicle, vehicleCustomer);
                int bestPosition = dto.getPosition();
                double distance = dto.getDistance();

                if (maxVehicleDuration != 0 && distance > maxVehicleDuration) {
                    continue;
                }

                vehicle.addCustomer(vehicleCustomer, bestPosition);
                vehicle.setEndDepot(Util.getBestEndDepot(problemDTO.getDepots(), vehicle));
                vehicle.setDuration(distance);
                customersToRemove.add(vehicleCustomer);
                break;
            }
        }

        vehicleCustomers.removeAll(customersToRemove);
        Util.deleteEmptyVehicles(vehicles);
    }

    private ArrayList<Chromosome> crossover(Chromosome parent1, Chromosome parent2) {
        ArrayList<Chromosome> children = new ArrayList<>();

        // Make two children by deep copying the parents.
        ArrayList<Vehicle> child1Vehicles = Util.deepCopyVehicles(parent1.getVehicles());
        ArrayList<Vehicle> child2Vehicles = Util.deepCopyVehicles(parent2.getVehicles());

        Collections.shuffle(child1Vehicles);
        Collections.shuffle(child2Vehicles);

        ArrayList<Customer> child1CustomersSliced = new ArrayList<>();
        ArrayList<Customer> child2CustomersSliced = new ArrayList<>();

        int n = Util.getRandomNumberInRange(1, child1Vehicles.size() / 2);
        for (int i = 0; i < n; i++) {
            Vehicle child1Vehicle = child1Vehicles.get(i);
            ArrayList<Customer> child1Customers = child1Vehicle.getCustomers();
            child1CustomersSliced.addAll(child1Customers.subList(0, child1Customers.size() / 2));
            child1Vehicle.setEndDepot(Util.getBestEndDepot(problemDTO.getDepots(), child1Vehicle));
        }
        n = Util.getRandomNumberInRange(1, child2Vehicles.size() / 2);
        for (int i = 0; i < n; i++) {
            Vehicle child2Vehicle = child2Vehicles.get(i);
            ArrayList<Customer> child2Customers = child2Vehicle.getCustomers();
            child2CustomersSliced.addAll(child2Customers.subList(0, child2Customers.size() / 2));
            child2Vehicle.setEndDepot(Util.getBestEndDepot(problemDTO.getDepots(), child2Vehicle));
        }

        // Remove the sublist of customers in child2 from child1's vehicles, and vice versa.
        Util.removeCustomers(child1Vehicles, child2CustomersSliced);
        Util.removeCustomers(child2Vehicles, child1CustomersSliced);

        // Distribute the sublist of customers in child2 in child1's vehicles, and vice versa.
        int solutionFound1 = distributeCustomersOnVehicles(child1Vehicles, child2CustomersSliced);
        int solutionFound2 = distributeCustomersOnVehicles(child2Vehicles, child1CustomersSliced);

        if (solutionFound1 == -1 || solutionFound2 == -1) {
            child1Vehicles = Util.deepCopyVehicles(parent1.getVehicles());
            child2Vehicles = Util.deepCopyVehicles(parent2.getVehicles());
        }

        // Delete every vehicle that has no customers attached.
        Util.deleteEmptyVehicles(child1Vehicles);
        Util.deleteEmptyVehicles(child2Vehicles);

        // Add and return the children.
        children.add(new Chromosome(child1Vehicles));
        children.add(new Chromosome(child2Vehicles));

        return children;
    }

    private int distributeCustomersOnVehicles(ArrayList<Vehicle> vehicles, ArrayList<Customer> customersToDistribute) {
        Vehicle minVehicle = null;
        int bestPosition = 0;
        double minDurationIncrease;

        for (Customer emptyCustomer : customersToDistribute) {
            minDurationIncrease = Double.MAX_VALUE;
            double duration = 0.0;

            for (Vehicle vehicle : vehicles) {
                int load = Util.calculateLoad(vehicle, emptyCustomer);
                if (load > maxVehicleLoad) {
                    continue;
                }

                double oldDuration = Util.calculateVehicleDistance(vehicle);
                PositionDistanceDTO dto = Util.getBestPositionOnVehicle(vehicle, emptyCustomer);
                int position = dto.getPosition();
                double newDuration = dto.getDistance();
                double durationIncrease = newDuration - oldDuration;

                if (maxVehicleDuration != 0 && newDuration > maxVehicleDuration) {
                    continue;
                }

                if (durationIncrease < minDurationIncrease) {
                    minVehicle = vehicle;
                    bestPosition = position;
                    minDurationIncrease = durationIncrease;
                    duration = newDuration;
                }
            }

            if (minVehicle != null) {
                minVehicle.getCustomers().add(bestPosition, emptyCustomer);
                minVehicle.setEndDepot(Util.getBestEndDepot(problemDTO.getDepots(), minVehicle));
                minVehicle.setDuration(duration);
            } else {
                Depot startDepot = Util.getRandomStartDepot(problemDTO.getDepots(), vehicles);
                if (startDepot != null) {
                    Vehicle vehicle = new Vehicle();
                    vehicle.setStartDepot(startDepot);
                    vehicle.setEndDepot(Util.getRandomDepot(problemDTO.getDepots()));

                    int load = Util.calculateLoad(vehicle, emptyCustomer);
                    if (load <= maxVehicleLoad) {
                        vehicle.addCustomer(emptyCustomer);
                        vehicle.setDuration(Util.calculateVehicleDistance(vehicle, emptyCustomer));
                    }

                    vehicles.add(vehicle);
                } else {
                    System.out.println("ERROR: No more available vehicles and constraints fails.");
                    return -1;
                }
            }
        }
        return 1;
    }

    public int getGenerations() {
        return generations;
    }

    public double getAverageFitness() {
        return averageFitness;
    }

    public void setMutationRate(double mutationRate) {
        this.mutationRate = mutationRate;
    }

    public void setCrossoverRate(double crossoverRate) {
        this.crossoverRate = crossoverRate;
    }
}
