package de.starrunner.sweethome3d.editing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.tools.TemporaryURLContent;

import de.starrunner.components.event.AbstractObjectEdit;
import de.starrunner.sweethome3d.editing.BackgroundImageEdit.ObjectBackgroundState;

public class BackgroundImageEdit extends AbstractObjectEdit<List<ObjectBackgroundState<? extends BackgroundImage>>> {
	private static final long	serialVersionUID	= 1L;

	private final String			presentationName;
	private AffineTransform		currentTransform;

	/**
	 * Creates a new BackgroundImageEdit.
	 *
	 * @param presentationName the undo/redo label
	 * @param home the home instance
	 * @param allLevels true to target all levels and home background (Modify Home),
	 *        false to target only the currently active level/home background
	 *        (Modify Background)
	 */
	public BackgroundImageEdit(String presentationName, Home home, boolean allLevels) {
		super(new ArrayList<ObjectBackgroundState<? extends BackgroundImage>>());
		this.presentationName = presentationName;

		if (allLevels) {
			if (home.getBackgroundImage() != null) { target.add(new BackgroundState(home.getBackgroundImage(), home, null)); }
			if (home.getLevels() != null) {
				for (Level level : home.getLevels()) {
					if (level.getBackgroundImage() != null) { target.add(new BackgroundState(level.getBackgroundImage(), home, level)); }
				}
			}
		} else {
			Level selectedLevel = home.getSelectedLevel();
			if (selectedLevel != null && selectedLevel.getBackgroundImage() != null) {
				target.add(new BackgroundState(selectedLevel.getBackgroundImage(), home, selectedLevel));
			} else if (home.getBackgroundImage() != null) { target.add(new BackgroundState(home.getBackgroundImage(), home, null)); }
		}

		this.currentTransform = new AffineTransform();
	}

	@Override
	public String getPresentationName() {
		return presentationName;
	}

	@Override
	public void doAction() {
		transform();
	}

	@Override
	public void undoAction() {
		for (ObjectBackgroundState<? extends BackgroundImage> state : target) {
			state.reset();
		}
	}

	private void transform() {
		for (ObjectBackgroundState<? extends BackgroundImage> state : target) {
			state.transform(currentTransform);
		}
	}

	/**
	 * Applies a transformation to the targeted background image(s).
	 *
	 * @param transform the affine transformation in plan coordinates (cm)
	 */
	public void transform(AffineTransform transform) {
		this.currentTransform = new AffineTransform(transform);
		transform();
	}

	/**
	 * Resolves the combined bounds in cm of all targeted background images,
	 * accounting for their existing scale and origin offset.
	 */
	public Rectangle2D.Float getBounds() {
		float left = Float.MAX_VALUE;
		float top = Float.MAX_VALUE;
		float right = -Float.MAX_VALUE;
		float bottom = -Float.MAX_VALUE;

		for (ObjectBackgroundState<? extends BackgroundImage> state : target) {
			Rectangle2D b = state.getInitialBounds();
			if (b.getWidth() > 0 && b.getHeight() > 0) {
				if (b.getMinX() < left) { left = (float) b.getMinX(); }
				if (b.getMaxX() > right) { right = (float) b.getMaxX(); }
				if (b.getMinY() < top) { top = (float) b.getMinY(); }
				if (b.getMaxY() > bottom) { bottom = (float) b.getMaxY(); }
			}
		}

		if (left > right || top > bottom) { return new Rectangle2D.Float(0, 0, 0, 0); }
		return new Rectangle2D.Float(left, top, right - left, bottom - top);
	}

	public abstract static class ObjectBackgroundState<T extends BackgroundImage> {
		protected final T object;

		public ObjectBackgroundState(T object) {
			this.object = object;
		}

		public abstract Rectangle2D getInitialBounds();

		public abstract void reset();

		public abstract void transform(AffineTransform worldTransform);
	}

	private static final class BackgroundState extends ObjectBackgroundState<BackgroundImage> {
		private final Home					home;
		private final Level					imageLevel;
		private final BackgroundImage		initialBackground;
		private final BufferedImage		initialBufferedImage;
		private final Rectangle2D.Float	initialBounds;

		public BackgroundState(BackgroundImage background, Home home, Level level) {
			super(background);
			this.home = home;
			this.imageLevel = level;
			this.initialBackground = background;
			this.initialBufferedImage = readImage(background.getImage());

			// xOrigin and yOrigin are already stored in plan units (cm)
			float scale = background.getScale();
			float minX = -background.getXOrigin();
			float minY = -background.getYOrigin();
			float width = initialBufferedImage.getWidth() * scale;
			float height = initialBufferedImage.getHeight() * scale;
			this.initialBounds = new Rectangle2D.Float(minX, minY, width, height);
		}

		private static BufferedImage readImage(Content imageContent) {
			try (InputStream contentStream = imageContent.openStream()) {
				BufferedImage image = ImageIO.read(contentStream);
				return image != null ? image : new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
			} catch (IOException ex) {
				return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
			}
		}

		@Override
		public Rectangle2D getInitialBounds() {
			return initialBounds;
		}

		@Override
		public void reset() {
			if (this.imageLevel != null) {
				this.imageLevel.setBackgroundImage(this.initialBackground);
			} else {
				this.home.setBackgroundImage(this.initialBackground);
			}
		}

		@Override
		public void transform(AffineTransform worldTx) {
			int srcW = initialBufferedImage.getWidth();
			int srcH = initialBufferedImage.getHeight();
			float baseScale = initialBackground.getScale();

			// 1. Map source image pixels to existing plan coordinates (cm):
			// planPoint = pixel * baseScale - originInCm
			AffineTransform pixelToPlan = new AffineTransform();
			pixelToPlan.translate(-initialBackground.getXOrigin(), -initialBackground.getYOrigin());
			pixelToPlan.scale(baseScale, baseScale);

			// 2. Apply the world transformation in plan coordinates (cm)
			AffineTransform fullPlanTx = new AffineTransform(worldTx);
			fullPlanTx.concatenate(pixelToPlan);

			// 3. Compute the axis-aligned bounding box in plan coordinates (cm)
			Point2D[] corners = new Point2D[] {
					new Point2D.Double(0, 0),
					new Point2D.Double(srcW, 0),
					new Point2D.Double(srcW, srcH),
					new Point2D.Double(0, srcH)
			};

			double minPlanX = Double.POSITIVE_INFINITY;
			double minPlanY = Double.POSITIVE_INFINITY;
			double maxPlanX = Double.NEGATIVE_INFINITY;
			double maxPlanY = Double.NEGATIVE_INFINITY;

			for (Point2D corner : corners) {
				Point2D dst = fullPlanTx.transform(corner, null);
				if (dst.getX() < minPlanX) minPlanX = dst.getX();
				if (dst.getY() < minPlanY) minPlanY = dst.getY();
				if (dst.getX() > maxPlanX) maxPlanX = dst.getX();
				if (dst.getY() > maxPlanY) maxPlanY = dst.getY();
			}

			double planWidth = Math.max(baseScale, maxPlanX - minPlanX);
			double planHeight = Math.max(baseScale, maxPlanY - minPlanY);

			// 4. Determine output pixel dimensions and scale (clamped to 4096px max)
			int outW = Math.max(1, (int) Math.round(planWidth / baseScale));
			int outH = Math.max(1, (int) Math.round(planHeight / baseScale));
			int maxDim = Math.max(outW, outH);
			float newScale = baseScale;
			if (maxDim > 4096) {
				double factor = 4096.0 / maxDim;
				outW = Math.max(1, (int) Math.round(outW * factor));
				outH = Math.max(1, (int) Math.round(outH * factor));
				newScale = (float) (planWidth / outW);
			}

			// 5. Build pixel-to-pixel render transform
			AffineTransform renderTx = new AffineTransform();
			renderTx.scale(1.0 / newScale, 1.0 / newScale);
			renderTx.translate(-minPlanX, -minPlanY);
			renderTx.concatenate(fullPlanTx);

			// 6. Render transformed image onto solid white canvas
			BufferedImage outImage = new BufferedImage(outW, outH, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = outImage.createGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2d.setColor(Color.WHITE);
			g2d.fillRect(0, 0, outW, outH);
			g2d.drawImage(initialBufferedImage, renderTx, null);
			g2d.dispose();

			try {
				File tempFile = File.createTempFile("sh3d_bg_", ".png");
				tempFile.deleteOnExit();
				ImageIO.write(outImage, "png", tempFile);
				Content newContent = new TemporaryURLContent(tempFile.toURI().toURL());

				// New origin in cm so that (-newXOrigin, -newYOrigin) == (minPlanX, minPlanY)
				float newXOrigin = (float) (-minPlanX);
				float newYOrigin = (float) (-minPlanY);
				float referencePixels = 100f;
				float newScaleDistance = referencePixels * newScale;

				BackgroundImage newBg = new BackgroundImage(
						newContent,
						newScaleDistance,
						0f, 0f,
						referencePixels, 0f,
						newXOrigin,
						newYOrigin,
						initialBackground.isVisible());

				if (this.imageLevel != null) {
					this.imageLevel.setBackgroundImage(newBg);
				} else {
					this.home.setBackgroundImage(newBg);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}
