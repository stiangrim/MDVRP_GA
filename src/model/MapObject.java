package model;

/**
 * Created by stgr99 on 24/01/2019.
 */
public interface MapObject {
    int id = 0;
    Integer x = 0;
    Integer y = 0;
    int adjustedX = 0;
    int adjustedY = 0;

    public int getId();

    public void setId(int id);

    public Integer getX();

    public void setX(Integer x);

    public Integer getY();

    public void setY(Integer y);

    public int getAdjustedX();

    public void setAdjustedX(int adjustedX);

    public int getAdjustedY();

    public void setAdjustedY(int adjustedY);
}
