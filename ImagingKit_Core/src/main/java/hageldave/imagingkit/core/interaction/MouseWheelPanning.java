package hageldave.imagingkit.core.interaction;

import hageldave.imagingkit.core.util.ImagePanel;

import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.util.Arrays;

/**
 * This class implements panning of an {@link ImagePanel} by scrolling the mouse wheel.
 * To avoid conflicts with other interactions, the 'shift' key can't be pressed during the panning.
 * The panning is either done horizontally or vertically, depending on whether the 'alt' key is pressed.
 * <br>
 * The MouseWheelPanning class first has to be registered on the image panel by calling register() and
 * passing the image panel object to the MouseWheelPanning constructor.
 * It can be deregistered if it isn't used anymore by calling deRegister().
 * <br>
 * Example use of the ImagePanning class:
 * <pre>MouseWheelPanning mwp = new MouseWheelPanning(imagePanel).register();</pre>
 * <pre>mwp.deRegister();</pre>
 */
public class MouseWheelPanning extends PanelInteraction  {

    final protected ImagePanel imagePanel;

    /**
     * Constructs a new MouseWheelPanning interaction for the given image panel.
     * @param imagePanel image panel to pan
     */
    public MouseWheelPanning(ImagePanel imagePanel) {
        this.imagePanel = imagePanel;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        super.mouseWheelMoved(e);
        if (pressedKeycode != KeyEvent.VK_SHIFT) {
            double scroll = e.getPreciseWheelRotation() * 1.5;
            AffineTransform panningAT = imagePanel.getPanningAffineTransform();
            double scaleX = imagePanel.getZoomAffineTransform().getScaleX();
            double scaleY = imagePanel.getZoomAffineTransform().getScaleY();
            if (pressedKeycode == KeyEvent.VK_ALT) {
                panningAT.translate(scroll / scaleX, 0);
            } else {
                panningAT.translate(0, scroll / scaleY);
            }
            imagePanel.setPanningAffineTransform(panningAT);
        }
    }

    @Override
    public MouseWheelPanning register(){
        if( ! Arrays.asList(imagePanel.getMouseWheelListeners()).contains(this))
            imagePanel.addMouseWheelListener(this);
        if ( ! Arrays.asList(imagePanel.getKeyListeners()).contains(this))
            imagePanel.addKeyListener(this);
        return this;
    }

    @Override
    public MouseWheelPanning deRegister(){
        imagePanel.removeMouseWheelListener(this);
        imagePanel.removeKeyListener(this);
        return this;
    }
}
