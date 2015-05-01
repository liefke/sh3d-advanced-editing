package de.starrunner.sweethome3d;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.geom.AffineTransform;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.AutoCommitSpinner;
import com.eteks.sweethome3d.swing.NullableSpinner.NullableSpinnerLengthModel;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.View;

import de.starrunner.util.strings.Mnemonics;

/**
 * Lets a user move the selected objects.
 *
 * Copyright (c) 2015 by Tobias Liefke
 *
 * @author Tobias Liefke
 */
public class MoveView extends ImmediateEditDialogView {

  private static final long serialVersionUID = -1605093837636982800L;

  private TransformEdit currentEdit;

  private NullableSpinnerLengthModel xModel;
  private NullableSpinnerLengthModel yModel;

  /**
   * Creates a new instance of MoveView.
   * 
   * @param home the application
   * @param preferences the current configuration
   * @param undoSupport used for undo support of the current action
   */
  public MoveView(Home home, UserPreferences preferences, UndoableEditSupport undoSupport) {
    super(Msg.msg("MoveView.dialogTitle"), home, preferences, undoSupport);
    initComponents();
  }

  /**
   * Create and add the components.
   */
  private void initComponents() {
    String unitName = preferences.getLengthUnit().getName();

    // Horizontal part of the move
    JLabel xLabel = new JLabel(Msg.msg("MoveView.xLabel", unitName));
    add(xLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
        new Insets(5, 5, 5, 5), 0, 0));
    xModel = new NullableSpinnerLengthModel(preferences, 0, -100000f, 100000f);
    xModel.addChangeListener(createLazyChangeListener());
    final JSpinner xSpinner = new AutoCommitSpinner(xModel);
    add(xSpinner, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.NONE,
        new Insets(5, 0, 5, 5), 0, 0));
    Mnemonics.configure(xLabel, xSpinner);

    // Vertical part of the move    
    JLabel yLabel = new JLabel(Msg.msg("MoveView.yLabel", unitName));
    add(yLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
        new Insets(5, 5, 5, 5), 0, 0));
    yModel = new NullableSpinnerLengthModel(preferences, 0, -100000f, 100000f);
    yModel.addChangeListener(createLazyChangeListener());
    final JSpinner ySpinner = new AutoCommitSpinner(yModel);
    add(ySpinner, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.NONE,
        new Insets(5, 0, 5, 5), 0, 0));
    Mnemonics.configure(yLabel, ySpinner);
  }

  /**
   * @see DialogView#displayView(View)
   */
  @Override
  public void displayView(View parentView) {
    currentEdit = new TransformEdit(getTitle(), home);

    showDialog(currentEdit);
  }

  @Override
  protected void apply() {
    // Check for correct input parameters
    Float x = xModel.getLength();
    Float y = yModel.getLength();
    if (x == null || y == null) {
      return;
    }

    // Convert input parameters to affine transformation
    currentEdit.transform(new AffineTransform(1, 0, 0, 1, preferences.getLengthUnit().unitToCentimeter(x),
        preferences.getLengthUnit().unitToCentimeter(y)));
  }

}
