package hageldave.imagingkit.core.interaction;

import hageldave.imagingkit.core.util.ImagePanel;

import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
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
            double zoom = Math.pow(1.7, e.getWheelRotation() * 0.2);

            AffineTransform zoomAT = imagePanel.getZoomAffineTransform();
            double prevScaleX = zoomAT.getScaleX();
            double prevScaleY = zoomAT.getScaleY();
            double imageWidth = imagePanel.getWidth() * prevScaleX;
            double imageHeight = imagePanel.getHeight() * prevScaleY;
            double imageX = zoomAT.transform(new Point2D.Double(0, 0), new Point2D.Double()).getX();
            double imageY = zoomAT.transform(new Point2D.Double(0, 0), new Point2D.Double()).getY();

//			TODO: this is for mouse centered zooming (doesn't work correctly currently)
//			ImagePanel.this.affineTransform.translate(e.getX(), e.getY());
//			ImagePanel.this.affineTransform.scale(zoom, zoom);
//			ImagePanel.this.affineTransform.translate(-e.getX(), -e.getY());

//			This zooms by the center of the panel

            zoomAT.translate(imageWidth / 2.0 + imageX, imageHeight / 2.0 + imageY);
            zoomAT.scale(zoom, zoom);
            zoomAT.translate(-(imageWidth / 2.0 + imageX), -(imageHeight / 2.0 + imageY));
            imagePanel.setZoomAffineTransform(zoomAT);
        }
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
