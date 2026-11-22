package de.starrunner.sweethome3d.editing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.plugin.PluginAction;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.tools.URLContent;

import de.starrunner.components.event.AbstractObjectEdit;

/**
 * Connects the end and start point of two or more selected walls.
 *
 * Copyright (c) 2010 by Tobias Liefke
 *
 * @author Tobias Liefke
 */
public class ConnectWallsAction extends PluginAction {
	private final AdvancedEditingPlugin	plugin;
	private ResourceBundle					resource;

	/**
	 * Creates a new instance of ConnectWallsAction.
	 * 
	 * @param plugin the parent plugin
	 */
	public ConnectWallsAction(AdvancedEditingPlugin plugin) {
		this.plugin = plugin;
		this.resource = ResourceBundle.getBundle("de.starrunner.sweethome3d.editing.locales.package", Locale.getDefault(),
				this.plugin.getPluginClassLoader());
		String baseActionName = "ConnectWallsAction.";

		putPropertyValue(Property.NAME, resource.getString(baseActionName + Property.NAME));
		putPropertyValue(Property.SHORT_DESCRIPTION, resource.getString(baseActionName + Property.SHORT_DESCRIPTION));
		putPropertyValue(Property.MENU,
				this.plugin.getUserPreferences().getLocalizedString(HomePane.class, "PLAN_MENU.Name"));
		putPropertyValue(Property.SMALL_ICON, new URLContent(de.starrunner.sweethome3d.editing.AdvancedEditingPlugin.class
				.getResource("/de/starrunner/sweethome3d/editing/resources/plan-join-walls.png")));
//		putPropertyValue(Property.TOOL_BAR, true);

		// Reset panel if language changes
		this.plugin.getUserPreferences().addPropertyChangeListener(UserPreferences.Property.LANGUAGE,
				new UserChangingPreferencesListener(this.plugin, this, baseActionName));
		// Reset panel if unit changes
		this.plugin.getUserPreferences().addPropertyChangeListener(UserPreferences.Property.UNIT,
				new UserChangingPreferencesListener(this.plugin, this, baseActionName));

		// Enable only if at least two walls are selected
		plugin.getHome().addSelectionListener(new SelectionListener() {
			@Override
			public void selectionChanged(SelectionEvent selectionEvent) {
				setEnabled(isWallsSelection(selectionEvent.getSelectedItems()));
			}
		});
	}

	/**
	 * Indicates that at least two walls are selected.
	 *
	 * @param selectedItems the current selection
	 * @return true if the given set contains at least two walls
	 */
	private static boolean isWallsSelection(Collection<?> selectedItems) {
		int count = 0;
		for (Object item : selectedItems) {
			if (item instanceof Wall) { if (++count == 2) { return true; } }
		}
		return false;
	}

	/**
	 * @see PluginAction#execute()
	 */
	@Override
	public void execute() {
		// Save the state of the walls
		List<Wall> selectedWalls = Home.getWallsSubList(plugin.getHome().getSelectedItems());
		List<WallState> states = new ArrayList<WallState>(selectedWalls.size());
		for (Wall wall : selectedWalls) {
			states.add(new WallState(wall));
		}

		// Create the edit action for joining walls (and its undo)
		AbstractObjectEdit<List<WallState>> edit = new AbstractObjectEdit<List<WallState>>(states) {
			private static final long serialVersionUID = 1L;

			@Override
			public void doAction() {
				int size = getTarget().size() - 1;
				for (int i = 0; i < size; i++) {
					// Join every pair of walls
					WallState first = getTarget().get(i);
					WallState second = getTarget().get(i + 1);
					first.wall.setXEnd(second.wall.getXStart());
					first.wall.setYEnd(second.wall.getYStart());
					first.wall.setWallAtEnd(second.wall);
					second.wall.setWallAtStart(first.wall);
				}
			}

			@Override
			public void undoAction() {
				for (WallState state : getTarget()) {
					// Reset the saved state
					state.undo();
				}
			}

			@Override
			public String getPresentationName() {
				return Msg.msg("ConnectWallsAction.NAME");
			}

		};
		edit.doAction();
		if (plugin.getUndoableEditSupport() != null) { plugin.getUndoableEditSupport().postEdit(edit); }
	}

	/**
	 * Used to store the state of a wall for undo.
	 */
	private static final class WallState {
		private final Wall	wall;
		private float[]		ends;
		private Wall			wallAtStart;
		private Wall			wallAtEnd;

		/**
		 * Creates a new instance of WallState.
		 *
		 * @param wall the associated wall
		 */
		public WallState(Wall wall) {
			this.wall = wall;
			ends = new float[] { wall.getXEnd(), wall.getYEnd() };
			wallAtStart = wall.getWallAtStart();
			wallAtEnd = wall.getWallAtEnd();
		}

		/**
		 * Resets the state of the associated wall.
		 */
		public void undo() {
			wall.setWallAtStart(wallAtStart);
			wall.setWallAtEnd(wallAtEnd);
			wall.setXEnd(ends[0]);
			wall.setYEnd(ends[1]);
		}
	}
}
