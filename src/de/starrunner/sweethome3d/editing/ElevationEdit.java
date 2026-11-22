package de.starrunner.sweethome3d.editing;

import java.util.ArrayList;
import java.util.List;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomePieceOfFurniture.SortableProperty;
import com.eteks.sweethome3d.model.Selectable;

import de.starrunner.components.event.AbstractObjectEdit;

/**
 * Elevates or distributes the elevation of selected objects.
 */
public class ElevationEdit extends AbstractObjectEdit<List<ElevationEdit.FurnitureState>> {
	private static final long			serialVersionUID	= 1L;

	private final Home					home;
	private final List<Selectable>	selectedItems;
	private final String					presentationName;

	/**
	 * Creates a new instance of the elevation edit for undo / redo.
	 *
	 * @param home the home
	 */
	public ElevationEdit(Home home) {
		this(home, Msg.msg("ElevationView.dialogTitle"));
	}

	/**
	 * Creates a new instance of the elevation edit with a custom presentation name.
	 *
	 * @param home the home
	 * @param presentationName the name shown in the Undo/Redo menu
	 */
	public ElevationEdit(Home home, String presentationName) {
		super(new ArrayList<FurnitureState>());
		this.home = home;
		this.presentationName = presentationName;
		this.selectedItems = home.getSelectedItems();

		// Collect individual pieces, unpacking groups if necessary
		for (HomePieceOfFurniture piece : Home.getFurnitureSubList(selectedItems)) {
			if (piece instanceof HomeFurnitureGroup) {
				for (HomePieceOfFurniture groupedPiece : ((HomeFurnitureGroup) piece).getAllFurniture()) {
					target.add(new FurnitureState(groupedPiece));
				}
			} else {
				target.add(new FurnitureState(piece));
			}
		}
	}

	@Override
	public void doAction() {
		for (FurnitureState state : target) {
			state.apply();
		}
		home.setSelectedItems(selectedItems);
	}

	@Override
	public void undoAction() {
		for (FurnitureState state : target) {
			state.undo();
		}
		home.setSelectedItems(selectedItems);
	}

	@Override
	public String getPresentationName() {
		return presentationName;
	}

	/**
	 * Elevates all selected furniture objects by the given amount.
	 *
	 * @param elevation the count of centimeters to elevate (or drop for negative
	 *        values)
	 */
	public void elevate(float elevation) {
		for (FurnitureState state : target) {
			state.setNewElevation(Math.max(state.initialElevation + elevation, 0));
			state.apply();
		}
	}

	/**
	 * Evenly distributes the elevation of selected furniture between the lowest and
	 * highest pieces.
	 */
	public void distributeElevation() {
		if (target.size() < 3) { return; }

		// Sort states by their initial elevation
		List<FurnitureState> sortedStates = new ArrayList<FurnitureState>(target);
		sortedStates.sort((s1, s2) -> HomePieceOfFurniture
				.getFurnitureComparator(SortableProperty.ELEVATION)
				.compare(s1.furniture, s2.furniture));

		float lowerElevation = sortedStates.get(0).initialElevation;
		float higherElevation = sortedStates.get(sortedStates.size() - 1).initialElevation;
		float step = (higherElevation - lowerElevation) / (sortedStates.size() - 1);

		for (int i = 1; i < sortedStates.size() - 1; i++) {
			sortedStates.get(i).setNewElevation(lowerElevation + step * i);
		}

		doAction();
	}

	/**
	 * Saves the initial and modified elevation state of a piece of furniture.
	 */
	public static final class FurnitureState {
		private final HomePieceOfFurniture	furniture;
		private final float						initialElevation;
		private float								newElevation;

		public FurnitureState(HomePieceOfFurniture furniture) {
			this.furniture = furniture;
			this.initialElevation = furniture.getElevation();
			this.newElevation = this.initialElevation;
		}

		public void setNewElevation(float newElevation) {
			this.newElevation = newElevation;
		}

		public void apply() {
			furniture.setElevation(newElevation);
		}

		public void undo() {
			furniture.setElevation(initialElevation);
		}
	}
}
