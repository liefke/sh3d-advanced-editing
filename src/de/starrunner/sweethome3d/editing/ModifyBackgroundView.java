package de.starrunner.sweethome3d.editing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.UndoableEditSupport;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.swing.AutoCommitSpinner;
import com.eteks.sweethome3d.swing.NullableSpinner.NullableSpinnerLengthModel;
import com.eteks.sweethome3d.swing.NullableSpinner.NullableSpinnerNumberModel;
import com.eteks.sweethome3d.viewcontroller.View;

import de.starrunner.components.event.ChangeState;
import de.starrunner.util.strings.Mnemonics;

public class ModifyBackgroundView extends ImmediateEditDialogView {
	private static final long				serialVersionUID	= 1L;

	private BackgroundImageEdit			backgroundEdit;
	private ChangeState						changeState			= new ChangeState();
	private NullableSpinnerNumberModel	angleModel;
	private NullableSpinnerNumberModel	scaleXModel;
	private NullableSpinnerNumberModel	scaleYModel;
	private JToggleButton					keepProportionsButton;

	private double								aspectRatio			= 1.0;

	private NullableSpinnerLengthModel	xModel;
	private NullableSpinnerLengthModel	yModel;

	private Rectangle2D.Float				bounds;

	public ModifyBackgroundView(Home home, UserPreferences preferences, UndoableEditSupport undoSupport, AdvancedEditingPlugin plugin) {
		super(Msg.msg("ModifyBackgroundView.dialogTitle") + "-" + plugin.getVersion(), home, preferences, undoSupport);
		initComponents();
	}

	private void initComponents() {
		String unitName = preferences.getLengthUnit().getName();

		// Rotate
		JLabel angleLabel = new JLabel(Msg.msg("RotateView.angleLabel"));
		add(angleLabel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END,
				GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
		angleModel = new NullableSpinnerNumberModel(0f, -360f, 360f, 0.5f);
		angleModel.addChangeListener(createLazyChangeListener());
		final JSpinner angleSpinner = new AutoCommitSpinner(angleModel);
		add(angleSpinner, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START,
				GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));
		Mnemonics.configure(angleLabel, angleSpinner);

		// Scale X (%)
		JLabel scaleXLabel = new JLabel(Msg.msg("ModifyBackgroundView.scaleXLabel"));
		add(scaleXLabel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END,
				GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
		scaleXModel = new NullableSpinnerNumberModel(100f, 1f, 1000f, 1f);
		final JSpinner scaleXSpinner = new AutoCommitSpinner(scaleXModel);
		add(scaleXSpinner, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START,
				GridBagConstraints.NONE, new Insets(0, 0, 5, 0), 0, 0));
		Mnemonics.configure(scaleXLabel, scaleXSpinner);

		// Scale Y (%)
		JLabel scaleYLabel = new JLabel(Msg.msg("ModifyBackgroundView.scaleYLabel"));
		add(scaleYLabel, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_END,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
		scaleYModel = new NullableSpinnerNumberModel(100f, 1f, 1000f, 1f);
		final JSpinner scaleYSpinner = new AutoCommitSpinner(scaleYModel);
		add(scaleYSpinner, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		Mnemonics.configure(scaleYLabel, scaleYSpinner);

		// Keep proportions toggle button spanning Scale X & Scale Y rows
		keepProportionsButton = new JToggleButton(new ChainLinkIcon(true));
		keepProportionsButton.setSelectedIcon(new ChainLinkIcon(true));
		keepProportionsButton.setIcon(new ChainLinkIcon(false));
		keepProportionsButton.setSelected(true);
		keepProportionsButton.setMargin(new Insets(2, 4, 2, 4));
		keepProportionsButton.setToolTipText(Msg.msg("ModifyBackgroundView.proportions"));
		keepProportionsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (keepProportionsButton.isSelected()) {
					double sx = scaleXModel.getNumber().doubleValue();
					double sy = scaleYModel.getNumber().doubleValue();
					aspectRatio = sy != 0 ? sx / sy : 1.0;
				}
			}
		});
		add(keepProportionsButton, new GridBagConstraints(2, 1, 1, 2, 0.0, 0.0, GridBagConstraints.LINE_START,
				GridBagConstraints.VERTICAL, new Insets(0, 4, 0, 0), 0, 0));

		// Synchronize Scale X and Scale Y when proportions are locked
		scaleXModel.addChangeListener(changeState.wrap(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (keepProportionsButton.isSelected() && scaleXModel.getNumber() != null) {
					float newScaleY = (float) (scaleXModel.getNumber().doubleValue() / aspectRatio);
					scaleYModel.setValue(Math.max(1f, Math.min(1000f, newScaleY)));
				}
				applyLazy();
			}
		}));

		scaleYModel.addChangeListener(changeState.wrap(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (keepProportionsButton.isSelected() && scaleYModel.getNumber() != null) {
					float newScaleX = (float) (scaleYModel.getNumber().doubleValue() * aspectRatio);
					scaleXModel.setValue(Math.max(1f, Math.min(1000f, newScaleX)));
				}
				applyLazy();
			}
		}));

		// Move X
		JLabel xLabel = new JLabel(Msg.msg("MoveView.xLabel", unitName));
		add(xLabel, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
				new Insets(0, 15, 5, 5), 0, 0));
		xModel = new NullableSpinnerLengthModel(preferences, 0, -100000f, 100000f);
		xModel.addChangeListener(createLazyChangeListener());
		final JSpinner xSpinner = new AutoCommitSpinner(xModel);
		add(xSpinner, new GridBagConstraints(4, 0, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
				new Insets(0, 0, 5, 5), 0, 0));
		Mnemonics.configure(xLabel, xSpinner);

		// Move Y
		JLabel yLabel = new JLabel(Msg.msg("MoveView.yLabel", unitName));
		add(yLabel, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
				new Insets(0, 15, 5, 5), 0, 0));
		yModel = new NullableSpinnerLengthModel(preferences, 0, -100000f, 100000f);
		yModel.addChangeListener(createLazyChangeListener());
		final JSpinner ySpinner = new AutoCommitSpinner(yModel);
		add(ySpinner, new GridBagConstraints(4, 1, 1, 1, 1.0, 0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
				new Insets(0, 0, 5, 5), 0, 0));
		Mnemonics.configure(yLabel, ySpinner);
	}

	@Override
	public void displayView(View parentView) {
		changeState.start();
		backgroundEdit = new BackgroundImageEdit(getTitle(), home, false);
		bounds = backgroundEdit.getBounds();
		aspectRatio = 1.0;
		changeState.stop();

		showDialog(backgroundEdit);
	}

	@Override
	protected void apply() {
		if (changeState.isChanging()) { return; }
		Float x = xModel.getLength();
		Float y = yModel.getLength();
		float dX = x != null ? preferences.getLengthUnit().unitToCentimeter(x) : 0f;
		float dY = y != null ? preferences.getLengthUnit().unitToCentimeter(y) : 0f;

		Number angle = angleModel.getNumber();
		double rad = angle != null ? Math.toRadians(angle.doubleValue()) : 0.0;

		Number sxNum = scaleXModel.getNumber();
		Number syNum = scaleYModel.getNumber();
		double scaleX = sxNum != null ? sxNum.doubleValue() / 100.0 : 1.0;
		double scaleY = syNum != null ? syNum.doubleValue() / 100.0 : 1.0;

		double centerX = bounds.getCenterX();
		double centerY = bounds.getCenterY();

		AffineTransform transform = new AffineTransform();
		transform.translate(centerX + dX, centerY + dY);
		transform.rotate(rad);
		transform.scale(scaleX, scaleY);
		transform.translate(-centerX, -centerY);

		backgroundEdit.transform(transform);
	}

	/**
	 * Vector chain-link icon indicating locked or unlocked X/Y proportions.
	 */
	private static class ChainLinkIcon implements Icon {
		private final boolean locked;

		public ChainLinkIcon(boolean locked) {
			this.locked = locked;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setStroke(new BasicStroke(1.5f));
			g2.setColor(locked ? new Color(40, 90, 170) : Color.GRAY);

			// Top and bottom bracket lines connecting Scale X and Scale Y
			g2.drawLine(x + 2, y + 3, x + 7, y + 3);
			g2.drawLine(x + 7, y + 3, x + 7, y + 11);
			g2.drawLine(x + 7, y + 21, x + 7, y + 29);
			g2.drawLine(x + 2, y + 29, x + 7, y + 29);

			// Center chain links
			g2.drawRoundRect(x + 4, y + 9, 6, 8, 4, 4);
			if (locked) {
				g2.drawRoundRect(x + 4, y + 15, 6, 8, 4, 4);
			} else {
				g2.drawRoundRect(x + 4, y + 17, 6, 8, 4, 4);
			}
			g2.dispose();
		}

		@Override
		public int getIconWidth() {
			return 14;
		}

		@Override
		public int getIconHeight() {
			return 32;
		}
	}
}
