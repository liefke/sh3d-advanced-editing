package de.starrunner.sweethome3d;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;

import javax.swing.*;
import javax.swing.event.*;

/**
 * Shows a preview of the current edited room.
 *
 * Copyright (c) 2010 by Tobias Liefke
 *
 * @author Tobias Liefke
 */
public class RoomPointsPreview extends JComponent implements ListDataListener {
  private static final long serialVersionUID = 3980438894958387347L;

  private JList list;
  private RoomPointsModel model;
  private Shape currentShape;
  private Rectangle2D currentBounds;
  private boolean markNextLine;
  private boolean markPreviousLine;

  /**
   * Creates a new instance of RoomPointsPreview.
   *
   * @param list the list containing the room points
   * 
   * @throws ClassCastException if the list does not contain a {@link RoomPointsModel}
   */
  public RoomPointsPreview(final JList list) {
    this.list = list;
    this.model = (RoomPointsModel) list.getModel();
    model.addListDataListener(this);
    list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        repaint();
      }
    });
    setPreferredSize(new Dimension(180, 180));
    setBorder(UIManager.getBorder("ScrollPane.border"));

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
          try {
            // First transform the point to plan coordinats
            AffineTransform transform = createTransformation();
            Point2D clickPoint = transform.inverseTransform(e.getPoint(), null);

            // Now find the clicked point
            int index = -1;
            double minDistanceSqr = 10 / transform.getScaleX(); // The maximum click distance
            minDistanceSqr *= minDistanceSqr;
            for (int i = 0; i < model.getSize(); i++) {
              float[] point = model.getPoint(i);
              double distanceSqr = Point2D.distanceSq(clickPoint.getX(), clickPoint.getY(), point[0], point[1]);
              if (distanceSqr < minDistanceSqr) {
                minDistanceSqr = distanceSqr;
                index = i;
              }
            }
            if (index >= 0) {
              list.setSelectedIndex(index);
            }
          } catch (NoninvertibleTransformException ex) {
            // Ignore an return
            return;
          }
        }
      }
    });
  }

  /**
   * @see JComponent#paintComponent(Graphics)
   */
  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (g instanceof Graphics2D && model != null) {

      // Create a new graphics object
      Graphics2D g2D = (Graphics2D) g.create();
      try {
        // Create and apply the transformation
        AffineTransform transform = createTransformation();
        g2D.transform(transform);
        double scale = transform.getScaleX();

        // Now draw the shape
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
        g2D.setColor(Color.GRAY);
        g2D.fill(currentShape);
        g2D.setPaintMode();
        g2D.setColor(getForeground());
        g2D.setStroke(new BasicStroke(1.5f / (float) scale, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2D.draw(currentShape);

        // And draw the selection
        int selectedIndex = list.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < model.getSize()) {
          float[] point = model.getPoint(selectedIndex);
          g2D.setColor(getForeground());
          g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
          g2D.fill(new Ellipse2D.Double(point[0] - 4 / scale, point[1] - 4 / scale, 8 / scale, 8 / scale));

          // And mark the incoming/outgoing lines
          g2D.setColor(Color.BLACK);
          g2D.setStroke(new BasicStroke(5f / (float) scale, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
          if (markNextLine) {
            float[] nextPoint = model.getPoint((selectedIndex + 1) % model.getSize());
            g2D.draw(new Line2D.Float(point[0], point[1], nextPoint[0], nextPoint[1]));
          }
          if (markPreviousLine) {
            float[] previousPoint = model.getPoint((selectedIndex + model.getSize() - 1) % model.getSize());
            g2D.draw(new Line2D.Float(point[0], point[1], previousPoint[0], previousPoint[1]));
          }
        }
      } finally {
        g2D.dispose();
      }
    }
  }

  /**
   * Creates the transformation of the room coordinates to the component.
   */
  private AffineTransform createTransformation() {
    // First create the shape, if nessecary
    if (currentShape == null) {
      currentShape = model.createShape();
      currentBounds = currentShape.getBounds2D();
    }

    Insets insets = getInsets();
    int width = getWidth() - (insets.left + insets.right) - 16;
    int height = getHeight() - (insets.top + insets.bottom) - 16;

    // Move to the center of the component
    AffineTransform transform = AffineTransform.getTranslateInstance(insets.left + 8 + width / 2d, insets.top + 8
        + height / 2d);

    // Now calculate the scale to fit the whole shape in the preview
    double scaleX = width / currentBounds.getWidth();
    double scaleY = height / currentBounds.getHeight();
    double scale;
    if (scaleX < scaleY) {
      scale = scaleX;
    } else {
      scale = scaleY;
    }
    transform.scale(scale, scale);

    // And position the shape
    transform.translate(-(currentBounds.getX() + currentBounds.getWidth() / 2),
      -(currentBounds.getY() + currentBounds.getHeight() / 2));
    return transform;
  }

  /**
   * Indicates that a thick line is used for the line from the selected point.
   *
   * @return true if the next line is marked in the drawing.
   */
  public boolean isMarkNextLine() {
    return markNextLine;
  }

  /**
   * Changes how the next line is displayed.
   *
   * @param markNextLine true to mark the next line in the drawing.
   */
  public void setMarkNextLine(boolean markNextLine) {
    this.markNextLine = markNextLine;
    repaint();
  }

  /**
   * Indicates that a thick line is used for the line to the selected point.
   *
   * @return true if the previous line is marked in the drawing
   */
  public boolean isMarkPreviousLine() {
    return markPreviousLine;
  }

  /**
   * Changes how the previous line is displayed.
   *
   * @param markPreviousLine true to mark the previous line in the drawing.
   */
  public void setMarkPreviousLine(boolean markPreviousLine) {
    this.markPreviousLine = markPreviousLine;
    repaint();
  }

  /**
   * @see ListDataListener#intervalAdded(ListDataEvent)
   */
  @Override
  public void intervalAdded(ListDataEvent e) {
    contentsChanged(e);
  }

  /**
   * @see ListDataListener#intervalRemoved(ListDataEvent)
   */
  @Override
  public void intervalRemoved(ListDataEvent e) {
    contentsChanged(e);
  }

  /**
   * @see ListDataListener#contentsChanged(ListDataEvent)
   */
  @Override
  public void contentsChanged(ListDataEvent e) {
    currentShape = null;
    repaint();
  }

}
