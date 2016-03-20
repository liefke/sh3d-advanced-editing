package de.starrunner.sweethome3d;

import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.text.Format;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;

import com.eteks.sweethome3d.model.LengthUnit;

/**
 * The points of an object in a list model.
 * 
 * The object is modified when a point in the list is changed.
 *
 * Copyright (c) 2016 by Tobias Liefke
 *
 * @author Tobias Liefke
 */
public class PointsModel extends AbstractListModel {

  private static final long serialVersionUID = 1L;

  private float[][] points = new float[0][];
  private PointsContainer target;
  private LengthUnit unit;

  /**
   * The unit used for displaing the points.
   *
   * @return The unit.
   */
  public LengthUnit getUnit() {
    return unit;
  }

  /**
   * Sets a new unit for displaying the points.
   *
   * @param unit The new unit to set.
   */
  public void setUnit(LengthUnit unit) {
    this.unit = unit;
    fireContentsChanged(this, 0, points.length - 1);
  }

  /**
   * The currently edited target object of this model.
   *
   * @return The edited object.
   */
  public PointsContainer getTarget() {
    return target;
  }

  /**
   * Creates a shape of the current points.
   *
   * @return the shape of the current points
   */
  public Shape createShape() {
    GeneralPath path = new GeneralPath();
    path.moveTo(points[0][0], points[0][1]);
    for (int i = 1; i < points.length; i++) {
      path.lineTo(points[i][0], points[i][1]);
    }
    if (target.isClosed()) {
      path.closePath();
    }
    return path;
  }

  /**
   * Sets a new selection.
   *
   * @param target The new object to edit.
   */
  public void setTarget(PointsContainer target) {
    this.target = null;
    int oldSize = this.points.length;
    if (oldSize > 0) {
      this.points = new float[0][];
      fireIntervalRemoved(this, 0, oldSize - 1);
    }
    this.target = target;
    this.points = target.getPoints();
    if (points.length > 0) {
      fireIntervalAdded(this, 0, points.length - 1);
    }
  }

  /**
   * The coordinates of the point at the given index.
   *
   * @param index the index of the point
   * @return an array with [x, y] (don't change directly, as this can't be tracked)
   */
  public float[] getPoint(int index) {
    return points[index];
  }

  /**
   * The coordinates of the point at the given index relative to the given fix point.
   * 
   * @param index the index of the point
   * @param fixIndex the index of the fix point
   * @return an array with the relative coordinates [x, y]
   */
  public float[] getRelativePoint(int index, int fixIndex) {
    float[] point = points[index];
    float[] fixPoint = points[fixIndex];
    return new float[] { point[0] - fixPoint[0], point[1] - fixPoint[1] };
  }

  /**
   * Sets the coordinates of the point at the given index relative to the given fix point.
   * 
   * @param index the index of the point
   * @param fixIndex the index of the fix point
   * @param point an array with the relative coordinates [x, y]
   */
  public void setRelativePoint(int index, int fixIndex, float[] point) {
    float[] fixPoint = points[fixIndex];
    setPoint(index, fixPoint[0] + point[0], fixPoint[1] + point[1]);
  }

  /**
   * Moves all points.
   * 
   * @param dx the x distance to move
   * @param dy the y distance to move
   */
  public void move(float dx, float dy) {
    for (float[] point : points) {
      point[0] += dx;
      point[1] += dy;
    }
    target.setPoints(points);
    fireContentsChanged(this, 0, points.length - 1);
  }

  /**
   * Sets the coordinates of the point at the given index.
   *
   * @param index the index of the point
   * @param x the x coordinate of the point
   * @param y the x coordinate of the point
   */
  public void setPoint(int index, float x, float y) {
    points[index][0] = x;
    points[index][1] = y;
    target.setPoints(points);
    fireContentsChanged(this, index, index);
  }

  /**
   * Sets the x coordinate of the point at the given index.
   *
   * @param index the index of the point
   * @param x the x coordinate of the point
   */
  public void setX(int index, float x) {
    points[index][0] = x;
    target.setPoints(points);
    fireContentsChanged(this, index, index);
  }

  /**
   * Sets the y coordinate of the point at the given index.
   *
   * @param index the index of the point
   * @param y the y coordinate of the point
   */
  public void setY(int index, float y) {
    points[index][1] = y;
    target.setPoints(points);
    fireContentsChanged(this, index, index);
  }

  /**
   * Adds a point at the given index.
   *
   * @param index the index of the new point
   * @param x the x coordinate of the point
   * @param y the y coordinate of the point
   */
  public void addPoint(int index, float x, float y) {
    target.addPoint(index, x, y);
    this.points = target.getPoints();
    fireIntervalAdded(this, index, index);
  }

  /**
   * Removes a point from the given index.
   *
   * @param index the index of the point
   */
  public void removePoint(int index) {
    target.removePoint(index);
    this.points = target.getPoints();
    fireIntervalRemoved(this, index, index);
  }

  /**
   * Moves a point from one index to another.
   *
   * @param index the index of the point
   * @param newIndex the new index of that point
   */
  public void movePoint(int index, int newIndex) {
    if (index != newIndex) {
      float[] point = points[index];
      if (index < newIndex) {
        System.arraycopy(points, index + 1, points, index, newIndex - index);
      } else {
        System.arraycopy(points, newIndex, points, newIndex + 1, index - newIndex);
      }
      points[newIndex] = point;
      target.setPoints(points);
      fireContentsChanged(this, index, newIndex);
    }
  }

  /**
   * @see ListModel#getSize()
   */
  @Override
  public int getSize() {
    return points.length;
  }

  /**
   * @see ListModel#getElementAt(int)
   */
  @Override
  public Object getElementAt(int index) {
    float[] point = points[index];
    Format format = unit.getFormat();
    return format.format(point[0]) + "; " + format.format(point[1]);
  }

  /**
   * Calculates the length of a line between the given point.
   *
   * @param startIndex the index of the starting point
   * @param endIndex the index of the end point
   * @return the length of the line
   */
  public float getLength(int startIndex, int endIndex) {
    return distance(points[startIndex], points[endIndex]);
  }

  /**
   * Calculates the angle of the two given lines from the start point.
   * 
   * If the length of the second line is 0, the next different point is used.
   *
   * @param startIndex the index of the starting point (where the angle is measured)
   * @param endIndex the index of the end point of the first line
   * @param oppositeIndex the index of the end point of the second line
   * @return the angle between the two lines (in degrees), 
   *         0 if the given point is the only one 
   *         or, if there is only one line (only two points), the angle above ground
   */
  public float getVectorAngle(int startIndex, int endIndex, int oppositeIndex) {
    if (points.length <= 1) {
      return 0;
    }
    float[] startPoint = points[startIndex];

    float[] endPoint = points[endIndex];
    float endLength = distance(endPoint, startPoint);
    if (endLength == 0) {
      // We have no real "angle"
      return 0;
    }

    // Normalize end vector
    float endVectorX = (endPoint[0] - startPoint[0]) / endLength;
    float endVectorY = (endPoint[1] - startPoint[1]) / endLength;

    // Find start vector
    float[] oppositePoint;
    if (points.length == 2) {
      // Use a vector coming straight from the left to calculate the angle above ground
      oppositePoint = new float[] { startPoint[0] - 100, startPoint[1] };
    } else {
      oppositeIndex = getDistantPoint(startIndex, oppositeIndex);
      // Just to be sure, normally it can't happen as we already previously tested for an existing angle
      if (oppositeIndex == startIndex) {
        return 0;
      }
      oppositePoint = points[oppositeIndex];
    }

    // Normalize start vector
    float startLength = distance(startPoint, oppositePoint);
    float startVectorX = (startPoint[0] - oppositePoint[0]) / startLength;
    float startVectorY = (startPoint[1] - oppositePoint[1]) / startLength;

    // Calculate angle
    return (float) (180 - Math.toDegrees(Math.atan2(startVectorX * endVectorY - startVectorY * endVectorX, startVectorX
        * endVectorX + startVectorY * endVectorY)));
  }

  /**
   * Sets the vector between the given points, after transfroming it using the vector to the opposite point.
   *
   * @param startIndex the index of the start point of the vector
   * @param endIndex the index of the end point of the vector
   * @param oppositeIndex the index of the end point of the opposite vector
   * @param length the length of the line to the end point
   * @param angle the angle from the opposite line to the end point (in degrees)
   * 
   * @see #getVectorAngle(int, int, int)
   * @see #getLength(int, int)
   */
  public void setVector(int startIndex, int endIndex, int oppositeIndex, float length, float angle) {
    if (points.length <= 1) {
      // Ignore for single point shapes
      return;
    }
    float[] startPoint = points[startIndex];
    float[] endPoint = points[endIndex];
    if (length == 0) {
      // Just set to the current point
      endPoint[0] = startPoint[0];
      endPoint[1] = startPoint[1];
    } else {
      // Calculate the new next point
      // Find start vector
      float[] oppositePoint;
      if (points.length == 2) {
        // Use a vector coming straight from the left to use the angle above ground
        oppositePoint = new float[] { startPoint[0] - 100, startPoint[1] };
      } else {
        int previousIndex = getDistantPoint(startIndex, oppositeIndex);
        if (previousIndex == startIndex) {
          // No real angle exists, just ignore the call
          return;
        }
        oppositePoint = points[previousIndex];
      }

      double theta = Math.toRadians(270 - angle)
          - Math.atan2(startPoint[0] - oppositePoint[0], startPoint[1] - oppositePoint[1]);
      endPoint[0] = (float) (startPoint[0] + length * Math.cos(theta));
      endPoint[1] = (float) (startPoint[1] + length * Math.sin(theta));
    }
    target.setPoints(points);
    fireContentsChanged(this, endIndex, endIndex);
  }

  /**
   * Calculates the distances between two points
   *
   * @param p1 the first point
   * @param p2 the second point
   * @return the distance
   */
  private float distance(float[] p1, float[] p2) {
    return (float) Point2D.distance(p1[0], p1[1], p2[0], p2[1]);
  }

  /**
   * Finds the point that has a distance greater zero from the given point.
   *
   * @param index the index of the point
   * @param targetIndex the index of the target point
   * @return the index of the distant point or {@code index} if none was found
   */
  private int getDistantPoint(int index, int targetIndex) {
    int distance = (targetIndex - index + points.length) % points.length < points.length / 2 ? 1 : (points.length - 1);
    float[] point = points[index];
    while (targetIndex != index) {
      if ((distance(point, points[targetIndex])) > 0) {
        return targetIndex;
      }
      targetIndex = (targetIndex + distance) % points.length;
    }
    return targetIndex;
  }

}