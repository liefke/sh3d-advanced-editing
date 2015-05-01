package de.starrunner.sweethome3d;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.NullableSpinner;
import com.eteks.sweethome3d.swing.NullableSpinner.NullableSpinnerLengthModel;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.View;

import de.starrunner.util.strings.Mnemonics;

/**
 * Lets a user elevate the selected furniture.
 *
 * Copyright (c) 2010 by Tobias Liefke
 *
 * @author Tobias Liefke
 */
public class ElevationView extends ImmediateEditDialogView {
  private static final long serialVersionUID = 5472910675709402527L;

  private ElevationEdit currentEdit;

  private NullableSpinnerLengthModel elevationModel;

  /**
   * Creates a new instance of ElevationView.
   * 
   * @param home the application
   * @param preferences the current configuration
   * @param undoSupport used for undo support of the current action
   */
  public ElevationView(Home home, UserPreferences preferences, UndoableEditSupport undoSupport) {
    super(Msg.msg("ElevationView.dialogTitle"), home, preferences, undoSupport);
    initComponents();
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
    elevationModel.addChangeListener(createLazyChangeListener());
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
    currentEdit = new ElevationEdit(home);
    showDialog(currentEdit);
  }

  @Override
  protected void apply() {
    Float elevation = elevationModel.getLength();
    if (elevation != null) {
      currentEdit.elevate(elevation);
    }
  }

}
