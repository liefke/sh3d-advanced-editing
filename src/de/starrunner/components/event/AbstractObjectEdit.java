package de.starrunner.components.event;

import javax.swing.undo.*;

/**
 * Edits an object (with undo/redo).
 *
 * Copyright (c) 2010 by Tobias Liefke
 *
 * @param <T> the type of the associated object
 * 
 * @author Tobias Liefke
 */
public abstract class AbstractObjectEdit<T> extends AbstractUndoableEdit {
  private static final long serialVersionUID = -263897863724658893L;

  protected final T target;

  /**
   * Creates a new instance of an edit operation.
   *
   * @param target the edited object
   */
  public AbstractObjectEdit(T target) {
    this.target = target;
  }

  /**
   * The object of this edit operation.
   *
   * @return the associated object.
   */
  public T getTarget() {
    return target;
  }

  /**
   * Redos the current action.
   * 
   * @see AbstractUndoableEdit#redo()
   */
  @Override
  public void redo() throws CannotRedoException {
    super.redo();
    doAction();
  }

  /**
   * Undos the current action.
   * 
   * @see AbstractUndoableEdit#undo()
   */
  @Override
  public void undo() throws CannotUndoException {
    super.undo();
    undoAction();
  }

  /**
   * Executes the action (without marking it).
   * 
   * May be used after creation to execute the action the first time. 
   * Calling {@link #redo()} would throw an exception in that case,
   * as the parent class assumes, that the action had already taken place.
   */
  public abstract void doAction();

  /**
   * Reverses the action (without marking it).
   */
  public abstract void undoAction();

}
