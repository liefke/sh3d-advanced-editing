package de.starrunner.sweethome3d;

import com.eteks.sweethome3d.model.Room;

/**
 * Wrapper for a {@link Room} to edit its points.
 *
 * Copyright (c) 2016 by Tobias Liefke
 *
 * @author Tobias Liefke
 */
public class RoomPoints implements PointsContainer {

  private final Room room;

  /**
   * Creates a new instance of RoomPoints.
   *
   * @param room the edited room
   */
  public RoomPoints(Room room) {
    this.room = room;
  }

  /**
   * The room of this RoomPoints.
   *
   * @return The room.
   */
  public Room getRoom() {
    return room;
  }

  @Override
  public float[][] getPoints() {
    return room.getPoints();
  }

  @Override
  public void setPoints(float[][] newPoints) {
    room.setPoints(newPoints);
  }

  @Override
  public void addPoint(int index, float x, float y) {
    room.addPoint(x, y, index);
  }

  @Override
  public void removePoint(int index) {
    room.removePoint(index);
  }

  @Override
  public boolean isClosed() {
    return true;
  }

  @Override
  public boolean isFilled() {
    return true;
  }

}
