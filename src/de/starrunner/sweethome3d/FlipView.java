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
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.View;

import de.starrunner.util.strings.Mnemonics;

/**
 * Lets a user flip the selected objects.
 *
 * Copyright (c) 2010 by Tobias Liefke
 * Copyright (c) 2015 by Igor A. Perminov
 *
 * @author Igor A. Perminov
 * @author Tobias Liefke
 */
public class FlipView extends ImmediateEditDialogView {
  private static final long serialVersionUID = 7891739638750256221L;

  private TransformEdit currentEdit;
  private Rectangle2D.Float bounds;

  private JCheckBox flipHorizontallyButton;
  private JCheckBox flipVerticallyButton;
  private JCheckBox rotateTextButton;
  private JCheckBox adjustTextButton;

  /**
   * Creates a new instance of FlipView.
   * 
   * @param home the application
   * @param preferences the current configuration
   * @param undoSupport used for undo support of the current action
   */
  public FlipView(Home home, UserPreferences preferences, UndoableEditSupport undoSupport) {
    super(Msg.msg("FlipView.dialogTitle"), home, preferences, undoSupport);
    initComponents();
  }

  /**
   * Create and add the components.
   */
  private void initComponents() {
    flipHorizontallyButton = Mnemonics.configure(new JCheckBox(Msg.msg("FlipView.flipHorizontallyLabel")));
    flipHorizontallyButton.addActionListener(createLazyActionListener());
    add(flipHorizontallyButton, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    add(new JLabel(new ImageIcon(FlipView.class.getResource("resources/plan-flip-objects.png"))),
      new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0,
          0, 0), 0, 0));
    flipVerticallyButton = Mnemonics.configure(new JCheckBox(Msg.msg("FlipView.flipVerticallyLabel")));
    flipVerticallyButton.addActionListener(createLazyActionListener());
    add(flipVerticallyButton, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    add(new JLabel(new ImageIcon(FlipView.class.getResource("resources/plan-flip-objects-vertical.png"))),
      new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(0, 0,
          0, 0), 0, 0));
    rotateTextButton = Mnemonics.configure(new JCheckBox(Msg.msg("FlipView.rotateTextLabel")));
    rotateTextButton.setSelected(true);
    rotateTextButton.addActionListener(createLazyActionListener());
    rotateTextButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        adjustTextButton.setEnabled(rotateTextButton.isSelected());
      }
    });
    add(rotateTextButton, new GridBagConstraints(0, 2, 2, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    adjustTextButton = Mnemonics.configure(new JCheckBox(Msg.msg("FlipView.adjustTextLabel")));
    adjustTextButton.setSelected(true);
    adjustTextButton.addActionListener(createLazyActionListener());
    add(adjustTextButton, new GridBagConstraints(0, 3, 2, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));
  }

  /**
   * @see DialogView#displayView(View)
   */
  @Override
  public void displayView(View parentView) {
    currentEdit = new TransformEdit(home);
    bounds = currentEdit.getBounds();

    showDialog(currentEdit);
  }

  @Override
  protected void apply() {
    boolean isFlipHorizontally = flipHorizontallyButton.isSelected();
    boolean isFlipVertically = flipVerticallyButton.isSelected();
    boolean isRotateText = rotateTextButton.isSelected();
    boolean isAdjustText = isRotateText && adjustTextButton.isSelected();
    float scaleX = isFlipHorizontally ? -1 : 1;
    float scaleY = isFlipVertically ? -1 : 1;
    currentEdit.transform(
      new AffineTransform(scaleX, 0, 0, scaleY, bounds.getCenterX() * (1 - scaleX), bounds.getCenterY() * (1 - scaleY)),
      isRotateText, isAdjustText);
  }

}
