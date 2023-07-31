package hageldave.imagingkit.core.interaction;

import hageldave.imagingkit.core.util.ImagePanel;

import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.util.Arrays;

public class MouseWheelPanning extends PanelInteraction  {

    final protected ImagePanel imagePanel;

    public MouseWheelPanning(ImagePanel imagePanel) {
        this.imagePanel = imagePanel;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        super.mouseWheelMoved(e);
        if (pressedKeycode != KeyEvent.VK_SHIFT) {
            double scroll = e.getPreciseWheelRotation() * 1.5;
            AffineTransform panningAT = imagePanel.getPanningAffineTransform();
            if (pressedKeycode == KeyEvent.VK_ALT) {
                double scaleX = imagePanel.getZoomAffineTransform().getScaleX();
                panningAT.translate(scroll / scaleX, 0);
            } else {
                double scaleY = imagePanel.getZoomAffineTransform().getScaleY();
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
