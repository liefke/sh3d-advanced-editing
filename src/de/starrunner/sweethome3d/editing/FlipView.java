package de.starrunner.sweethome3d.editing;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Compass;
import com.eteks.sweethome3d.model.DimensionLine;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Label;
import com.eteks.sweethome3d.model.ObserverCamera;
import com.eteks.sweethome3d.model.Polyline;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.viewcontroller.View;

import de.starrunner.util.strings.Mnemonics;

/**
 * Lets a user flip the selected objects.
 *
 * Copyright (c) 2010 by Tobias Liefke Copyright (c) 2015 by Igor A. Perminov
 *
 * @author Igor A. Perminov
 * @author Tobias Liefke
 */
public class FlipView extends ImmediateEditDialogView {
	private static final long	serialVersionUID	= 1L;

	private FlipEdit				flipEdit;
	private Rectangle2D.Float	bounds;

	private JCheckBox				copyButton;
	private JCheckBox				reverseDirectionButton;
	private JCheckBox				flipHorizontallyButton;
	private JCheckBox				flipVerticallyButton;
	private JCheckBox				rotateTextButton;
	private JCheckBox				adjustTextButton;

	/**
	 * Creates a new instance of FlipView.
	 * 
	 * @param home the application
	 * @param preferences the current configuration
	 * @param undoSupport used for undo support of the current action
	 * @param plugin
	 */
	public FlipView(Home home, UserPreferences preferences, UndoableEditSupport undoSupport, AdvancedEditingPlugin plugin) {
		super(Msg.msg("FlipView.dialogTitle") + "-" + plugin.getVersion(), home, preferences, undoSupport);
		initComponents();
	}

	/**
	 * Create and add the components.
	 */
	private void initComponents() {
		copyButton = Mnemonics.configure(new JCheckBox(Msg.msg("FlipView.copyLabel")));
		copyButton.addActionListener(createLazyActionListener());
		copyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reverseDirectionButton.setEnabled(copyButton.isSelected());
			}
		});

		reverseDirectionButton = Mnemonics.configure(new JCheckBox(Msg.msg("FlipView.reverseLabel")));
		reverseDirectionButton.setEnabled(false);
		reverseDirectionButton.addActionListener(createLazyActionListener());

		JPanel copyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		copyPanel.add(copyButton);
		copyPanel.add(reverseDirectionButton);
		add(copyPanel, new GridBagConstraints(0, 0, 2, 1, 0, 0, GridBagConstraints.LINE_START,
				GridBagConstraints.NONE, new Insets(0, 0, 4, 0), 0, 0));

		flipHorizontallyButton = Mnemonics.configure(new JCheckBox(Msg.msg("FlipView.flipHorizontallyLabel")));
		flipHorizontallyButton.addActionListener(createLazyActionListener());
		add(flipHorizontallyButton, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(new JLabel(new ImageIcon(FlipView.class.getResource("resources/plan-flip-objects.png"))),
				new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));

		flipVerticallyButton = Mnemonics.configure(new JCheckBox(Msg.msg("FlipView.flipVerticallyLabel")));
		flipVerticallyButton.addActionListener(createLazyActionListener());
		add(flipVerticallyButton, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(new JLabel(new ImageIcon(FlipView.class.getResource("resources/plan-flip-objects-vertical.png"))),
				new GridBagConstraints(1, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));

		rotateTextButton = Mnemonics.configure(new JCheckBox(Msg.msg("FlipView.rotateTextLabel")));
		rotateTextButton.setSelected(true);
		rotateTextButton.addActionListener(createLazyActionListener());
		rotateTextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				adjustTextButton.setEnabled(rotateTextButton.isSelected());
			}
		});
		add(rotateTextButton, new GridBagConstraints(0, 3, 2, 1, 0, 0, GridBagConstraints.LINE_START,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		adjustTextButton = Mnemonics.configure(new JCheckBox(Msg.msg("FlipView.adjustTextLabel")));
		adjustTextButton.setSelected(true);
		adjustTextButton.addActionListener(createLazyActionListener());
		add(adjustTextButton, new GridBagConstraints(0, 4, 2, 1, 0, 0, GridBagConstraints.LINE_START,
				GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));
	}

	/**
	 * @see ImmediateEditDialogView#displayView(View)
	 */
	@Override
	public void displayView(View parentView) {
		flipEdit = new FlipEdit(getTitle(), home);
		boolean hasSelection = flipEdit.hasValidSelection();

		copyButton.setEnabled(hasSelection && !home.isAllLevelsSelection());
		reverseDirectionButton.setEnabled(hasSelection && copyButton.isSelected());
		flipHorizontallyButton.setEnabled(hasSelection);
		flipVerticallyButton.setEnabled(hasSelection);
		rotateTextButton.setEnabled(hasSelection);
		adjustTextButton.setEnabled(hasSelection && rotateTextButton.isSelected());

		bounds = flipEdit.getBounds();

		showDialog(flipEdit);
	}

	@Override
	protected void apply() {
		if (flipEdit == null || !flipEdit.hasValidSelection()) { return; }

		boolean isCopy = copyButton.isSelected();
		boolean isReverse = reverseDirectionButton.isSelected();
		boolean isFlipHorizontally = flipHorizontallyButton.isSelected();
		boolean isFlipVertically = flipVerticallyButton.isSelected();
		boolean isRotateText = rotateTextButton.isSelected();
		boolean isAdjustText = isRotateText && adjustTextButton.isSelected();

		float scaleX = isFlipHorizontally ? -1 : 1;
		float scaleY = isFlipVertically ? -1 : 1;
		double tx = bounds.getCenterX() * (1 - scaleX);
		double ty = bounds.getCenterY() * (1 - scaleY);

		if (isCopy) {
			int directionSign = isReverse ? -1 : 1;
			if (isFlipHorizontally) { tx += directionSign * bounds.getWidth(); }
			if (isFlipVertically) { ty += directionSign * bounds.getHeight(); }
		}

		flipEdit.apply(isCopy, new AffineTransform(scaleX, 0, 0, scaleY, tx, ty), isRotateText, isAdjustText);
	}

	/**
	 * Handles both in-place flip and copy-flip with full undo/redo support.
	 */
	private static class FlipEdit extends AbstractUndoableEdit {
		private static final long			serialVersionUID	= 1L;

		private final String					presentationName;
		private final Home					home;
		private final List<Selectable>	rawSelection;
		private final List<Selectable>	validSelection;
		private final TransformEdit		originalTransformEdit;

		private boolean						copyMode;
		private List<Selectable>			copiedItems;
		private TransformEdit				copiedTransformEdit;

		public FlipEdit(String presentationName, Home home) {
			this.presentationName = presentationName;
			this.home = home;
			this.rawSelection = new ArrayList<Selectable>(home.getSelectedItems());
			this.validSelection = filterSelectableItems(rawSelection);

			if (!validSelection.isEmpty()) {
				home.setSelectedItems(validSelection);
				this.originalTransformEdit = new TransformEdit(presentationName, home);
				home.setSelectedItems(rawSelection);
			} else {
				this.originalTransformEdit = null;
			}
		}

		private static List<Selectable> filterSelectableItems(List<Selectable> items) {
			List<Selectable> filtered = new ArrayList<Selectable>(items.size());
			for (Selectable item : items) {
				if (item != null && !(item instanceof ObserverCamera) && !(item instanceof Compass)) { filtered.add(item); }
			}
			return filtered;
		}

		public boolean hasValidSelection() {
			return !validSelection.isEmpty();
		}

		/**
		 * Calculates bounds only for the selected items excluding Compass and
		 * ObserverCamera.
		 */
		public Rectangle2D.Float getBounds() {
			if (originalTransformEdit != null) { return originalTransformEdit.getBounds(); }
			return new Rectangle2D.Float(0, 0, 0, 0);
		}

		public void apply(boolean isCopy, AffineTransform transformation, boolean rotateText, boolean adjustText) {
			if (!hasValidSelection()) { return; }

			if (isCopy) {
				if (!copyMode) {
					// Reset original items in case they were flipped in place earlier
					originalTransformEdit.undoAction();

					// Duplicate filtered items (excluding Compass and ObserverCamera), add to home,
					// and select
					copiedItems = Home.duplicate(validSelection);
					addItemsToHome(copiedItems);
					home.setSelectedItems(copiedItems);
					copiedTransformEdit = new TransformEdit(presentationName, home);
					copyMode = true;
				}
				copiedTransformEdit.transform(transformation, rotateText, adjustText);
			} else {
				if (copyMode) {
					// Remove copied items and restore original selection
					removeItemsFromHome(copiedItems);
					copiedItems = null;
					copiedTransformEdit = null;
					home.setSelectedItems(rawSelection);
					copyMode = false;
				}
				originalTransformEdit.transform(transformation, rotateText, adjustText);
			}
		}

		@Override
		public String getPresentationName() {
			return presentationName;
		}

		@Override
		public void undo() throws CannotUndoException {
			super.undo();
			if (!hasValidSelection()) { return; }
			if (copyMode) {
				removeItemsFromHome(copiedItems);
				home.setSelectedItems(rawSelection);
			} else {
				originalTransformEdit.undo();
				home.setSelectedItems(rawSelection);
			}
		}

		@Override
		public void redo() throws CannotRedoException {
			super.redo();
			if (!hasValidSelection()) { return; }
			if (copyMode) {
				addItemsToHome(copiedItems);
				copiedTransformEdit.doAction();
			} else {
				originalTransformEdit.redo();
			}
		}

		private void addItemsToHome(List<Selectable> items) {
			for (Selectable item : items) {
				if (item instanceof Wall) {
					home.addWall((Wall) item);
				} else if (item instanceof Room) {
					home.addRoom((Room) item);
				} else if (item instanceof Polyline) {
					home.addPolyline((Polyline) item);
				} else if (item instanceof DimensionLine) {
					home.addDimensionLine((DimensionLine) item);
				} else if (item instanceof Label) {
					home.addLabel((Label) item);
				} else if (item instanceof HomePieceOfFurniture) { home.addPieceOfFurniture((HomePieceOfFurniture) item); }
			}
		}

		private void removeItemsFromHome(List<Selectable> items) {
			for (Selectable item : items) {
				if (item instanceof Wall) {
					home.deleteWall((Wall) item);
				} else if (item instanceof Room) {
					home.deleteRoom((Room) item);
				} else if (item instanceof Polyline) {
					home.deletePolyline((Polyline) item);
				} else if (item instanceof DimensionLine) {
					home.deleteDimensionLine((DimensionLine) item);
				} else if (item instanceof Label) {
					home.deleteLabel((Label) item);
				} else if (item instanceof HomePieceOfFurniture) { home.deletePieceOfFurniture((HomePieceOfFurniture) item); }
			}
		}
	}
}
