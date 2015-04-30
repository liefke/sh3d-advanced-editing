package de.starrunner.sweethome3d;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.*;
import com.eteks.sweethome3d.swing.NullableSpinner.NullableSpinnerLengthModel;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.View;

import de.starrunner.components.event.ChangeState;
import de.starrunner.util.strings.Mnemonics;

/**
 * Lets a user elevate the selected furniture.
 *
 * Copyright (c) 2010 by Tobias Liefke
 *
 * @author Tobias Liefke
 */
public class ElevationView extends JPanel implements DialogView {
  private static final long serialVersionUID = 5472910675709402527L;

  private final Home home;
  private final UserPreferences preferences;
  private final UndoableEditSupport undoSupport;

  private final ChangeState changeState = new ChangeState();

  private ElevationEdit currentEdit;
  private Timer timer;

  private NullableSpinnerLengthModel elevationModel;

  /**
   * Creates a new instance of ElevationView.
   * 
   * @param home the application
   * @param preferences the current configuration
   * @param undoSupport used for undo support of the current action
   */
  public ElevationView(Home home, UserPreferences preferences, UndoableEditSupport undoSupport) {
    super(new GridBagLayout());
    this.home = home;
    this.preferences = preferences;
    this.undoSupport = undoSupport;
    initTimer();
    initComponents();
  }

  /**
   * Initializes the timer responsible for changing the objects.
   */
  private void initTimer() {
    timer = new Timer(300, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        elevate();
      }
    });
    timer.setRepeats(false);
  }

  /**
   * Create and add the components.
   */
  private void initComponents() {
    JLabel elevationLabel = new JLabel(Msg.msg("ElevationView.elevationLabel", preferences.getLengthUnit().getName()));
    add(elevationLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    elevationModel = new NullableSpinnerLengthModel(preferences, -100000f, 100000f);
    elevationModel.setLength(new Float(0));
    elevationModel.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        timer.restart();
      }
    });
    final JSpinner elevationSpinner = new NullableSpinner(elevationModel);
    add(elevationSpinner, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    Mnemonics.configure(elevationLabel, elevationSpinner);
  }

  /**
   * @see DialogView#displayView(View)
   */
  @Override
  public void displayView(View parentView) {
    changeState.start();
    currentEdit = new ElevationEdit(home);

    Window parentWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
    if (SwingTools.showConfirmDialog(parentWindow instanceof JFrame ? ((JFrame) parentWindow).getRootPane() : null,
      this, Msg.msg("ElevationView.dialogTitle"), null) == JOptionPane.OK_OPTION) {

      // Apply the last change, if nessecary
      if (timer.isRunning()) {
        timer.stop();
        elevate();
      }

      if (undoSupport != null) {
        undoSupport.postEdit(currentEdit);
      }
    } else {
      // Revert any changes
      timer.stop();
      currentEdit.undo();
    }
  }

  private void elevate() {
    Float elevation = elevationModel.getLength();
    if (elevation != null) {
      currentEdit.elevate(elevation);
    }
  }

}
