package hageldave.imagingkit.core.interaction;

import hageldave.imagingkit.core.util.ImagePanel;

import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.Arrays;

public class MouseFocusedZooming extends PanelInteraction {
    final protected ImagePanel imagePanel;

    public MouseFocusedZooming(ImagePanel imagePanel) {
        this.imagePanel = imagePanel;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        super.mouseWheelMoved(e);
        if (pressedKeycode == KeyEvent.VK_SHIFT) {
//			This zooms by the center of the panel
            try {
                this.imagePanel.appendZoomAffineTransform(this.updateAffineTransform(e, Math.pow(1.7, e.getWheelRotation() * 0.1)));
            } catch (NoninvertibleTransformException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public AffineTransform updateAffineTransform(MouseWheelEvent e, double zoom) throws NoninvertibleTransformException {
        double x = e.getX();
        double y = e.getY();
        Point2D.Double point = new Point2D.Double(x, y);
        point = (Point2D.Double) imagePanel.getZoomAffineTransform().inverseTransform(point, point);

        AffineTransform zoomAT = new AffineTransform();
        zoomAT.translate(point.getX(), point.getY());
        zoomAT.scale(zoom, zoom);
        zoomAT.translate(-point.getX(), -point.getY());

        return zoomAT;
    }

    @Override
    public MouseFocusedZooming register(){
        if( ! Arrays.asList(imagePanel.getMouseWheelListeners()).contains(this))
            imagePanel.addMouseWheelListener(this);
        if ( ! Arrays.asList(imagePanel.getKeyListeners()).contains(this))
            imagePanel.addKeyListener(this);
        return this;
    }

    @Override
    public MouseFocusedZooming deRegister(){
        imagePanel.removeMouseWheelListener(this);
        imagePanel.removeKeyListener(this);
        return this;
    }

}
