package hageldave.imagingkit.core.interaction;

import hageldave.imagingkit.core.util.ImagePanel;

import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.util.Arrays;

/**
 * This class implements zooming of an {@link ImagePanel} by scrolling the mouse wheel.
 * The zooming of this class is different from that of {@link MouseFocusedZooming},
 * as it always zooms into the center of the current viewport, not on the current mouse position.
 * <br>
 * To zoom on the image panel, the 'shift' key has to be pressed.
 * The zooming is done by scaling the zoomAffineTransform {@link AffineTransform} of the image panel.
 * <br>
 * The ImageZooming class first has to be registered on the image panel by calling register().
 * It can be deregistered if it isn't used anymore by calling deRegister().
 * <br>
 * Example use of the ImageZooming class:
 * <pre>ImageZooming iz = new ImageZooming(imagePanel).register();</pre>
 * <pre>iz.deRegister();</pre>
 */
public class ImageZooming extends PanelInteraction  {
    final protected ImagePanel imagePanel;

    /**
     * Constructs a new ImageZooming interaction for the given image panel.
     * @param imagePanel image panel to zoom
     */
    public ImageZooming(ImagePanel imagePanel) {
        this.imagePanel = imagePanel;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        super.mouseWheelMoved(e);
        if (pressedKeycode == KeyEvent.VK_SHIFT) {
            AffineTransform zoomAT = this.calcZoomAffineTransform(Math.pow(1.7, e.getWheelRotation() * 0.1));
            AffineTransform panelAT = this.imagePanel.getZoomAffineTransform();
            panelAT.concatenate(zoomAT);
            this.imagePanel.setZoomAffineTransform(panelAT);
        }
    }

    protected AffineTransform calcZoomAffineTransform(double zoom) {
        double imageWidth = imagePanel.getBounds().getWidth();
        double imageHeight = imagePanel.getBounds().getHeight();

        AffineTransform zoomAT = new AffineTransform();
        zoomAT.translate(imageWidth / 2.0, imageHeight / 2.0);
        zoomAT.scale(zoom, zoom);
        zoomAT.translate(-(imageWidth / 2.0), -(imageHeight / 2.0));
        return zoomAT;
    }

    @Override
    public ImageZooming register(){
        if( ! Arrays.asList(imagePanel.getMouseWheelListeners()).contains(this))
            imagePanel.addMouseWheelListener(this);
        if ( ! Arrays.asList(imagePanel.getKeyListeners()).contains(this))
            imagePanel.addKeyListener(this);
        return this;
    }

    @Override
    public ImageZooming deRegister(){
        imagePanel.removeMouseWheelListener(this);
        imagePanel.removeKeyListener(this);
        return this;
    }
}
