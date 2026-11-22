package de.starrunner.sweethome3d.editing;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.AutoCommitSpinner;
import com.eteks.sweethome3d.swing.NullableSpinner.NullableSpinnerLengthModel;
import com.eteks.sweethome3d.swing.NullableSpinner.NullableSpinnerNumberModel;
import com.eteks.sweethome3d.viewcontroller.View;

import de.starrunner.util.strings.Mnemonics;

public class ModifyHomeView extends ImmediateEditDialogView {
	private static final long				serialVersionUID	= 1L;

	private TransformEdit					currentEdit;
	private BackgroundImageEdit			backgroundEdit;
	private Rectangle2D.Float				bounds;

	private NullableSpinnerNumberModel	angleModel;
	private JCheckBox							rotateTextButton;
	private JCheckBox							adjustTextButton;

	private NullableSpinnerLengthModel	xModel;
	private NullableSpinnerLengthModel	yModel;

	public ModifyHomeView(Home home, UserPreferences preferences, UndoableEditSupport undoSupport, AdvancedEditingPlugin plugin) {
		super(Msg.msg("ModifyHomeView.dialogTitle") + "-" + plugin.getVersion(), home, preferences, undoSupport);
		initComponents();
	}

	private void initComponents() {
		String unitName = preferences.getLengthUnit().getName();

		JLabel angleLabel = new JLabel(Msg.msg("RotateView.angleLabel"));
		add(angleLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
		angleModel = new NullableSpinnerNumberModel(0f, -360f, 360f, 0.5f);
		angleModel.addChangeListener(createLazyChangeListener());
		final JSpinner angleSpinner = new AutoCommitSpinner(angleModel);
		add(angleSpinner, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		Mnemonics.configure(angleLabel, angleSpinner);

		rotateTextButton = Mnemonics.configure(new JCheckBox(Msg.msg("RotateView.rotateTextLabel")));
		rotateTextButton.setSelected(true);
		rotateTextButton.addActionListener(createLazyActionListener());
		rotateTextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				adjustTextButton.setEnabled(rotateTextButton.isSelected());
			}
		});
		add(rotateTextButton, new GridBagConstraints(0, 1, 2, 1, 0, 0, GridBagConstraints.LINE_START,
				GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));

		adjustTextButton = Mnemonics.configure(new JCheckBox(Msg.msg("RotateView.adjustTextLabel")));
		adjustTextButton.setSelected(true);
		adjustTextButton.addActionListener(createLazyActionListener());
		add(adjustTextButton, new GridBagConstraints(0, 2, 2, 1, 0, 0, GridBagConstraints.LINE_START,
				GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));

		// Horizontal part of the move
		JLabel xLabel = new JLabel(Msg.msg("MoveView.xLabel", unitName));
		add(xLabel, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
		xModel = new NullableSpinnerLengthModel(preferences, 0, -100000f, 100000f);
		xModel.addChangeListener(createLazyChangeListener());
		final JSpinner xSpinner = new AutoCommitSpinner(xModel);
		add(xSpinner, new GridBagConstraints(3, 1, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.NONE,
				new Insets(5, 0, 5, 5), 0, 0));
		Mnemonics.configure(xLabel, xSpinner);

		// Vertical part of the move
		JLabel yLabel = new JLabel(Msg.msg("MoveView.yLabel", unitName));
		add(yLabel, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
		yModel = new NullableSpinnerLengthModel(preferences, 0, -100000f, 100000f);
		yModel.addChangeListener(createLazyChangeListener());
		final JSpinner ySpinner = new AutoCommitSpinner(yModel);
		add(ySpinner, new GridBagConstraints(3, 2, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END, GridBagConstraints.NONE,
				new Insets(5, 0, 5, 5), 0, 0));
		Mnemonics.configure(yLabel, ySpinner);
	}

	@Override
	public void displayView(View parentView) {
		currentEdit = new TransformEdit(getTitle(), home);
		backgroundEdit = new BackgroundImageEdit(getTitle(), home, true);

		// Use drawn elements' bounds as the shared center of rotation;
		// fall back to background bounds only if the plan has no drawn elements
		Rectangle2D.Float itemBounds = currentEdit.getBounds();
		if (hasPlanElements() && (itemBounds.getWidth() > 0 || itemBounds.getHeight() > 0)) {
			bounds = itemBounds;
		} else {
			bounds = backgroundEdit.getBounds();
		}

		showDialog(null);
	}

	@Override
	protected void showDialog(UndoableEdit ignored) {
		java.awt.Window parentWindow = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
		if (com.eteks.sweethome3d.swing.SwingTools.showConfirmDialog(
				parentWindow instanceof javax.swing.JFrame ? ((javax.swing.JFrame) parentWindow).getRootPane() : null,
				this, getTitle(), getInitialFocusedComponent()) == javax.swing.JOptionPane.OK_OPTION) {
			if (undoSupport != null) {
				final String title = getTitle();
				undoSupport.beginUpdate();
				undoSupport.postEdit(currentEdit);
				undoSupport.postEdit(backgroundEdit);
				undoSupport.postEdit(new javax.swing.undo.AbstractUndoableEdit() {
					private static final long serialVersionUID = 1L;

					@Override
					public String getPresentationName() {
						return title;
					}
				});
				undoSupport.endUpdate();
			}
		} else {
			backgroundEdit.undo();
			currentEdit.undo();
		}
	}

	private boolean hasPlanElements() {
		return !home.getWalls().isEmpty()
				|| !home.getRooms().isEmpty()
				|| !home.getFurniture().isEmpty()
				|| !home.getPolylines().isEmpty()
				|| !home.getDimensionLines().isEmpty()
				|| !home.getLabels().isEmpty();
	}

	@Override
	protected void apply() {
		Float x = xModel.getLength();
		Float y = yModel.getLength();
		float dX = x != null ? preferences.getLengthUnit().unitToCentimeter(x) : 0f;
		float dY = y != null ? preferences.getLengthUnit().unitToCentimeter(y) : 0f;
		float centerX = (float) bounds.getCenterX();
		float centerY = (float) bounds.getCenterY();
		Number angle = angleModel.getNumber();
		double rad = angle != null ? Math.toRadians(angle.doubleValue()) : 0.0;
		boolean rotateText = rotateTextButton.isSelected();

		AffineTransform transform = AffineTransform.getRotateInstance(rad, centerX, centerY);
		transform.preConcatenate(AffineTransform.getTranslateInstance(dX, dY));

		currentEdit.transform(transform, rotateText, rotateText && adjustTextButton.isSelected());
		backgroundEdit.transform(transform);
	}
}
