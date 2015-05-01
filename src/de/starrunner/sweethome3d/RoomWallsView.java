package de.starrunner.sweethome3d;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.*;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.View;

import de.starrunner.components.event.AbstractObjectEdit;
import de.starrunner.components.event.ChangeState;
import de.starrunner.util.strings.Mnemonics;

/**
 * The dialog for creating the walls around a room.
 *
 * Copyright (c) 2010 by Tobias Liefke
 *
 * @author Tobias Liefke
 */
public class RoomWallsView extends ImmediateEditDialogView {
  private static final long serialVersionUID = -5513320579987973880L;

  private CreateWallsEdit edit;

  private ChangeState changeState = new ChangeState();

  // Components
  private DefaultListModel walls = new DefaultListModel();
  private JList wallsList = new JList(walls);
  private JCheckBox createWallBox = new JCheckBox();
  private JCheckBox useDefaultsBox = new JCheckBox();

  /**
   * Creates a new instance of RoomWallsView.
   * 
   * @param home the application
   * @param preferences the current configuration
   * @param undoSupport used for undo support of the current action
   */
  public RoomWallsView(Home home, UserPreferences preferences, UndoableEditSupport undoSupport) {
    super("Create walls", home, preferences, undoSupport);
    initComponents();
  }

  /**
   * Create and add the components.
   */
  private void initComponents() {

    // Walls list
    this.add(Mnemonics.configure(new JLabel("&Walls:"), wallsList), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
        GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    wallsList.getSelectionModel().addListSelectionListener(changeState.wrap(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
          showSelectedWall();
        }
      }
    }));
    setInitialFocusedComponent(wallsList);
    this.add(new JScrollPane(wallsList), new GridBagConstraints(0, 1, 1, 2, 1.0, 1.0, GridBagConstraints.LINE_START,
        GridBagConstraints.BOTH, new Insets(5, 0, 0, 5), 0, 0));

    // Wall settings
    this.add(Mnemonics.configure(createWallBox, "Create this wall"), new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
        GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

    this.add(Mnemonics.configure(useDefaultsBox, "Use defaults"), new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
        GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
  }

  /**
   * @see DialogView#displayView(View)
   */
  @Override
  public void displayView(View parentView) {
    Window parentWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
    JRootPane parentComponent = parentWindow instanceof JFrame ? ((JFrame) parentWindow).getRootPane() : null;

    List<Selectable> selectedItems = home.getSelectedItems();
    List<Room> selectedRooms = Home.getRoomsSubList(selectedItems);

    // Only allow one room
    if (selectedRooms.size() != 1) {
      JOptionPane.showMessageDialog(parentComponent, "You need to select exactly one room for this action.");
      return;
    }

    // Only allow a room with one polygon
    Room room = selectedRooms.get(0);
    if (!room.isSingular()) {
      JOptionPane.showMessageDialog(parentComponent,
        "You need to select a room with exactly one polygon for this action.");
      return;
    }

    List<float[]> points = new ArrayList<float[]>(Arrays.asList(room.getPoints()));
    // If points are not clockwise reverse their order
    if (!room.isClockwise()) {
      Collections.reverse(points);
    }
    // Remove equal points 
    for (int i = 0; i < points.size(); i++) {
      float[] point = points.get(i);
      float[] nextPoint = points.get((i + 1) % points.size());
      if (point[0] == nextPoint[0] && point[1] == nextPoint[1]) {
        points.remove(i);
        i--;
      }
    }

    // Only allow rooms containing space (to identify the border)
    if (points.size() < 3) {
      JOptionPane.showMessageDialog(parentComponent,
        "You need to select a room with at least three points for this action.");
      return;
    }

    changeState.start();

    // Create the new walls with their defaults
    edit = new CreateWallsEdit(home, room);
    walls.addElement(new WallDefaults(preferences.getNewWallThickness()));

    for (int i = 0; i < points.size(); i++) {
      float[] point = points.get(i);
      float[] nextPoint = points.get((i + 1) % points.size());
      walls.addElement(new RoomWall(point, nextPoint, i));
    }

    changeState.stop();
    showDialog(edit);
  }

  /**
   * Shows the properties of the selected wall.
   */
  protected void showSelectedWall() {
    // TODO Auto-generated method stub
  }

  private void createWallsAroundRoom(Room room) {
    //    float halfWallThickness = preferences.getNewWallThickness() / 2;
    //    float[][] largerRoomPoints = new float[roomPoints.length][];
    //    for (int i = 0; i < roomPoints.length; i++) {
    //      float[] point = roomPoints[i];
    //      float[] previousPoint = roomPoints[(i + roomPoints.length - 1) % roomPoints.length];
    //      float[] nextPoint = roomPoints[(i + 1) % roomPoints.length];
    //
    //      // Compute the angle of the line with a direction orthogonal to line (previousPoint, point)
    //      double previousAngle = Math.atan2(point[0] - previousPoint[0], previousPoint[1] - point[1]);
    //      // Compute the points of the line joining previous and current point
    //      // at a distance equal to the half wall thickness 
    //      float deltaX = (float) (Math.cos(previousAngle) * halfWallThickness);
    //      float deltaY = (float) (Math.sin(previousAngle) * halfWallThickness);
    //      float[] point1 = { previousPoint[0] - deltaX, previousPoint[1] - deltaY };
    //      float[] point2 = { point[0] - deltaX, point[1] - deltaY };
    //
    //      // Compute the angle of the line with a direction orthogonal to line (point, nextPoint)
    //      double nextAngle = Math.atan2(nextPoint[0] - point[0], point[1] - nextPoint[1]);
    //      // Compute the points of the line joining current and next point
    //      // at a distance equal to the half wall thickness 
    //      deltaX = (float) (Math.cos(nextAngle) * halfWallThickness);
    //      deltaY = (float) (Math.sin(nextAngle) * halfWallThickness);
    //      float[] point3 = { point[0] - deltaX, point[1] - deltaY };
    //      float[] point4 = { nextPoint[0] - deltaX, nextPoint[1] - deltaY };
    //
    //      largerRoomPoints[i] = computeIntersection(point1, point2, point3, point4);
    //    }
    //
    //    // Create walls joining points of largerRoomPoints
    //    List<Wall> newWalls = new ArrayList<Wall>();
    //    Wall lastWall = null;
    //    for (int i = 0; i < largerRoomPoints.length; i++) {
    //      float[] point = largerRoomPoints[i];
    //      float[] nextPoint = largerRoomPoints[(i + 1) % roomPoints.length];
    //      Wall wall = createWall(point[0], point[1], nextPoint[0], nextPoint[1], null, lastWall);
    //      newWalls.add(wall);
    //      lastWall = wall;
    //    }
    //    joinNewWallEndToWall(lastWall, newWalls.get(0), null);
  }

  /** 
   * Returns the intersection point between the lines defined by the points
   * (<code>point1</code>, <code>point2</code>) and (<code>point3</code>, <code>pont4</code>).
   */
  private float[] computeIntersection(float[] point1, float[] point2, float[] point3, float[] point4) {
    float x = point2[0];
    float y = point2[1];
    float alpha1 = (point2[1] - point1[1]) / (point2[0] - point1[0]);
    float beta1 = point2[1] - alpha1 * point2[0];
    float alpha2 = (point4[1] - point3[1]) / (point4[0] - point3[0]);
    float beta2 = point4[1] - alpha2 * point4[0];

    if (alpha1 != alpha2) {
      // If first line is vertical
      if (Math.abs(alpha1) > 1E5) {
        if (Math.abs(alpha2) < 1E5) {
          x = point1[0];
          y = alpha2 * x + beta2;
        }
        // If second line is vertical
      } else if (Math.abs(alpha2) > 1E5) {
        if (Math.abs(alpha1) < 1E5) {
          x = point3[0];
          y = alpha1 * x + beta1;
        }
      } else {
        x = (beta2 - beta1) / (alpha1 - alpha2);
        y = alpha1 * x + beta1;
      }
    }
    return new float[] { x, y };
  }

  /**
   * Returns a new wall instance between (<code>xStart</code>,
   * <code>yStart</code>) and (<code>xEnd</code>, <code>yEnd</code>)
   * end points. The new wall is added to home and its start point is joined 
   * to the start of <code>wallStartAtStart</code> or 
   * the end of <code>wallEndAtStart</code>.
   */
  protected Wall createWall(float xStart, float yStart, float xEnd, float yEnd, Wall wallStartAtStart,
      Wall wallEndAtStart) {
    // Create a new wall
    Wall newWall = new Wall(xStart, yStart, xEnd, yEnd, preferences.getNewWallThickness(),
        preferences.getNewWallHeight());
    home.addWall(newWall);
    if (wallStartAtStart != null) {
      newWall.setWallAtStart(wallStartAtStart);
      wallStartAtStart.setWallAtStart(newWall);
    } else if (wallEndAtStart != null) {
      newWall.setWallAtStart(wallEndAtStart);
      wallEndAtStart.setWallAtEnd(newWall);
    }
    return newWall;
  }

  /**
   * Joins the end point of <code>wall</code> to the start of
   * <code>wallStartAtEnd</code> or the end of <code>wallEndAtEnd</code>.
   */
  private void joinNewWallEndToWall(Wall wall, Wall wallStartAtEnd, Wall wallEndAtEnd) {
    if (wallStartAtEnd != null) {
      wall.setWallAtEnd(wallStartAtEnd);
      wallStartAtEnd.setWallAtStart(wall);
      // Make wall end at the exact same position as wallAtEnd start point
      wall.setXEnd(wallStartAtEnd.getXStart());
      wall.setYEnd(wallStartAtEnd.getYStart());
    } else if (wallEndAtEnd != null) {
      wall.setWallAtEnd(wallEndAtEnd);
      wallEndAtEnd.setWallAtEnd(wall);
      // Make wall end at the exact same position as wallAtEnd end point
      wall.setXEnd(wallEndAtEnd.getXEnd());
      wall.setYEnd(wallEndAtEnd.getYEnd());
    }
  }

  private static class WallSettings {
    // The thickness of the wall (in centimeters)
    protected float thickness;
    // The count of centimeters the wall overlaps the room
    protected float overlap;
  }

  /**
   * Saves the values of an edited wall.
   */
  private static final class RoomWall extends WallSettings {
    // The index of the wall in the room
    private final int index;

    private final float[] startPoint;
    private final float[] endPoint;

    // The created wall
    private Wall wall;

    // Use the default values
    private boolean useDefaults = true;

    /**
     * Creates a new instance of RoomWall.
     *
     * @param wall the wall that is managed by this settings
     */
    private RoomWall(float[] startPoint, float[] endPoint, int index) {
      this.startPoint = startPoint;
      this.endPoint = endPoint;
      this.index = index;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
      return index + ". wall";
    }

  }

  /**
   * Saves the default values.
   */
  private static final class WallDefaults extends WallSettings {

    private WallDefaults(float thickness) {
      this.thickness = thickness;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
      return "Defaults";
    }

  }

  /**
   * Creates the walls (resp. removes them during undo).
   */
  private static final class CreateWallsEdit extends AbstractObjectEdit<Home> {
    private static final long serialVersionUID = 6428805832370301978L;

    private final Room room;
    private final List<Wall> walls = new ArrayList<Wall>();

    /**
     * Creates a new instance of CreateWallsEdit.
     *
     * @param home the home to add the walls to
     * @param room the room that the walls surround (just used for selection during undo)
     */
    public CreateWallsEdit(Home home, Room room) {
      super(home);
      this.room = room;
    }

    @Override
    public String getPresentationName() {
      return "Create walls around room";
    }

    /**
     * @see AbstractObjectEdit#doAction()
     */
    @Override
    public void doAction() {
      if (!walls.isEmpty()) {
        Wall previosWall = walls.get(walls.size() - 1);
        for (Wall wall : walls) {
          // Add and connect walls
          // During undo the walls are disconnected -> thus we don't expect them to be connected
          target.addWall(wall);
          if (previosWall != wall) {
            wall.setWallAtStart(previosWall);
            previosWall.setWallAtEnd(wall);
          }
        }
      }
      target.setSelectedItems(walls);
    }

    /**
     * @see AbstractObjectEdit#undoAction()
     */
    @Override
    public void undoAction() {
      for (Wall wall : walls) {
        target.deleteWall(wall);
      }
      target.setSelectedItems(Collections.singletonList(room));
    }

  }

  @Override
  protected void apply() {
    // We never call #applyLazy()
  }

}
