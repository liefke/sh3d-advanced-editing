package de.starrunner.sweethome3d;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.*;
import com.eteks.sweethome3d.swing.NullableSpinner.NullableSpinnerNumberModel;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.View;

import de.starrunner.components.event.ChangeState;
import de.starrunner.util.strings.Mnemonics;

/**
 * Lets a user rotate the selected objects.
 *
 * Copyright (c) 2010 by Tobias Liefke
 * Copyright (c) 2015 by Igor A. Perminov
 *
 * @author Tobias Liefke
 */
public class RotateView extends JPanel implements DialogView {
  private static final long serialVersionUID = 6279629119148423282L;

  private final Home home;
  private final UndoableEditSupport undoSupport;

  private final ChangeState changeState = new ChangeState();

  private TransformEdit currentEdit;
  private Rectangle2D.Float bounds;
  private Timer rotateTimer;

  private NullableSpinnerNumberModel angleModel;

  private JCheckBox rotateTextButton;
  private JCheckBox adjustTextButton;

  /**
   * Creates a new instance of RotateView.
   * 
   * @param home the application
   * @param preferences the current configuration
   * @param undoSupport used for undo support of the current action
   */
  public RotateView(Home home, UserPreferences preferences, UndoableEditSupport undoSupport) {
    super(new GridBagLayout());
    this.home = home;
    this.undoSupport = undoSupport;
    initRotateTimer();
    initComponents();
  }

  /**
   * Initializes the timer responsible for rotate the model.
   */
  private void initRotateTimer() {
    rotateTimer = new Timer(300, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        rotate();
      }
    });
    rotateTimer.setRepeats(false);
  }

  /**
   * Create and add the components.
   */
  private void initComponents() {
    JLabel angleLabel = new JLabel(Msg.msg("RotateView.angleLabel"));
    add(angleLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.NONE,
        new Insets(0, 0, 0, 5), 0, 0));
    angleModel = new NullableSpinnerNumberModel(0f, -360f, 360f, 0.5f);
    angleModel.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        rotateTimer.restart();
      }
    });
    final JSpinner angleSpinner = new AutoCommitSpinner(angleModel);
    add(angleSpinner, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    Mnemonics.configure(angleLabel, angleSpinner);
    ActionListener actionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          rotateTimer.restart();
        }
    };
    rotateTextButton = Mnemonics.configure(new JCheckBox(Msg.msg("RotateView.rotateTextLabel")));
    rotateTextButton.setSelected(true);
    rotateTextButton.addActionListener(actionListener);
    rotateTextButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          adjustTextButton.setEnabled(rotateTextButton.isSelected());
        }
    });
    add(rotateTextButton, new GridBagConstraints(0, 1, 2, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));
    adjustTextButton = Mnemonics.configure(new JCheckBox(Msg.msg("RotateView.adjustTextLabel")));
    adjustTextButton.setSelected(true);
    adjustTextButton.addActionListener(actionListener);
    add(adjustTextButton, new GridBagConstraints(0, 2, 2, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));
  }

  /**
   * @see DialogView#displayView(View)
   */
  @Override
  public void displayView(View parentView) {
    changeState.start();
    currentEdit = new TransformEdit(home);
    bounds = currentEdit.getBounds();

    Window parentWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
    if (SwingTools.showConfirmDialog(parentWindow instanceof JFrame ? ((JFrame) parentWindow).getRootPane() : null,
      this, Msg.msg("RotateView.dialogTitle"), null) == JOptionPane.OK_OPTION) {

      // Apply the last change, if nessecary
      if (rotateTimer.isRunning()) {
        rotateTimer.stop();
        rotate();
      }

      if (undoSupport != null) {
        undoSupport.postEdit(currentEdit);
      }
    } else {
      // Revert any changes
      rotateTimer.stop();
      currentEdit.undo();
    }
  }

  private void rotate() {
    Number angle = angleModel.getNumber();
    if (angle != null) {
      boolean isRotateText = rotateTextButton.isSelected();
      boolean isAdjustText = isRotateText && adjustTextButton.isSelected();
      currentEdit.transform(AffineTransform.getRotateInstance(Math.toRadians(angle.floatValue()), bounds.getCenterX(),
        bounds.getCenterY()), isRotateText, isAdjustText);
    }
  }

}
