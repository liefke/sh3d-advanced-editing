package de.starrunner.sweethome3d;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.*;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.*;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.View;

import de.starrunner.components.event.ChangeState;
import de.starrunner.util.strings.Mnemonics;

/**
 * Lets a user flip the selected objects.
 *
 * Copyright (c) 2010 by Tobias Liefke
 * Copyright (c) 2015 by Igor A. Perminov
 *
 * @author Igor A. Perminov
 */
public class FlipView extends JPanel implements DialogView {
  private static final long serialVersionUID = 7891739638750256221L;

  private final Home home;
  private final UndoableEditSupport undoSupport;

  private final ChangeState changeState = new ChangeState();

  private TransformEdit currentEdit;
  private Rectangle2D.Float bounds;
  private Timer flipTimer;

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
    super(new GridBagLayout());
    this.home = home;
    this.undoSupport = undoSupport;
    initFlipTimer();
    initComponents();
  }

  /**
   * Initializes the timer responsible for rotate the model.
   */
  private void initFlipTimer() {
    flipTimer = new Timer(300, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        flip();
      }
    });
    flipTimer.setRepeats(false);
  }

  /**
   * Create and add the components.
   */
  private void initComponents() {
    ActionListener changeListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          flipTimer.restart();
        }
    };
    flipHorizontallyButton = Mnemonics.configure(new JCheckBox(Msg.msg("FlipView.flipHorizontallyLabel")));
    flipHorizontallyButton.addActionListener(changeListener);
    add(flipHorizontallyButton, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    flipVerticallyButton = Mnemonics.configure(new JCheckBox(Msg.msg("FlipView.flipVerticallyLabel")));
    flipVerticallyButton.addActionListener(changeListener);
    add(flipVerticallyButton, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    rotateTextButton = Mnemonics.configure(new JCheckBox(Msg.msg("FlipView.rotateTextLabel")));
    rotateTextButton.setSelected(true);
    rotateTextButton.addActionListener(changeListener);
    rotateTextButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          adjustTextButton.setEnabled(rotateTextButton.isSelected());
        }
    });
    add(rotateTextButton, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START,
        GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    adjustTextButton = Mnemonics.configure(new JCheckBox(Msg.msg("FlipView.adjustTextLabel")));
    adjustTextButton.setSelected(true);
    adjustTextButton.addActionListener(changeListener);
    add(adjustTextButton, new GridBagConstraints(0, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START,
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
      this, Msg.msg("FlipView.dialogTitle"), null) == JOptionPane.OK_OPTION) {

      // Apply the last change, if nessecary
      if (flipTimer.isRunning()) {
        flipTimer.stop();
        flip();
      }

      if (undoSupport != null) {
        undoSupport.postEdit(currentEdit);
      }
    } else {
      // Revert any changes
      flipTimer.stop();
      currentEdit.undo();
    }
  }

  private void flip() {
    boolean isFlipHorizontally = flipHorizontallyButton.isSelected();
    boolean isFlipVertically = flipVerticallyButton.isSelected();
    boolean isRotateText = rotateTextButton.isSelected();
    boolean isAdjustText = isRotateText && adjustTextButton.isSelected();
    float scaleX = isFlipHorizontally ? -1 : 1;
    float scaleY = isFlipVertically ? -1 : 1;
    currentEdit.transform(new AffineTransform(
          scaleX, 0, 0, scaleY,
          bounds.getCenterX() * (1 - scaleX), bounds.getCenterY() * (1 - scaleY)),
        isRotateText, isAdjustText);
  }

}
