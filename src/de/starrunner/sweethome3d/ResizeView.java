package de.starrunner.sweethome3d;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.*;
import com.eteks.sweethome3d.swing.*;
import com.eteks.sweethome3d.swing.NullableSpinner.NullableSpinnerLengthModel;
import com.eteks.sweethome3d.swing.NullableSpinner.NullableSpinnerNumberModel;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.View;

import de.starrunner.components.event.ChangeState;
import de.starrunner.util.strings.Mnemonics;

/**
 * Lets a user resize the selected objects.
 *
 * Copyright (c) 2010 by Tobias Liefke
 *
 * @author Tobias Liefke
 */
public class ResizeView extends ImmediateEditDialogView {
  private static final long serialVersionUID = -3184198019104925119L;

  /**
   * The measure used for the new size.
   */
  private enum Measure {
    ABSOLUTE {
      @Override
      protected float doConvert(Measure source, float size, float value) {
        if (source == RELATIVE) {
          return value + size;
        } else /* if (source == PERCENT)*/{
          return value / 100 * size;
        }
      }

      @Override
      public float getResizeFactor(float size, float value) {
        return size == 0 ? 1 : (value / size);
      }

    },
    RELATIVE {
      @Override
      protected float doConvert(Measure source, float size, float value) {
        if (source == ABSOLUTE) {
          return value - size;
        } else /* if (source == PERCENT)*/{
          return (value / 100 - 1) * size;
        }
      }

      @Override
      public float getResizeFactor(float size, float value) {
        return size == 0 ? 1 : (value / size + 1);
      }
    },
    PERCENT {
      @Override
      protected float doConvert(Measure source, float size, float value) {
        if (source == ABSOLUTE) {
          return size == 0 ? 100 : (value / size * 100);
        } else /* if (source == RELATIVE) */{
          return size == 0 ? 100 : ((value / size + 1) * 100);
        }
      }

      @Override
      public float getResizeFactor(float size, float value) {
        return value / 100;
      }
    };

    public abstract float getResizeFactor(float size, float value);

    public Float convert(Measure source, Number size, Number value) {
      if (size == null || value == null) {
        return null;
      }
      if (source == this) {
        return value.floatValue();
      }
      return doConvert(source, size.floatValue(), value.floatValue());
    }

    protected abstract float doConvert(Measure source, float size, float value);
  }

  /**
   * The positions of the fix point.
   */
  private enum Position {
    NORTH_WEST("nw", 0, 0), NORTH("n", 0.5f, 0), NORTH_EAST("ne", 1, 0), // First row
    WEST("w", 0, 0.5f), CENTER("c", 0.5f, 0.5f), EAST("e", 1, 0.5f), // Second row
    SOUTH_WEST("sw", 0, 1), SOUTH("s", 0.5f, 1), SOUTH_EAST("se", 1, 1); // Third row

    private final String imageName;
    private final float dx;
    private final float dy;

    Position(String shortCut, float xd, float yd) {
      this.imageName = "/de/starrunner/sweethome3d/resources/resize-fix-" + shortCut + ".png";
      this.dx = xd;
      this.dy = yd;
    }

  }

  private TransformEdit currentEdit;
  private Rectangle2D.Float bounds;

  private ChangeState changeState = new ChangeState();

  private NullableSpinnerNumberModel widthModel;
  private NullableSpinnerNumberModel heightModel;
  private JSpinner widthSpinner;
  private JSpinner heightSpinner;
  private JComboBox measureBox;
  private JCheckBox keepRatioButton;
  private JSpinner ratioSpinner;
  private NullableSpinnerNumberModel ratioModel;
  private NullableSpinnerLengthModel fixPointXModel;
  private NullableSpinnerLengthModel fixPointYModel;
  private JToggleButton[] positionButtons;

  /**
   * Creates a new instance of ResizeView.
   * 
   * @param home the application
   * @param preferences the current configuration
   * @param undoSupport used for undo support of the current action
   */
  public ResizeView(Home home, UserPreferences preferences, UndoableEditSupport undoSupport) {
    super(Msg.msg("ResizeView.dialogTitle"), home, preferences, undoSupport);
    initComponents();
  }

  /**
   * Create and add the components.
   */
  private void initComponents() {
    String unitName = preferences.getLengthUnit().getName();

    // The dimension panel
    JPanel dimensionPanel = new JPanel(new GridBagLayout());
    dimensionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
      Msg.msg("ResizeView.dimensionPanel")));
    add(dimensionPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.FIRST_LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

    // The width
    JLabel widthLabel = new JLabel(Msg.msg("ResizeView.widthLabel"));
    dimensionPanel.add(widthLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
    widthModel = new NullableSpinnerNumberModel(0, -100000f, 100000f, 0.5f);
    widthModel.addChangeListener(changeState.wrap(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        if (keepRatioButton.isSelected()) {
          keepRatio(widthModel);
        } else {
          applyLazy();
        }
      }
    }));
    widthSpinner = new AutoCommitSpinner(widthModel);
    dimensionPanel.add(widthSpinner, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(5, 0, 0, 5), 0, 0));
    Mnemonics.configure(widthLabel, widthSpinner);

    // The height
    JLabel heightLabel = new JLabel(Msg.msg("ResizeView.heightLabel"));
    dimensionPanel.add(heightLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
    heightModel = new NullableSpinnerNumberModel(0, -100000f, 100000f, 0.5f);
    heightModel.addChangeListener(changeState.wrap(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        if (keepRatioButton.isSelected()) {
          keepRatio(heightModel);
        } else {
          applyLazy();
        }
      }
    }));
    heightSpinner = new AutoCommitSpinner(heightModel);
    dimensionPanel.add(heightSpinner, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
    Mnemonics.configure(heightLabel, heightSpinner);

    // The measure
    JLabel measureLabel = new JLabel(Msg.msg("ResizeView.measureLabel", unitName));
    dimensionPanel.add(measureLabel, new GridBagConstraints(2, 0, 1, 2, 0.0, 0.0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));

    final String[] measures = new String[Measure.values().length];
    int i = 0;
    for (Measure measure : Measure.values()) {
      measures[i++] = Msg.msg("ResizeView." + measure.name().toLowerCase() + "Measure", unitName);
    }

    measureBox = new JComboBox(measures);
    measureBox.addItemListener(changeState.wrap(new ItemListener() {
      private Measure previousMeasure;

      @Override
      public void itemStateChanged(ItemEvent e) {
        Measure measure = Measure.values()[Arrays.asList(measures).indexOf(e.getItem())];
        if (e.getStateChange() == ItemEvent.DESELECTED) {
          previousMeasure = measure;
        } else if (e.getStateChange() == ItemEvent.SELECTED && measure != previousMeasure) {
          widthModel.setValue(measure.convert(previousMeasure, bounds.width, widthModel.getNumber()));
          heightModel.setValue(measure.convert(previousMeasure, bounds.height, heightModel.getNumber()));
        }
      }
    }));
    dimensionPanel.add(measureBox, new GridBagConstraints(3, 0, 1, 2, 0.0, 0.0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    Mnemonics.configure(measureLabel, measureBox);

    keepRatioButton = Mnemonics.configure(new JCheckBox(Msg.msg("ResizeView.keepRatioLabel")));
    keepRatioButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (keepRatioButton.isSelected()) {
          changeState.start();
          try {
            keepRatio(null);
          } finally {
            changeState.stop();
          }
        }
      }
    });
    dimensionPanel.add(keepRatioButton, new GridBagConstraints(0, 2, 4, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

    ratioModel = new NullableSpinnerNumberModel(0f, -100000f, 100000f, 0.01f);
    ratioModel.addChangeListener(changeState.wrap(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        keepRatio(null);
      }
    }));
    ratioSpinner = new AutoCommitSpinner(ratioModel);
    dimensionPanel.add(ratioSpinner, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_END,
        GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
    dimensionPanel.add(new JLabel(": 1"), new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));

    // The position panel
    JPanel positionPanel = new JPanel(new GridBagLayout());
    positionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
      Msg.msg("ResizeView.positionPanel")));
    add(positionPanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.FIRST_LINE_START,
        GridBagConstraints.HORIZONTAL, new Insets(5, 0, 0, 0), 0, 0));

    // The position buttons
    GridBagConstraints gc = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER,
        GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0);
    ButtonGroup positionGroup = new ButtonGroup();
    ActionListener buttonListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        changeFixPoint(Position.valueOf(e.getActionCommand()));
        applyLazy();
      }
    };
    positionButtons = new JToggleButton[9];
    for (Position position : Position.values()) {
      JToggleButton positionButton = new JToggleButton(new ImageIcon(ResizeView.class.getResource(position.imageName)));
      positionButton.setMargin(new Insets(4, 4, 4, 4));
      positionButton.addActionListener(buttonListener);
      positionButton.setActionCommand(position.name());
      positionPanel.add(positionButton, gc);
      positionGroup.add(positionButton);
      positionButtons[position.ordinal()] = positionButton;
      if (++gc.gridx == 3) {
        gc.gridx = 0;
        if (++gc.gridy == 2) {
          gc.insets = new Insets(1, 1, 5, 1);
        }
      }
    }

    JPanel fixPointPanel = new JPanel(new GridBagLayout());
    positionPanel.add(fixPointPanel, new GridBagConstraints(3, 0, 1, 3, 0, 0, GridBagConstraints.LINE_END,
        GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

    // Absolute x coordinate of the fix point
    JLabel fixPointXLabel = new JLabel(Msg.msg("ResizeView.xLabel", unitName));
    fixPointPanel.add(fixPointXLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    fixPointXModel = new NullableSpinnerLengthModel(preferences, -100000f, 100000f);
    fixPointXModel.addChangeListener(createLazyChangeListener());
    final JSpinner fixPointXSpinner = new AutoCommitSpinner(fixPointXModel);
    fixPointPanel.add(fixPointXSpinner, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END,
        GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
    Mnemonics.configure(fixPointXLabel, fixPointXSpinner);

    // Absolute y coordinate of the fix point
    JLabel fixPointYLabel = new JLabel(Msg.msg("ResizeView.yLabel", unitName));
    fixPointPanel.add(fixPointYLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
    fixPointYModel = new NullableSpinnerLengthModel(preferences, -100000f, 100000f);
    fixPointYModel.addChangeListener(createLazyChangeListener());
    final JSpinner fixPointYSpinner = new AutoCommitSpinner(fixPointYModel);
    fixPointPanel.add(fixPointYSpinner, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END,
        GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
    Mnemonics.configure(fixPointYLabel, fixPointYSpinner);
  }

  /**
   * @see DialogView#displayView(View)
   */
  @Override
  public void displayView(View parentView) {
    // Don't apply the next UI changes
    changeState.start();
    currentEdit = new TransformEdit(getTitle(), home);

    // Calculate bounds in current unit
    bounds = currentEdit.getBounds();
    LengthUnit unit = preferences.getLengthUnit();
    bounds.x = unit.centimeterToUnit(bounds.x);
    bounds.y = unit.centimeterToUnit(bounds.y);
    bounds.width = unit.centimeterToUnit(bounds.width);
    bounds.height = unit.centimeterToUnit(bounds.height);

    // Prepare all fields
    measureBox.setSelectedIndex(Measure.ABSOLUTE.ordinal());
    widthModel.setValue(bounds.width);
    heightModel.setValue(bounds.height);
    keepRatioButton.setSelected(true);
    ratioModel.setValue(bounds.width / bounds.height);
    changeFixPoint(Position.CENTER);
    changeState.stop();
    widthSpinner.setEnabled(bounds.width != 0);
    heightSpinner.setEnabled(bounds.height != 0);
    keepRatioButton.setEnabled(widthSpinner.isEnabled() && heightSpinner.isEnabled());
    ratioSpinner.setEnabled(keepRatioButton.isEnabled());

    // And finally - display the dialog
    showDialog(currentEdit);
  }

  /**
   * Changes the position of the fix point to the given position.
   */
  private void changeFixPoint(Position position) {
    positionButtons[position.ordinal()].setSelected(true);
    fixPointXModel.setLength(bounds.x + position.dx * bounds.width);
    fixPointYModel.setLength(bounds.y + position.dy * bounds.height);
  }

  /**
   * Ensures that width and height have the aspect ratio from the ratio input.
   *
   * @param sourceModel the model that is changing
   */
  private void keepRatio(SpinnerNumberModel sourceModel) {
    Number width = widthModel.getNumber();
    Number height = heightModel.getNumber();
    Number ratio = ratioModel.getNumber();
    if (ratio != null) {
      Measure currentMeasure = Measure.values()[measureBox.getSelectedIndex()];
      if (width != null && sourceModel != heightModel) {
        heightModel.setValue(currentMeasure.convert(Measure.ABSOLUTE, bounds.height,
          Measure.ABSOLUTE.convert(currentMeasure, bounds.width, width) / ratio.floatValue()));
      } else if (height != null && sourceModel != widthModel) {
        widthModel.setValue(currentMeasure.convert(Measure.ABSOLUTE, bounds.width,
          Measure.ABSOLUTE.convert(currentMeasure, bounds.height, height) * ratio.floatValue()));
      }
      applyLazy();
    }
  }

  @Override
  protected void apply() {
    // Check for correct input parameters
    Number width = widthModel.getNumber();
    Number height = heightModel.getNumber();
    Float fixPointX = fixPointXModel.getLength();
    Float fixPointY = fixPointYModel.getLength();
    if (width == null || height == null || fixPointX == null || fixPointY == null) {
      return;
    }

    // Convert input parameters to affine transformation
    Measure measure = Measure.values()[measureBox.getSelectedIndex()];
    float resizeX = measure.getResizeFactor(bounds.width, width.floatValue());
    float resizeY = measure.getResizeFactor(bounds.height, height.floatValue());

    currentEdit.transform(new AffineTransform(resizeX, 0, 0, resizeY, (1 - resizeX)
        * preferences.getLengthUnit().unitToCentimeter(fixPointX), (1 - resizeY)
        * preferences.getLengthUnit().unitToCentimeter(fixPointY)));
  }

}
