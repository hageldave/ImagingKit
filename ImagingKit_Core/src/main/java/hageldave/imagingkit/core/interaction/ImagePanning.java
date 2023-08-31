package hageldave.imagingkit.core.interaction;

import hageldave.imagingkit.core.util.ImagePanel;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Arrays;

/**
 * This class implements panning of an {@link ImagePanel} by dragging the mouse.
 * To activate the panning, the 'e' key has to be pressed.
 * The panning is done by translating the panningAffineTransform {@link AffineTransform} of the image panel.
 * <br>
 * The ImagePanning class first has to be registered on the image panel by calling register() and
 * passing the image panel object to the ImagePanning constructor.
 * It can be deregistered if it isn't used anymore by calling deRegister().
 * <br>
 * Example use of the ImagePanning class:
 * <pre>ImagePanning ip = new ImagePanning(imagePanel).register();</pre>
 * <pre>ip.deRegister();</pre>
 */
public class ImagePanning extends PanelInteraction  {
    final protected ImagePanel imagePanel;
    protected Point2D dragStart = null;

    /**
     * Constructs a new ImagePanning interaction for the given image panel.
     * @param imagePanel image panel to pan
     */
    public ImagePanning(ImagePanel imagePanel) {
        this.imagePanel = imagePanel;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        super.mouseDragged(e);
        if (this.dragStart != null) {
            double mouseTx = e.getX()-this.dragStart.getX();
            double mouseTy = e.getY()-this.dragStart.getY();
            double scaleX = imagePanel.getZoomAffineTransform().getScaleX();
            double scaleY = imagePanel.getZoomAffineTransform().getScaleY();
            mouseTx /= scaleX;
            mouseTy /= scaleY;

            AffineTransform transformedTrans = imagePanel.getPanningAffineTransform();
            transformedTrans.translate(mouseTx, mouseTy);
            imagePanel.setPanningAffineTransform(transformedTrans);
            this.dragStart.setLocation(e.getX(), e.getY());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (pressedKeycode == KeyEvent.VK_E) {
            this.dragStart = e.getPoint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        this.dragStart = null;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);
        if (e.getKeyCode() == KeyEvent.VK_E) {
            imagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        super.keyReleased(e);
        imagePanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public ImagePanning register(){
        if( ! Arrays.asList(imagePanel.getMouseListeners()).contains(this))
            imagePanel.addMouseListener(this);
        if( ! Arrays.asList(imagePanel.getMouseMotionListeners()).contains(this))
            imagePanel.addMouseMotionListener(this);
        if ( ! Arrays.asList(imagePanel.getKeyListeners()).contains(this))
            imagePanel.addKeyListener(this);
        return this;
    }

    @Override
    public ImagePanning deRegister(){
        imagePanel.removeMouseListener(this);
        imagePanel.removeMouseMotionListener(this);
        imagePanel.removeKeyListener(this);
        return this;
    }
}
