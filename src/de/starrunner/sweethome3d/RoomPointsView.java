package de.starrunner.sweethome3d;

import java.awt.*;
import java.awt.event.*;
import java.util.Collections;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.*;

import com.eteks.sweethome3d.model.*;
import com.eteks.sweethome3d.swing.*;
import com.eteks.sweethome3d.swing.NullableSpinner.NullableSpinnerLengthModel;
import com.eteks.sweethome3d.swing.NullableSpinner.NullableSpinnerNumberModel;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.View;

import de.starrunner.components.event.AbstractObjectEdit;
import de.starrunner.components.event.ChangeState;
import de.starrunner.util.strings.Mnemonics;

/**
 * Lets a user edit the points of a room.
 *
 * Copyright (c) 2010 by Tobias Liefke
 *
 * @author Tobias Liefke
 */
public class RoomPointsView extends JPanel implements DialogView {
  private static final long serialVersionUID = -3184198019104925119L;

  private final Home home;
  private final UserPreferences preferences;
  private final UndoableEditSupport undoSupport;

  private ChangeState changeState = new ChangeState();

  private RoomPointsModel pointsModel;
  private JList pointsList;
  private JTabbedPane lineTabs;

  private NullableSpinnerLengthModel pointXModel;
  private NullableSpinnerLengthModel pointYModel;

  private LineTab incomingLineTab;
  private LineTab outgoingLineTab;

  /**
   * Creates a new instance of RoomPointsView.
   * 
   * @param home the application
   * @param preferences the current configuration
   * @param undoSupport used for undo support of the current action
   */
  public RoomPointsView(Home home, UserPreferences preferences, UndoableEditSupport undoSupport) {
    super(new GridBagLayout());
    this.home = home;
    this.preferences = preferences;
    this.undoSupport = undoSupport;
    initComponents();
  }

  /**
   * Create and add the components.
   */
  private void initComponents() {
    String unitName = preferences.getLengthUnit().getName();

    // Create the list
    JLabel pointsLabel = new JLabel(Msg.msg("RoomPointsView.pointsLabel"));
    add(pointsLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));

    pointsModel = new RoomPointsModel();
    pointsList = new JList(pointsModel);
    pointsList.setToolTipText(Msg.msg("RoomPointsView.pointsListTooltip"));
    pointsList.setFixedCellWidth(100);
    pointsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    // Enable moving of points in the list
    MouseInputAdapter listMouseHandler = new MouseInputAdapter() {
      private int fromIndex;

      @Override
      public void mousePressed(MouseEvent e) {
        fromIndex = pointsList.getSelectedIndex();
      }

      @Override
      public void mouseDragged(MouseEvent e) {
        int toIndex = pointsList.locationToIndex(e.getPoint());
        if (toIndex != fromIndex) {
          pointsModel.movePoint(fromIndex, toIndex);
          fromIndex = toIndex;
        }
      }
    };
    pointsList.addMouseListener(listMouseHandler);
    pointsList.addMouseMotionListener(listMouseHandler);
    ActionMap actionMap = pointsList.getActionMap();
    actionMap.put("movePointUp", new MovePointAction(-1));
    actionMap.put("movePointDown", new MovePointAction(1));
    InputMap inputMap = pointsList.getInputMap(WHEN_FOCUSED);
    inputMap.put(KeyStroke.getKeyStroke("alt UP"), "movePointUp");
    inputMap.put(KeyStroke.getKeyStroke("alt DOWN"), "movePointDown");

    add(new JScrollPane(pointsList), new GridBagConstraints(0, 1, 1, 2, 1.0, 1.0, GridBagConstraints.LINE_START,
        GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    Mnemonics.configure(pointsLabel, pointsList);

    // Button for creating a new point 
    JButton newButton = Mnemonics.configure(new JButton(Msg.msg("RoomPointsView.newButton")));
    newButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int index = pointsList.getSelectedIndex();
        if (index < 0) {
          index = pointsModel.getSize();
          if (index < 0) {
            pointsModel.addPoint(0, 0, 0);
            pointsList.setSelectedIndex(0);
            return;
          }
        }
        float[] point = pointsModel.getPoint(index);
        float[] point2 = pointsModel.getPoint((index + 1) % pointsModel.getSize());
        pointsModel.addPoint(index + 1, (point[0] + point2[0]) / 2, (point[1] + point2[1]) / 2);
        pointsList.setSelectedIndex(index + 1);
      }
    });
    add(newButton, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
        GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));

    // The panel for the absolute coordinates of the current point
    final JPanel pointPanel = new JPanel(new GridBagLayout());
    pointPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
      Msg.msg("RoomPointsView.pointPanel")));
    add(pointPanel, new GridBagConstraints(1, 0, 1, 2, 0.0, 0.0, GridBagConstraints.FIRST_LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(0, 5, 0, 0), 0, 0));

    // Checkbox for moving the whole room
    final JCheckBox moveAllBox = Mnemonics.configure(new JCheckBox(Msg.msg("RoomPointsView.moveAllBox")));
    pointPanel.add(moveAllBox, new GridBagConstraints(0, 0, 4, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(5, 0, 0, 5), 0, 0));

    // Absolute x coordinate of the selected point
    JLabel pointXLabel = new JLabel(Msg.msg("RoomPointsView.xLabel", unitName));
    pointPanel.add(pointXLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    pointXModel = new NullableSpinnerLengthModel(preferences, -100000f, 100000f);
    pointXModel.addChangeListener(changeState.wrap(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        Float x = pointXModel.getLength();
        if (x != null) {
          if (moveAllBox.isSelected()) {
            pointsModel.move(x - pointsModel.getPoint(pointsList.getSelectedIndex())[0], 0);
          } else {
            pointsModel.setX(pointsList.getSelectedIndex(), x);
          }
          loadValues(pointXModel);
        }
      }
    }));
    final JSpinner pointXSpinner = new NullableSpinner(pointXModel);
    pointPanel.add(pointXSpinner, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END,
        GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
    Mnemonics.configure(pointXLabel, pointXSpinner);

    // Absolute y coordinate of the selected point
    JLabel pointYLabel = new JLabel(Msg.msg("RoomPointsView.yLabel", unitName));
    pointPanel.add(pointYLabel, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    pointYModel = new NullableSpinnerLengthModel(preferences, -100000f, 100000f);
    pointYModel.addChangeListener(changeState.wrap(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        Float y = pointYModel.getLength();
        if (y != null) {
          if (moveAllBox.isSelected()) {
            pointsModel.move(0, y - pointsModel.getPoint(pointsList.getSelectedIndex())[1]);
          } else {
            pointsModel.setY(pointsList.getSelectedIndex(), y);
          }
          loadValues(pointYModel);
        }
      }
    }));
    final JSpinner pointYSpinner = new NullableSpinner(pointYModel);
    pointPanel.add(pointYSpinner, new GridBagConstraints(3, 1, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END,
        GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
    Mnemonics.configure(pointYLabel, pointYSpinner);

    // The tabs for the lines before and after the selected point
    lineTabs = new JTabbedPane();
    add(lineTabs, new GridBagConstraints(1, 2, 1, 1, 0.0, 1.0, GridBagConstraints.FIRST_LINE_START,
        GridBagConstraints.BOTH, new Insets(0, 5, 0, 0), 0, 0));

    outgoingLineTab = new LineTab(1);
    lineTabs.addTab(Msg.msg("RoomPointsView.outgoingLineTab"), outgoingLineTab);

    incomingLineTab = new LineTab(-1);
    lineTabs.addTab(Msg.msg("RoomPointsView.incomingLineTab"), incomingLineTab);

    // Button for removing a point
    final JButton removeButton = Mnemonics.configure(new JButton(Msg.msg("RoomPointsView.removeButton")));
    removeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int index = pointsList.getSelectedIndex();
        pointsModel.removePoint(index);
        pointsList.setSelectedIndex(Math.min(index, pointsModel.getSize() - 1));
      }
    });
    add(removeButton, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.LAST_LINE_END,
        GridBagConstraints.NONE, new Insets(5, 5, 0, 0), 0, 0));

    // Show preview
    JLabel previewLabel = new JLabel(Msg.msg("RoomPointsView.previewLabel"));
    add(previewLabel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 5, 5, 0), 0, 0));

    final RoomPointsPreview preview = new RoomPointsPreview(pointsList);
    preview.setMarkNextLine(true);
    add(preview, new GridBagConstraints(2, 1, 1, 3, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(0, 5, 0, 0), 0, 0));
    lineTabs.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        boolean nextLine = lineTabs.getSelectedIndex() == 0;
        preview.setMarkNextLine(nextLine);
        preview.setMarkPreviousLine(!nextLine);
      }
    });

    // Initialize selection listener
    pointsList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting() && pointsModel.getRoom() != null) {
          removeButton.setEnabled(pointsModel.getSize() > 1);
          loadValues(null);
        }
      }
    });
  }

  private void loadValues(Object source) {
    boolean changing = changeState.isChanging();
    try {
      if (!changing) {
        changeState.start();
      }
      int index = pointsList.getSelectedIndex();
      if (index >= 0) {
        if (source != pointXModel && source != pointYModel) {
          float[] point = pointsModel.getPoint(index);
          pointXModel.setLength(point[0]);
          pointYModel.setLength(point[1]);
        }

        incomingLineTab.loadLineValues(source);
        outgoingLineTab.loadLineValues(source);
      }
    } finally {
      if (!changing) {
        changeState.stop();
      }
    }
  }

  /**
   * @see DialogView#displayView(View)
   */
  @Override
  public void displayView(View parentView) {
    LengthUnit lengthUnit = preferences.getLengthUnit();
    List<Selectable> selectedItems = home.getSelectedItems();
    List<Room> selectedRooms = Home.getRoomsSubList(selectedItems);
    RoomEdit edit;
    if (selectedRooms.size() == 0) {
      // Use the last existing room
      List<Room> rooms = home.getRooms();
      if (!rooms.isEmpty()) {
        // Change the last room
        edit = new RoomPointsEdit(rooms.get(rooms.size() - 1));
      } else {
        // Add a new room
        // Default to 5 meter or 12 feet
        float length;
        if (lengthUnit == LengthUnit.INCH || lengthUnit == LengthUnit.INCH_DECIMALS) {
          length = LengthUnit.inchToCentimeter(144);
        } else {
          length = 500;
        }
        edit = new NewRoomEdit(home, length);
      }
    } else {
      // Change the selected room
      edit = new RoomPointsEdit(selectedRooms.get(0));
    }
    edit.doAction();
    pointsModel.setUnit(lengthUnit);
    pointsModel.setRoom(edit.getTarget());
    pointsList.setSelectedIndex(0);
    lineTabs.setSelectedIndex(0);

    Window parentWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
    if (SwingTools.showConfirmDialog(parentWindow instanceof JFrame ? ((JFrame) parentWindow).getRootPane() : null,
      this, Msg.msg("RoomPointsView.dialogTitle"), pointsList) == JOptionPane.OK_OPTION) {
      // Any change is already applied to the room -> just save undo action
      if (undoSupport != null) {
        undoSupport.postEdit(edit);
      }
    } else {
      // Revert any changes
      edit.undo();
    }
  }

  /**
   * Displays the line parameters between the current point and the previous/next one.
   *
   * @author Tobias Liefke
   */
  private class LineTab extends JPanel {
    private static final long serialVersionUID = -7960626821795880141L;

    private final int distance;

    private NullableSpinnerLengthModel otherPointXModel;
    private NullableSpinnerLengthModel otherPointYModel;
    private NullableSpinnerLengthModel lineXModel;
    private NullableSpinnerLengthModel lineYModel;
    private NullableSpinnerLengthModel lengthModel;
    private NullableSpinnerNumberModel angleModel;

    /**
     * Creates a new instance of LineTab.
     *
     * @param distance -1 for displaying the line from the previous point, 
     *                 +1 for displaying the one to the next point
     */
    public LineTab(int distance) {
      super(new GridBagLayout());
      this.distance = distance;
      initComponents();
    }

    public void loadLineValues(Object source) {
      int index = getIndex();

      if (source != otherPointXModel && source != otherPointYModel) {
        float[] point = pointsModel.getPoint(index);
        otherPointXModel.setLength(point[0]);
        otherPointYModel.setLength(point[1]);
      }

      if (source != lineXModel && source != lineYModel) {
        float[] point = pointsModel.getRelativePoint(index, pointsList.getSelectedIndex());
        lineXModel.setLength(point[0]);
        lineYModel.setLength(point[1]);
      }

      if (source != angleModel && source != lengthModel) {
        lengthModel.setLength(pointsModel.getLength(pointsList.getSelectedIndex(), index));
        angleModel.setValue(pointsModel.getVectorAngle(pointsList.getSelectedIndex(), index, getOppositeIndex()));
      }
    }

    private int getIndex() {
      int index = pointsList.getSelectedIndex() + distance;
      int pointsCount = pointsModel.getSize();
      return index < 0 ? index + pointsCount : (index >= pointsCount) ? index - pointsCount : index;
    }

    private int getOppositeIndex() {
      int index = pointsList.getSelectedIndex() - distance;
      int pointsCount = pointsModel.getSize();
      return index < 0 ? index + pointsCount : (index >= pointsCount) ? index - pointsCount : index;
    }

    private void initComponents() {
      String unitName = preferences.getLengthUnit().getName();

      // The panel for the absolute coordinates of the other point
      JPanel pointPanel = new JPanel(new GridBagLayout());
      pointPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
        Msg.msg("RoomPointsView.otherPointPanel", distance)));
      add(pointPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.FIRST_LINE_START,
          GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));

      // Absolute x coordinate of the other point
      JLabel otherPointXLabel = new JLabel(Msg.msg("RoomPointsView.xLabel", unitName));
      pointPanel.add(otherPointXLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      otherPointXModel = new NullableSpinnerLengthModel(preferences, -100000f, 100000f);
      otherPointXModel.addChangeListener(changeState.wrap(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
          Float x = otherPointXModel.getLength();
          if (x != null) {
            pointsModel.setX(getIndex(), x);
            loadValues(otherPointXModel);
          }
        }
      }));
      final JSpinner otherPointXSpinner = new NullableSpinner(otherPointXModel);
      pointPanel.add(otherPointXSpinner, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END,
          GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
      Mnemonics.configure(otherPointXLabel, otherPointXSpinner);

      // Absolute y coordinate of the other point
      JLabel otherPointYLabel = new JLabel(Msg.msg("RoomPointsView.yLabel", unitName));
      pointPanel.add(otherPointYLabel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      otherPointYModel = new NullableSpinnerLengthModel(preferences, -100000f, 100000f);
      otherPointYModel.addChangeListener(changeState.wrap(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
          Float y = otherPointYModel.getLength();
          if (y != null) {
            pointsModel.setY(getIndex(), y);
            loadValues(otherPointYModel);
          }
        }
      }));
      final JSpinner otherPointYSpinner = new NullableSpinner(otherPointYModel);
      pointPanel.add(otherPointYSpinner, new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END,
          GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
      Mnemonics.configure(otherPointYLabel, otherPointYSpinner);

      // The panel for the line
      JPanel linePanel = new JPanel(new GridBagLayout());
      linePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
        Msg.msg("RoomPointsView.linePanel", distance)));
      add(linePanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.FIRST_LINE_START,
          GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));

      // X distance of the line
      JLabel lineXLabel = new JLabel(Msg.msg("RoomPointsView.xLabel", unitName));
      linePanel.add(lineXLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
      lineXModel = new NullableSpinnerLengthModel(preferences, -100000f, 100000f);
      lineXModel.addChangeListener(changeState.wrap(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
          Float x = lineXModel.getLength();
          Float y = lineYModel.getLength();
          if (x != null && y != null) {
            pointsModel.setRelativePoint(getIndex(), pointsList.getSelectedIndex(), new float[] { x, y });
            loadValues(lineXModel);
          }
        }
      }));
      final JSpinner lineXSpinner = new NullableSpinner(lineXModel);
      linePanel.add(lineXSpinner, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END,
          GridBagConstraints.NONE, new Insets(5, 0, 0, 5), 0, 0));
      Mnemonics.configure(lineXLabel, lineXSpinner);

      // Y distance of the line
      JLabel lineYLabel = new JLabel(Msg.msg("RoomPointsView.yLabel", unitName));
      linePanel.add(lineYLabel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
      lineYModel = new NullableSpinnerLengthModel(preferences, -100000f, 100000f);
      lineYModel.addChangeListener(changeState.wrap(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
          Float x = lineXModel.getLength();
          Float y = lineYModel.getLength();
          if (x != null && y != null) {
            pointsModel.setRelativePoint(getIndex(), pointsList.getSelectedIndex(), new float[] { x, y });
            loadValues(lineYModel);
          }
        }
      }));
      final JSpinner lineYSpinner = new NullableSpinner(lineYModel);
      linePanel.add(lineYSpinner, new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END,
          GridBagConstraints.NONE, new Insets(5, 0, 0, 5), 0, 0));
      Mnemonics.configure(lineYLabel, lineYSpinner);

      // The distance between the points
      JLabel lengthLabel = new JLabel(Msg.msg("RoomPointsView.lengthLabel", unitName));
      linePanel.add(lengthLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      lengthModel = new NullableSpinnerLengthModel(preferences, -100000f, 100000f);
      lengthModel.addChangeListener(changeState.wrap(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
          Float length = lengthModel.getLength();
          Number angle = angleModel.getNumber();
          if (length != null && angle != null) {
            pointsModel.setVector(pointsList.getSelectedIndex(), getIndex(), getOppositeIndex(), length,
              angle.floatValue());
            loadValues(lengthModel);
          }
        }
      }));
      final JSpinner vectorLengthSpinner = new NullableSpinner(lengthModel);
      linePanel.add(vectorLengthSpinner, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END,
          GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
      Mnemonics.configure(lengthLabel, vectorLengthSpinner);

      // The angle of the line from this point to the next
      JLabel angleLabel = new JLabel(Msg.msg("RoomPointsView.angleLabel"));
      linePanel.add(angleLabel, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
          GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
      angleModel = new NullableSpinnerNumberModel(0f, -360f, 360f, 0.5f);
      angleModel.addChangeListener(changeState.wrap(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
          Float length = lengthModel.getLength();
          Number angle = angleModel.getNumber();
          if (length != null && angle != null) {
            pointsModel.setVector(pointsList.getSelectedIndex(), getIndex(), getOppositeIndex(), length,
              angle.floatValue());
            loadValues(angleModel);
          }
        }
      }));
      final JSpinner angleSpinner = new NullableSpinner(angleModel);
      linePanel.add(angleSpinner, new GridBagConstraints(3, 1, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END,
          GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
      Mnemonics.configure(angleLabel, angleSpinner);
    }

  }

  /**
   * Base class for any modifications to the room.
   */
  private abstract static class RoomEdit extends AbstractObjectEdit<Room> {
    private static final long serialVersionUID = 2715739085750697063L;

    /**
     * Creates a new instance of RoomEdit.
     *
     * @param room the edited room
     */
    public RoomEdit(Room room) {
      super(room);
    }

    @Override
    public String getPresentationName() {
      return Msg.msg("RoomPointsView.pointsEdit");
    }
  }

  /**
   * Adds a new room to the plan.
   */
  private static final class NewRoomEdit extends RoomEdit {
    private static final long serialVersionUID = 3418154221734909643L;

    private final List<Selectable> selectedItems;
    private final Home home;

    private NewRoomEdit(Home home, float length) {
      super(new Room(new float[][] { { 0, 0 }, { length, 0 }, { length, length }, { 0, length } }));
      this.home = home;
      this.selectedItems = home.getSelectedItems();
    }

    @Override
    public void undoAction() throws CannotUndoException {
      home.setSelectedItems(selectedItems);
      home.deleteRoom(target);
    }

    @Override
    public void doAction() throws CannotRedoException {
      home.addRoom(target);
      home.setSelectedItems(Collections.singletonList(target));
    }

  }

  /**
   * Adds the points of a room in the plan.
   */
  private static final class RoomPointsEdit extends RoomEdit {
    private static final long serialVersionUID = -9063757164674999439L;

    private final float[][] oldPoints;
    private float[][] newPoints;

    private RoomPointsEdit(Room room) {
      super(room);
      this.newPoints = this.oldPoints = room.getPoints();
    }

    @Override
    public void undoAction() throws CannotUndoException {
      newPoints = target.getPoints();
      target.setPoints(oldPoints);
    }

    @Override
    public void doAction() throws CannotRedoException {
      target.setPoints(newPoints);
    }

  }

  /**
   * Moves a point in the list of points
   */
  private class MovePointAction extends AbstractAction {
    private static final long serialVersionUID = 5625648055261721533L;

    private final int diff;

    public MovePointAction(int diff) {
      this.diff = diff;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
      int index = pointsList.getSelectedIndex();
      if (index >= 0) {
        int newIndex = index + diff;
        if (newIndex >= 0 && newIndex < pointsModel.getSize()) {
          pointsModel.movePoint(index, newIndex);
          pointsList.setSelectedIndex(newIndex);
        }
      }
    }
  }

}
