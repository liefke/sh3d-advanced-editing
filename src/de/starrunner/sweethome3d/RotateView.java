package de.starrunner.sweethome3d;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.*;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.AutoCommitSpinner;
import com.eteks.sweethome3d.swing.NullableSpinner.NullableSpinnerNumberModel;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.View;

import de.starrunner.util.strings.Mnemonics;

/**
 * Lets a user rotate the selected objects.
 *
 * Copyright (c) 2010 by Tobias Liefke
 * Copyright (c) 2015 by Igor A. Perminov
 *
 * @author Tobias Liefke
 * @author Igor A. Perminov
 */
public class RotateView extends ImmediateEditDialogView {
  private static final long serialVersionUID = 6279629119148423282L;

  private TransformEdit currentEdit;
  private Rectangle2D.Float bounds;

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
    super(Msg.msg("RotateView.dialogTitle"), home, preferences, undoSupport);
    initComponents();
  }

  /**
   * Create and add the components.
   */
  private void initComponents() {
    JLabel angleLabel = new JLabel(Msg.msg("RotateView.angleLabel"));
    add(angleLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.NONE,
        new Insets(0, 0, 0, 5), 0, 0));
    angleModel = new NullableSpinnerNumberModel(0f, -360f, 360f, 0.5f);
    angleModel.addChangeListener(createLazyChangeListener());
    final JSpinner angleSpinner = new AutoCommitSpinner(angleModel);
    add(angleSpinner, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    Mnemonics.configure(angleLabel, angleSpinner);
    rotateTextButton = Mnemonics.configure(new JCheckBox(Msg.msg("RotateView.rotateTextLabel")));
    rotateTextButton.setSelected(true);
    rotateTextButton.addActionListener(createLazyActionListener());
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
    adjustTextButton.addActionListener(createLazyActionListener());
    add(adjustTextButton, new GridBagConstraints(0, 2, 2, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));
  }

  /**
   * @see DialogView#displayView(View)
   */
  @Override
  public void displayView(View parentView) {
    currentEdit = new TransformEdit(getTitle(), home);
    bounds = currentEdit.getBounds();

    showDialog(currentEdit);
  }

  @Override
  protected void apply() {
    Number angle = angleModel.getNumber();
    if (angle != null) {
      boolean rotateText = rotateTextButton.isSelected();
      currentEdit.transform(
        AffineTransform.getRotateInstance(Math.toRadians(angle.floatValue()), bounds.getCenterX(), bounds.getCenterY()),
        rotateText, rotateText && adjustTextButton.isSelected());
    }
  }

}
