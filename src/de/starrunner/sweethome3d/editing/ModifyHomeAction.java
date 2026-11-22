package de.starrunner.sweethome3d.editing;

import java.util.Locale;
import java.util.ResourceBundle;

import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Compass;
import com.eteks.sweethome3d.model.DimensionLine;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Label;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.Polyline;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.plugin.PluginAction;
import com.eteks.sweethome3d.swing.HomePane;
import com.eteks.sweethome3d.tools.URLContent;

public class ModifyHomeAction extends PluginAction {
	private final AdvancedEditingPlugin	plugin;
	private ResourceBundle					resource;

	public ModifyHomeAction(AdvancedEditingPlugin plugin) {
		this.plugin = plugin;
		this.resource = ResourceBundle.getBundle("de.starrunner.sweethome3d.editing.locales.package", Locale.getDefault(),
				this.plugin.getPluginClassLoader());
		String baseActionName = "ModifyHomeAction.";

		putPropertyValue(Property.NAME, resource.getString(baseActionName + Property.NAME));
		putPropertyValue(Property.SHORT_DESCRIPTION, resource.getString(baseActionName + Property.SHORT_DESCRIPTION));
		putPropertyValue(Property.MENU,
				this.plugin.getUserPreferences().getLocalizedString(HomePane.class, "PLAN_MENU.Name"));
		putPropertyValue(Property.SMALL_ICON, new URLContent(AdvancedEditingPlugin.class
				.getResource("/de/starrunner/sweethome3d/editing/resources/plan-rotate-objects.png")));

		this.plugin.getUserPreferences().addPropertyChangeListener(UserPreferences.Property.LANGUAGE,
				new UserChangingPreferencesListener(this.plugin, this, baseActionName));
		this.plugin.getUserPreferences().addPropertyChangeListener(UserPreferences.Property.UNIT,
				new UserChangingPreferencesListener(this.plugin, this, baseActionName));

		Home home = this.plugin.getHome();

		home.addSelectionListener(new SelectionListener() {
			@Override
			public void selectionChanged(SelectionEvent selectionEvent) {
				updateEnabledState();
			}
		});

		// Re-evaluate when plan elements are added or removed
		home.addWallsListener(new CollectionListener<Wall>() {
			@Override
			public void collectionChanged(CollectionEvent<Wall> ev) {
				updateEnabledState();
			}
		});
		home.addRoomsListener(new CollectionListener<Room>() {
			@Override
			public void collectionChanged(CollectionEvent<Room> ev) {
				updateEnabledState();
			}
		});
		home.addFurnitureListener(new CollectionListener<HomePieceOfFurniture>() {
			@Override
			public void collectionChanged(CollectionEvent<HomePieceOfFurniture> ev) {
				updateEnabledState();
			}
		});
		home.addPolylinesListener(new CollectionListener<Polyline>() {
			@Override
			public void collectionChanged(CollectionEvent<Polyline> ev) {
				updateEnabledState();
			}
		});
		home.addDimensionLinesListener(new CollectionListener<DimensionLine>() {
			@Override
			public void collectionChanged(CollectionEvent<DimensionLine> ev) {
				updateEnabledState();
			}
		});
		home.addLabelsListener(new CollectionListener<Label>() {
			@Override
			public void collectionChanged(CollectionEvent<Label> ev) {
				updateEnabledState();
			}
		});

		updateEnabledState();
	}

	private void updateEnabledState() {
		Home home = plugin.getHome();
		boolean hasValidElements = !home.getWalls().isEmpty()
				|| !home.getRooms().isEmpty()
				|| !home.getFurniture().isEmpty()
				|| !home.getPolylines().isEmpty()
				|| !home.getDimensionLines().isEmpty()
				|| !home.getLabels().isEmpty();

		boolean hasSelectableSelected = false;
		for (Selectable item : home.getSelectedItems()) {
			if (!(item instanceof Compass) && !(item instanceof Level)) {
				hasSelectableSelected = true;
				break;
			}
		}

		setEnabled(hasValidElements && !hasSelectableSelected);
	}

	@Override
	public void execute() {
		new ModifyHomeView(plugin.getHome(), plugin.getUserPreferences(), plugin.getUndoableEditSupport(), plugin).displayView(null);
	}
}
