package dto;

import model.Customer;

/**
 * Created by stgr99 on 08/02/2019.
 */
public class CustomerDistanceDTO {
    private final Customer customer;
    private final double distance;

    public CustomerDistanceDTO(Customer customer, double distance) {
        this.customer = customer;
        this.distance = distance;
    }

    public Customer getCustomer() {
        return customer;
    }

    public double getDistance() {
        return distance;
    }
}
