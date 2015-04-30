package de.starrunner.sweethome3d;

import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

import com.eteks.sweethome3d.model.*;

import de.starrunner.components.event.AbstractObjectEdit;
import de.starrunner.sweethome3d.TransformEdit.ObjectState;

/**
 * Applies a {@link AffineTransform transformation} to the list of selected objects.
 *
 * Copyright (c) 2010 by Tobias Liefke
 *
 * @author Tobias Liefke
 */
public class TransformEdit extends AbstractObjectEdit<List<ObjectState<? extends Selectable>>> {
  private static final long serialVersionUID = -5514573790001584955L;

  private final Home home;

  private AffineTransform transformation;

  /**
   * Creates a new instance of the resize edit for undo / redo .
   *
   * @param home the home
   */
  public TransformEdit(Home home) {
    super(new ArrayList<ObjectState<? extends Selectable>>());
    // Save the current state of the selected items
    this.home = home;
    for (Selectable item : home.getSelectedItems()) {
      if (item instanceof Wall) {
        target.add(new WallState((Wall) item));
      } else if (item instanceof Room) {
        target.add(new RoomState((Room) item));
      } else if (item instanceof Label) {
        target.add(new LabelState((Label) item));
      } else if (item instanceof DimensionLine) {
        target.add(new DimensionState((DimensionLine) item));
      } else if (item instanceof HomePieceOfFurniture) {
        target.add(new FurnitureState((HomePieceOfFurniture) item));
      } else if (item instanceof ObserverCamera) {
        target.add(new CameraState((ObserverCamera) item));
      } else if (item != null) {
        System.err.println(getClass() + " - Unknown item type: " + item.getClass());
      }
    }
  }

  @Override
  public void doAction() {
    transform();
    selectItems();
  }

  @Override
  public void undoAction() {
    for (ObjectState<? extends Selectable> state : target) {
      state.reset();
    }
    selectItems();
  }

  private void selectItems() {
    List<Selectable> items = new ArrayList<Selectable>(target.size());
    for (ObjectState<?> state : target) {
      items.add(state.object);
    }
    home.setSelectedItems(items);
  }

  /**
   * Applies the transformation to all associated objects using the previous states.
   * 
   * @param transformation the affine transformation
   */
  public void transform(AffineTransform transformation) {
    this.transformation = transformation;
    transform();
  }

  private void transform() {
    for (ObjectState<?> state : target) {
      state.transform(transformation);
    }
  }

  /**
   * Resolves the bounds of all associated object in cm.
   *
   * @return the bounds of the objects, or an empty rectangle if no points were found
   */
  public Rectangle2D.Float getBounds() {
    float left = Float.MAX_VALUE;
    float top = Float.MAX_VALUE;
    float bottom = -Float.MAX_VALUE;
    float right = -Float.MAX_VALUE;
    for (ObjectState<?> state : target) {
      for (float[] point : state.getPoints()) {
        if (point[0] < left) {
          left = point[0];
        }
        if (point[0] > right) {
          right = point[0];
        }
        if (point[1] < top) {
          top = point[1];
        }
        if (point[1] > bottom) {
          bottom = point[1];
        }
      }
    }
    if (left > right || top > bottom) {
      return new Rectangle2D.Float(0, 0, 0, 0);
    }
    return new Rectangle2D.Float(left, top, right - left, bottom - top);
  }

  /**
   * Represents the state of an item from the plan for undo / redo
   * 
   * @param <T> the type of the associated object
   */
  public abstract static class ObjectState<T extends Selectable> {
    protected final T object;

    /**
     * Creates a new instance of ObjectState.
     *
     * @param object the associated object
     */
    public ObjectState(T object) {
      this.object = object;
    }

    /**
     * The current points of the associated object.
     *
     * @return the points for calculating the bounds of this object
     */
    public float[][] getPoints() {
      return object.getPoints();
    }

    /**
     * Resets to the state found during creation.
     */
    public abstract void reset();

    /**
     * Applies the transformation to the associated object using the coordinates found during creation.
     * 
     * @param transformation the associated transformation
     */
    public abstract void transform(AffineTransform transformation);

  }

  /**
   * Saves the state of a wall.
   */
  private static final class WallState extends ObjectState<Wall> {
    private Point2D startPoint;
    private Point2D endPoint;

    private Integer leftSideColor;
    private HomeTexture leftSideTexture;

    private Integer rightSideColor;
    private HomeTexture rightSideTexture;

    /**
     * Creates a new wall state.
     *
     * @param wall the wall
     */
    public WallState(Wall wall) {
      super(wall);
      // Save the start and end point
      startPoint = new Point2D.Float(wall.getXStart(), wall.getYStart());
      endPoint = new Point2D.Float(wall.getXEnd(), wall.getYEnd());

      // Save the styles for mirroring
      leftSideColor = wall.getLeftSideColor();
      leftSideTexture = wall.getLeftSideTexture();
      rightSideColor = wall.getRightSideColor();
      rightSideTexture = wall.getRightSideTexture();
    }

    @Override
    public void reset() {
      setStartPoint(startPoint);
      setEndPoint(endPoint);
      object.setLeftSideColor(leftSideColor);
      object.setLeftSideTexture(leftSideTexture);
      object.setRightSideColor(rightSideColor);
      object.setRightSideTexture(rightSideTexture);
    }

    @Override
    public void transform(AffineTransform transformation) {
      setStartPoint(transformation.transform(startPoint, null));
      setEndPoint(transformation.transform(endPoint, null));

      if (transformation.getScaleX() < 0 != transformation.getScaleY() < 0) {
        // If this is a mirroring transformation exhange the left and right style
        object.setLeftSideColor(rightSideColor);
        object.setLeftSideTexture(rightSideTexture);
        object.setRightSideColor(leftSideColor);
        object.setRightSideTexture(leftSideTexture);
      } else {
        // Undo a possible style exchange
        object.setLeftSideColor(leftSideColor);
        object.setLeftSideTexture(leftSideTexture);
        object.setRightSideColor(rightSideColor);
        object.setRightSideTexture(rightSideTexture);
      }
    }

    private void setStartPoint(Point2D point) {
      float x = (float) point.getX();
      float y = (float) point.getY();
      object.setXStart(x);
      object.setYStart(y);
      Wall start = object.getWallAtStart();
      if (start != null) {
        if (start.getWallAtStart() == object) {
          start.setXStart(x);
          start.setYStart(y);
        } else if (start.getWallAtEnd() == object) {
          start.setXEnd(x);
          start.setYEnd(y);
        }
      }
    }

    private void setEndPoint(Point2D point) {
      float x = (float) point.getX();
      float y = (float) point.getY();
      object.setXEnd(x);
      object.setYEnd(y);
      Wall end = object.getWallAtEnd();
      if (end != null) {
        if (end.getWallAtStart() == object) {
          end.setXStart(x);
          end.setYStart(y);
        } else if (end.getWallAtEnd() == object) {
          end.setXEnd(x);
          end.setYEnd(y);
        }
      }
    }

    /**
     * Return the start and end point of the wall (not the corners).
     */
    @Override
    public float[][] getPoints() {
      return new float[][] { { object.getXStart(), object.getYStart() }, { object.getXEnd(), object.getYEnd() } };
    }

  }

  /**
   * Saves the state of a room.
   */
  private static final class RoomState extends ObjectState<Room> {
    private final float[][] points;

    /**
     * Creates a new room state.
     *
     * @param room the associated room
     */
    public RoomState(Room room) {
      super(room);
      // Save the points (is already a copy)
      points = room.getPoints();
    }

    @Override
    public void reset() {
      object.setPoints(points);
    }

    @Override
    public void transform(AffineTransform transformation) {
      // Create a new array for modification
      float[][] points = new float[this.points.length][];
      for (int i = 0; i < points.length; i++) {
        float[] newPoint = new float[2];
        transformation.transform(this.points[i], 0, newPoint, 0, 1);
        points[i] = newPoint;
      }
      object.setPoints(points);
    }

  }

  /**
   * Saves the state of a label.
   */
  private static final class LabelState extends ObjectState<Label> {
    private final float x;
    private final float y;

    /**
     * Creates a new label state.
     *
     * @param label the associated label
     */
    public LabelState(Label label) {
      super(label);
      // Save the point
      x = label.getX();
      y = label.getY();
    }

    @Override
    public void reset() {
      object.setX(x);
      object.setY(y);
    }

    @Override
    public void transform(AffineTransform transformation) {
      Point2D.Float point = new Point2D.Float(x, y);
      transformation.transform(point, point);
      object.setX(point.x);
      object.setY(point.y);
    }

  }

  /**
   * Saves the state of a dimension line.
   */
  private static final class DimensionState extends ObjectState<DimensionLine> {
    private final Point2D.Float start;
    private final Point2D.Float end;

    /**
     * Creates a new dimension line state.
     * 
     * @param line the associated dimension line
     */
    public DimensionState(DimensionLine line) {
      super(line);
      // Save the current state
      start = new Point2D.Float(line.getXStart(), line.getYStart());
      end = new Point2D.Float(line.getXEnd(), line.getYEnd());
    }

    @Override
    public void reset() {
      object.setXStart(start.x);
      object.setYStart(start.y);
      object.setXEnd(end.x);
      object.setYEnd(end.y);
    }

    @Override
    public void transform(AffineTransform transformation) {
      Point2D.Float transform = new Point2D.Float();
      transformation.transform(start, transform);
      object.setXStart(transform.x);
      object.setYStart(transform.y);
      transformation.transform(end, transform);
      object.setXEnd(transform.x);
      object.setYEnd(transform.y);
    }

    /**
     * Return the start and end point of the line (not the bounding box).
     */
    @Override
    public float[][] getPoints() {
      return new float[][] { { object.getXStart(), object.getYStart() }, { object.getXEnd(), object.getYEnd() } };
    }

  }

  /**
   * Saves the state of a furniture.
   */
  private static final class FurnitureState extends ObjectState<HomePieceOfFurniture> {
    private final Point2D.Float center;
    private final float angle;
    private final float width;
    private final float depth;

    private final Point2D.Float topRight;
    private final Point2D.Float bottomRight;

    /**
     * Creates a new furniture state.
     *
     * @param furniture the associated furniture
     */
    public FurnitureState(HomePieceOfFurniture furniture) {
      super(furniture);
      // Save the current state
      center = new Point2D.Float(furniture.getX(), furniture.getY());
      angle = furniture.getAngle();
      width = furniture.getWidth();
      depth = furniture.getDepth();

      // Calculate the corners for later transformation
      float sin = (float) Math.sin(angle);
      float cos = (float) Math.cos(angle);
      float halfWidth = width / 2;
      float halfHeight = depth / 2;
      topRight = new Point2D.Float(cos * halfWidth - sin * halfHeight + center.x, sin * halfWidth + cos * halfHeight
          + center.y);
      bottomRight = new Point2D.Float(cos * halfWidth + sin * halfHeight + center.x, sin * halfWidth - cos * halfHeight
          + center.y);
    }

    @Override
    public void reset() {
      object.setAngle(angle);
      object.setX(center.x);
      object.setY(center.y);
      object.setWidth(width);
      object.setDepth(depth);
    }

    @Override
    public void transform(AffineTransform transformation) {
      Point2D.Float newCenter = new Point2D.Float();
      transformation.transform(center, newCenter);
      object.setX(newCenter.x);
      object.setY(newCenter.y);

      // Calculate the new width, height and angle
      Point2D.Float newTopRight = new Point2D.Float();
      transformation.transform(topRight, newTopRight);
      Point2D.Float newBottomRight = new Point2D.Float();
      transformation.transform(bottomRight, newBottomRight);
      object.setDepth((float) newTopRight.distance(newBottomRight));
      Point2D.Float newRight = new Point2D.Float((newTopRight.x + newBottomRight.x) / 2,
          (newTopRight.y + newBottomRight.y) / 2);
      object.setWidth((float) newCenter.distance(newRight) * 2);
      object.setAngle((float) Math.atan2(newRight.y - newCenter.y, newRight.x - newCenter.x));
    }
  }

  /**
   * Saves the state of the camera.
   */
  private static final class CameraState extends ObjectState<ObserverCamera> {
    private final Point2D.Float point;

    /**
     * Creates a new camera state.
     *
     * @param camera the associated camera
     */
    public CameraState(ObserverCamera camera) {
      super(camera);
      // Save the current state
      point = new Point2D.Float(camera.getX(), camera.getY());
    }

    @Override
    public void reset() {
      object.setX(point.x);
      object.setY(point.y);
    }

    @Override
    public void transform(AffineTransform transformation) {
      Point2D.Float result = new Point2D.Float();
      transformation.transform(point, result);
      object.setX(result.x);
      object.setY(result.y);
    }

    /**
     * Return the position of the camera (not the bounding box).
     */
    @Override
    public float[][] getPoints() {
      return new float[][] { { object.getX(), object.getY() } };
    }
  }

}
