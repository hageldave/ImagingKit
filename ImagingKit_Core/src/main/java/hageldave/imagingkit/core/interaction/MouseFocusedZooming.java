package hageldave.imagingkit.core.interaction;

import hageldave.imagingkit.core.util.ImagePanel;

import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.Arrays;

/**
 * This class implements zooming of an {@link ImagePanel} by scrolling the mouse wheel.
 * The zooming of this class is different from that of {@link ImageZooming},
 * as it always zooms onto the current mouse position, not into the center of the current viewport.
 * <br>
 * To zoom on the image panel, the 'shift' key has to be pressed.
 * The zooming is done by scaling the zoomAffineTransform {@link AffineTransform} of the image panel.
 * <br>
 * The MouseFocusedZooming class first has to be registered on the image panel by calling register().
 * It can be deregistered if it isn't used anymore by calling deRegister().
 * <br>
 * Example use of the MouseFocusedZooming class:
 * <pre>MouseFocusedZooming mfz = new MouseFocusedZooming(imagePanel).register();</pre>
 * <pre>mfz.deRegister();</pre>
 */
public class MouseFocusedZooming extends PanelInteraction {
    final protected ImagePanel imagePanel;

    /**
     * Constructs a new MouseFocusedZooming object for the given image panel.
     * @param imagePanel image panel to zoom
     */
    public MouseFocusedZooming(ImagePanel imagePanel) {
        this.imagePanel = imagePanel;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        super.mouseWheelMoved(e);
        if (pressedKeycode == KeyEvent.VK_SHIFT) {
            try {
                AffineTransform zoomAT = this.calcZoomAffineTransform(e, Math.pow(1.7, e.getWheelRotation() * 0.1));
                AffineTransform panelAT = this.imagePanel.getZoomAffineTransform();
                panelAT.concatenate(zoomAT);
                this.imagePanel.setZoomAffineTransform(panelAT);
            } catch (NoninvertibleTransformException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    protected AffineTransform calcZoomAffineTransform(MouseWheelEvent e, double zoom) throws NoninvertibleTransformException {
        Point2D.Double mouseCoordinates = new Point2D.Double(e.getX(), e.getY());
        mouseCoordinates = (Point2D.Double) imagePanel.getZoomAffineTransform().inverseTransform(mouseCoordinates, mouseCoordinates);

        AffineTransform zoomAT = new AffineTransform();
        zoomAT.translate(mouseCoordinates.getX(), mouseCoordinates.getY());
        zoomAT.scale(zoom, zoom);
        zoomAT.translate(-mouseCoordinates.getX(), -mouseCoordinates.getY());
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
