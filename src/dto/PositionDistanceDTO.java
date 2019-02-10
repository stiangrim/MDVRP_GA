package dto;

/**
 * Created by stgr99 on 08/02/2019.
 */
public class PositionDistanceDTO {

    private final int position;
    private final double distance;

    public PositionDistanceDTO(int position, double distance) {
        this.position = position;
        this.distance = distance;
    }

    public int getPosition() {
        return position;
    }

    public double getDistance() {
        return distance;
    }
}
