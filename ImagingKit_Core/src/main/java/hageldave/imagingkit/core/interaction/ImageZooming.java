package hageldave.imagingkit.core.interaction;

import hageldave.imagingkit.core.util.ImagePanel;

import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.util.Arrays;

public class ImageZooming extends PanelInteraction  {
    final protected ImagePanel imagePanel;

    public ImageZooming(ImagePanel imagePanel) {
        this.imagePanel = imagePanel;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        super.mouseWheelMoved(e);
        if (pressedKeycode == KeyEvent.VK_SHIFT) {
            this.imagePanel.appendZoomAffineTransform(this.updateAffineTransform(Math.pow(1.7, e.getWheelRotation() * 0.1)));
        }
    }

    public AffineTransform updateAffineTransform(double zoom) {
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
