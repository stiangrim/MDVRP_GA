package ga;

import dto.PositionDistanceDTO;
import dto.ProblemDTO;
import model.Customer;
import model.Depot;
import model.MapObject;
import model.Vehicle;
import util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by stgr99 on 23/01/2019.
 */
public class Population {

    private ArrayList<DNA> population;
    private ArrayList<DNA> matingPool;
    private DNA bestDNA = null;
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
            DNA dna = new DNA(problemDTO, optimizePopulation);
            population.add(dna);
            System.out.println(++solutionsCount + " / " + populationSize);
        }
    }

    public void calculateFitness() {
        averageFitness = 0.0;
        for (DNA dna : population) {
            double totalFitness = 0.0;

            ArrayList<Vehicle> vehicles = dna.getVehicles();
            for (Vehicle vehicle : vehicles) {
                double vehicleFitness = Util.calculateVehicleDistance(vehicle);
                vehicle.setDuration(vehicleFitness);
                totalFitness += vehicleFitness;
            }

            if (totalFitness < bestFitness) {
                bestFitness = totalFitness;
                bestDNA = dna;
            }

            averageFitness += totalFitness;

            dna.setFitness(totalFitness);
        }
        averageFitness = averageFitness / population.size();
    }

    public DNA getBestDNA() {
        return this.bestDNA;
    }

    private DNA getBestDNA(ArrayList<DNA> population) {
        double bestFitness = (double) Integer.MAX_VALUE;
        DNA bestDNA = null;

        for (DNA dna : population) {
            if (dna.getFitness() < bestFitness) {
                bestFitness = dna.getFitness();
                bestDNA = dna;
            }
        }

        return bestDNA;
    }

    /**
     * Tournament Selection
     *
     * @param numberOfContestants number of random contestants to be ranked.
     */
    public void naturalSelection(int numberOfContestants) {
        matingPool.clear();
        ArrayList<DNA> contestants = new ArrayList<>();

        while (matingPool.size() < populationSize) {
            contestants.clear();
            for (int i = 0; i < numberOfContestants; i++) {
                contestants.add(Util.getRandomDNA(population));
            }
            matingPool.add(getBestDNA(contestants));
        }
    }

    public void crossover(boolean mutate) {
        population.clear();

        // Add children to the population, given by the crossover rate.
        // If population is 100 and crossover rate is 0.8, 80 children will be added to the population.
        int childrenSize = (int) (populationSize * crossoverRate);
        while (population.size() < childrenSize) {
            DNA parent1 = Util.getRandomDNA(matingPool);
            DNA parent2 = Util.getRandomDNA(matingPool);

            ArrayList<DNA> children = crossoverV2(parent1, parent2);
            DNA child1 = children.get(0);
            DNA child2 = children.get(1);

            if (Util.getNumberOfCustomers(child1.getVehicles()) > 360) {
                System.out.println("Crossover1");
            }

            if (Util.getNumberOfCustomers(child2.getVehicles()) > 360) {
                System.out.println("Crossover2");
            }

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

    private void mutate(DNA dna) {
        ArrayList<Vehicle> vehicles = dna.getVehicles();

        double bestOldFitness = bestDNA.getFitness();


        double random = Util.getRandomDouble(0, 1);
        if (random > 0 && random <= 0.3) {
            inverseMutation(vehicles);
        } else if (random > 0.3 && random <= 0.8) {
            swapMutation(vehicles);
        } else if (random > 0.8 && random <= 0.9) {
            reRoutingMutationV2(vehicles);
        } else if (random > 0.9 && random <= 1) {
            reArrangeMutation(vehicles);
        }

        calculateFitness();
        double bestNewFitness = bestDNA.getFitness();

        if (bestNewFitness < bestOldFitness) {
            if (random > 0 && random <= 0.2) {
                System.out.println("Inverse Mutation " + (bestNewFitness - bestOldFitness));
            } else if (random > 0.2 && random <= 0.6) {
                System.out.println("Swap Mutation " + (bestNewFitness - bestOldFitness));
            } else if (random > 0.6 && random <= 0.9) {
                System.out.println("ReRouting Mutation " + (bestNewFitness - bestOldFitness));
            } else if (random > 0.9 && random <= 1) {
                System.out.println("ReArrange Mutation " + (bestNewFitness - bestOldFitness));
            }
        }


        int customerAmount = 0;
        for (Vehicle vehicle : vehicles) {
            customerAmount += vehicle.getCustomers().size();
        }
        if (customerAmount > 360) {
            System.out.println(customerAmount);
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
                if (maxVehicleDuration != 0) {
                    double duration1 = Util.calculateVehicleDistance(randomVehicle1, c2, index);
                    double duration2 = Util.calculateVehicleDistance(randomVehicle2, c1, index);

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
                customersToBeRemoved.add(vehicleCustomer);
            }

        }
    }


    private void reRoutingMutationV2(ArrayList<Vehicle> vehicles) {
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
                customersToRemove.add(vehicleCustomer);
                break;
            }
        }

        vehicleCustomers.removeAll(customersToRemove);
        Util.deleteEmptyVehicles(vehicles);
    }

    private ArrayList<DNA> crossoverV3(DNA parent1, DNA parent2) {
        ArrayList<DNA> children = new ArrayList<>();

        // Make two children by deep copying the parents.
        ArrayList<Vehicle> child1Vehicles = Util.deepCopyVehicles(parent1.getVehicles());
        ArrayList<Vehicle> child2Vehicles = Util.deepCopyVehicles(parent2.getVehicles());

        // Create array lists that contains the customers to be removed from vehicle x and distributed on vehicle y
        ArrayList<Customer> child1CustomersSliced = new ArrayList<>();
        ArrayList<Customer> child2CustomersSliced = new ArrayList<>();

        // Get two different depots
        Depot child1Depot = Util.getRandomDepot(problemDTO.getDepots());
        Depot child2Depot = Util.getRandomDepot(problemDTO.getDepots());
        while (child2Depot == child1Depot) {
            child2Depot = Util.getRandomDepot(problemDTO.getDepots());
        }

        // Add all customers that is on a vehicle with start depot on child1Depot
        for (Vehicle child1Vehicle : child1Vehicles) {
            if (child1Vehicle.getStartDepot() == child1Depot) {
                child1CustomersSliced.addAll(child1Vehicle.getCustomers());
            }
        }

        // Add all customers that is on a vehicle with start depot on child2Depot
        for (Vehicle child2Vehicle : child2Vehicles) {
            if (child2Vehicle.getStartDepot() == child2Depot) {
                child2CustomersSliced.addAll(child2Vehicle.getCustomers());
            }
        }

        // Remove the sublist of customers in child2 from child1's vehicles, and vice versa.
        Util.removeCustomers(child1Vehicles, child2CustomersSliced);
        Util.removeCustomers(child2Vehicles, child1CustomersSliced);

        // Update end depots
        for (Vehicle child1Vehicle : child1Vehicles) {
            child1Vehicle.setEndDepot(Util.getBestEndDepot(problemDTO.getDepots(), child1Vehicle));
        }
        for (Vehicle child2Vehicle : child2Vehicles) {
            child2Vehicle.setEndDepot(Util.getBestEndDepot(problemDTO.getDepots(), child2Vehicle));
        }

        // Distribute the sublist of customers in child2 in child1's vehicles, and vice versa.
        distributeCustomersOnVehicles(child1Vehicles, child2CustomersSliced);
        distributeCustomersOnVehicles(child2Vehicles, child1CustomersSliced);

        // Delete every vehicle that has no customers attached.
        Util.deleteEmptyVehicles(child1Vehicles);
        Util.deleteEmptyVehicles(child2Vehicles);

        // Add and return the children.
        children.add(new DNA(child1Vehicles));
        children.add(new DNA(child2Vehicles));

        return children;
    }

    private ArrayList<DNA> crossoverV2(DNA parent1, DNA parent2) {
        ArrayList<DNA> children = new ArrayList<>();

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
        Util.removeCustomersV2(child1Vehicles, child2CustomersSliced);
        Util.removeCustomersV2(child2Vehicles, child1CustomersSliced);

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
        children.add(new DNA(child1Vehicles));
        children.add(new DNA(child2Vehicles));

        return children;
    }

    private ArrayList<DNA> crossover(DNA parent1, DNA parent2) {
        ArrayList<DNA> children = new ArrayList<>();

        // Make two children by deep copying the parents.
        ArrayList<Vehicle> child1Vehicles = Util.deepCopyVehicles(parent1.getVehicles());
        ArrayList<Vehicle> child2Vehicles = Util.deepCopyVehicles(parent2.getVehicles());

        // Select a random vehicle from both children.
        Vehicle child1Vehicle = Util.getRandomVehicle(child1Vehicles);
        Vehicle child2Vehicle = Util.getRandomVehicle(child2Vehicles);

        // Get half of the customers as a sublist from each vehicle.
        ArrayList<Customer> child1Customers = child1Vehicle.getCustomers();
        ArrayList<Customer> child2Customers = child2Vehicle.getCustomers();
        ArrayList<Customer> child1CustomersSliced = new ArrayList<>(child1Customers.subList(0, child1Customers.size() / 2));
        ArrayList<Customer> child2CustomersSliced = new ArrayList<>(child2Customers.subList(0, child2Customers.size() / 2));

        // Remove the sublist of customers in child2 from child1's vehicles, and vice versa.
        Util.removeCustomers(child1Vehicles, child2CustomersSliced);
        Util.removeCustomers(child2Vehicles, child1CustomersSliced);

        child1Vehicle.setEndDepot(Util.getBestEndDepot(problemDTO.getDepots(), child1Vehicle));
        child2Vehicle.setEndDepot(Util.getBestEndDepot(problemDTO.getDepots(), child2Vehicle));

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
        children.add(new DNA(child1Vehicles));
        children.add(new DNA(child2Vehicles));

        return children;
    }

    private int distributeCustomersOnVehicles(ArrayList<Vehicle> vehicles, ArrayList<Customer> customersToDistribute) {
        Vehicle minVehicle = null;
        int bestPosition = 0;
        double minDurationIncrease;

        for (Customer emptyCustomer : customersToDistribute) {
            minDurationIncrease = Double.MAX_VALUE;

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
                }
            }

            if (minVehicle != null) {
                minVehicle.getCustomers().add(bestPosition, emptyCustomer);
                minVehicle.setEndDepot(Util.getBestEndDepot(problemDTO.getDepots(), minVehicle));
            } else {
                Depot startDepot = Util.getRandomStartDepot(problemDTO.getDepots(), vehicles);
                if (startDepot != null) {
                    Vehicle vehicle = new Vehicle();
                    vehicle.setStartDepot(startDepot);
                    vehicle.setEndDepot(Util.getRandomDepot(problemDTO.getDepots()));
                    vehicles.add(vehicle);

                    int load = Util.calculateLoad(vehicle, emptyCustomer);
                    if (load <= maxVehicleLoad) {
                        vehicle.addCustomer(emptyCustomer);
                    }
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
}
