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
            double scroll = e.getPreciseWheelRotation() * 1.3;
            AffineTransform panningAT = imagePanel.getPanningAffineTransform();
            if (pressedKeycode == KeyEvent.VK_ALT) {
                panningAT.translate(scroll, 0);
            } else {
                panningAT.translate(0, scroll);
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
