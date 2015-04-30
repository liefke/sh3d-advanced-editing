package de.starrunner.components.event;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.event.*;

/**
 * Used to find out if a change was initiated by the user 
 * or is the result of a programmatic change.
 *
 * Copyright (c) 2010 by Tobias Liefke
 *
 * @author Tobias Liefke
 */
public class ChangeState {
  private boolean changing;

  /**
   * Indicates that the current change was not started by a user.
   *
   * @return true if {@link #start} was called last, false if {@link #stop} was called last
   */
  public boolean isChanging() {
    return changing;
  }

  /**
   * Called when a change of user starts and all following changes should be ignored.
   */
  public void start() {
    changing = true;
  }

  /**
   * Called to stop the reaction to a user change.
   */
  public void stop() {
    changing = false;
  }

  /**
   * Wraps an existing change listener.
   *
   * @param listener the listener that is only interested in changes started by the user
   * @return the listener to use for components
   */
  public ChangeListener wrap(final ChangeListener listener) {
    return new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        if (!changing) {
          try {
            changing = true;
            listener.stateChanged(e);
          } finally {
            changing = false;
          }
        }
      }
    };
  }

  /**
   * Wraps an existing item listener.
   *
   * @param listener the listener that is only interested in selections started by the user
   * @return the listener to use for components
   */
  public ItemListener wrap(final ItemListener listener) {
    return new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (!changing) {
          try {
            changing = true;
            listener.itemStateChanged(e);
          } finally {
            changing = false;
          }
        }
      }
    };
  }

  /**
   * Wraps an existing list selection listener.
   *
   * @param listener the listener that is only interested in selections started by the user
   * @return the listener to use for components
   */
  public ListSelectionListener wrap(final ListSelectionListener listener) {
    return new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        if (!changing) {
          try {
            changing = true;
            listener.valueChanged(e);
          } finally {
            changing = false;
          }
        }

      }
    };
  }

  /**
   * Wraps an existing tree selection listener.
   *
   * @param listener the listener that is only interested in selections started by the user
   * @return the listener to use for components
   */
  public TreeSelectionListener wrap(final TreeSelectionListener listener) {
    return new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        if (!changing) {
          try {
            changing = true;
            listener.valueChanged(e);
          } finally {
            changing = false;
          }
        }
      }
    };
  }

}
