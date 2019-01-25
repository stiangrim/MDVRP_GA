package model;

/**
 * Created by stgr99 on 22/01/2019.
 */
public class Customer implements MapObject {
    private int id;
    private Integer x;
    private Integer y;
    private int adjustedX;
    private int adjustedY;
    private int serviceDuration;
    private int demand;
    private boolean visited;

    public Customer(int id, int x, int y, int serviceDuration, int demand) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.serviceDuration = serviceDuration;
        this.demand = demand;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public int getServiceDuration() {
        return serviceDuration;
    }

    public void setServiceDuration(int serviceDuration) {
        this.serviceDuration = serviceDuration;
    }

    public int getDemand() {
        return demand;
    }

    public void setDemand(int demand) {
        this.demand = demand;
    }

    public int getAdjustedX() {
        return adjustedX;
    }

    public void setAdjustedX(int adjustedX) {
        this.adjustedX = adjustedX;
    }

    public int getAdjustedY() {
        return adjustedY;
    }

    public void setAdjustedY(int adjustedY) {
        this.adjustedY = adjustedY;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }
}
