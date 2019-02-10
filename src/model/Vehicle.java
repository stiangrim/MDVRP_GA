package model;

import java.util.ArrayList;

/**
 * Created by stgr99 on 24/01/2019.
 */
public class Vehicle {

    private Depot startDepot;
    private Depot endDepot;
    private ArrayList<Customer> customers;

    private int vehicleNumber;
    private int load = 0;
    private double duration = 0.0;


    public Vehicle() {
        customers = new ArrayList<>();
    }

    public Vehicle(Depot startDepot, Depot endDepot, int vehicleNumber) {
        this.startDepot = startDepot;
        this.endDepot = endDepot;
        this.vehicleNumber = vehicleNumber;
        this.customers = new ArrayList<>();
    }

    public Vehicle(Depot startDepot, Depot endDepot, ArrayList<Customer> customers, int vehicleNumber) {
        this.startDepot = startDepot;
        this.endDepot = endDepot;
        this.customers = customers;
        this.vehicleNumber = vehicleNumber;
    }

    public Depot getStartDepot() {
        return startDepot;
    }

    public void setStartDepot(Depot startDepot) {
        this.startDepot = startDepot;
    }

    public Depot getEndDepot() {
        return endDepot;
    }

    public void setEndDepot(Depot endDepot) {
        this.endDepot = endDepot;
    }

    public ArrayList<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(ArrayList<Customer> customers) {
        this.customers = customers;
    }

    public void addCustomer(Customer customer) {
        this.customers.add(customer);
    }

    public void addCustomer(Customer customer, int position) {
        this.customers.add(position, customer);
    }

    public int getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(int vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public int getLoad() {
        return load;
    }

    public void setLoad(int load) {
        this.load = load;
    }
}
