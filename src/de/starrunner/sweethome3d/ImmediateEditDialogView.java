package de.starrunner.sweethome3d;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.SwingTools;
import com.eteks.sweethome3d.viewcontroller.DialogView;

/**
 * Base class for all dialogs that show any change immediately in the model.
 *
 * Copyright (c) 2015 by Tobias Liefke
 *
 * @author Tobias Liefke
 */
public abstract class ImmediateEditDialogView extends JPanel implements DialogView {

  private static final long serialVersionUID = 4997133191205357484L;

  protected final Home home;
  protected final UserPreferences preferences;
  protected final UndoableEditSupport undoSupport;

  private final Timer timer;

  private JComponent initialFocusedComponent;

  private String title;

  /**
   * Creates a new instance of a ImmediateEditDialogView.
   * 
   * @param title the title of the dialog
   * @param home the application
   * @param preferences the current configuration
   * @param undoSupport used for undo support of the current action
   */
  public ImmediateEditDialogView(String title, Home home, UserPreferences preferences, UndoableEditSupport undoSupport) {
    super(new GridBagLayout());
    this.title = title;
    this.home = home;
    this.preferences = preferences;
    this.undoSupport = undoSupport;

    // Initializes the timer responsible for applying the changes to the home,
    // to have a small timeout before changes are applied, which results in better user experience.
    timer = new Timer(300, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        apply();
      }
    });
    timer.setRepeats(false);
  }

  /**
   * Displays the dialog and applies or undos the given edit when finished.
   * 
   * @param edit the edit to post or undo
   */
  protected void showDialog(UndoableEdit edit) {
    Window parentWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
    if (SwingTools.showConfirmDialog(parentWindow instanceof JFrame ? ((JFrame) parentWindow).getRootPane() : null,
      this, getTitle(), getInitialFocusedComponent()) == JOptionPane.OK_OPTION) {

      // Apply the last change, if nessecary
      if (timer.isRunning()) {
        timer.stop();
        apply();
      }

      // Post the edit
      if (undoSupport != null) {
        undoSupport.postEdit(edit);
      }
    } else {
      // Revert any changes
      timer.stop();
      edit.undo();
    }
  }

  /**
   * The component to focus when the dialog opens.
   *
   * @return the component or {@code null} if none is focused
   */
  protected JComponent getInitialFocusedComponent() {
    return initialFocusedComponent;
  }

  /**
   * Sets the component to focus when the dialog opens.
   *
   * @param initialFocusedComponent The component or {@code null} to focus nothing
   */
  public void setInitialFocusedComponent(JComponent initialFocusedComponent) {
    this.initialFocusedComponent = initialFocusedComponent;
  }

  /**
   * The title of the dialog.
   *
   * @return The dialog title.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the title to use for the dialog.
   * 
   * Only applies to new dialogs.
   *
   * @param title The new dialog title.
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /** 
   * Applies all options from the UI to the current home.
   */
  protected abstract void apply();

  /**
   * Starts the timer that calls {@link #apply()} eventually after a short period.
   */
  protected void applyLazy() {
    timer.restart();
  }

  /**
   * Creates an {@code AcionListener} that waits a small amount of time and calls {@link #apply()} eventually.
   *
   * @return the action listener
   */
  protected ActionListener createLazyActionListener() {
    return new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        applyLazy();
      }
    };
  }

  /**
   * Creates an {@code ChangeListener} that waits a small amount of time and calls {@link #apply()} eventually.
   *
   * @return the change listener
   */
  protected ChangeListener createLazyChangeListener() {
    return new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        applyLazy();
      }
    };
  }

}