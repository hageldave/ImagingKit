package hageldave.imagingkit.core.interaction;

import hageldave.imagingkit.core.util.ImagePanel;

import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.util.Arrays;

public class ImageZooming extends PanelInteraction  {
    final protected ImagePanel imagePanel;
    protected double zoom = 1.0;

    public ImageZooming(ImagePanel imagePanel) {
        this.imagePanel = imagePanel;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        super.mouseWheelMoved(e);
        if (pressedKeycode == KeyEvent.VK_SHIFT) {
            this.zoom *= Math.pow(1.7, e.getWheelRotation() * 0.1);

//			TODO: this is for mouse centered zooming (doesn't work correctly currently)
//
//          double prevScaleX = zoomAT.getScaleX();
//          double prevScaleY = zoomAT.getScaleY();
//          double imageWidth = imagePanel.getWidth() * prevScaleX;
//          double imageHeight = imagePanel.getHeight() * prevScaleY;
//          double imageX = zoomAT.transform(new Point2D.Double(0, 0), new Point2D.Double()).getX();
//          double imageY = zoomAT.transform(new Point2D.Double(0, 0), new Point2D.Double()).getY();
//			ImagePanel.this.affineTransform.translate(e.getX(), e.getY());
//			ImagePanel.this.affineTransform.scale(zoom, zoom);
//			ImagePanel.this.affineTransform.translate(-e.getX(), -e.getY());

//			This zooms by the center of the panel
            this.imagePanel.setZoomAffineTransform(updateAffineTransform());
        }
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    public AffineTransform updateAffineTransform() {
        double imageX = 0;
        double imageY = 0;
        if (imagePanel.getGraphics().getClipBounds() != null) {
            imageX = -imagePanel.getGraphics().getClipBounds().getX();
            imageY = -imagePanel.getGraphics().getClipBounds().getY();
        }

        double imageWidth = imagePanel.getBounds().getWidth();
        double imageHeight = imagePanel.getBounds().getHeight();

        AffineTransform zoomAT = new AffineTransform();
        zoomAT.translate(imageWidth / 2.0 + imageX, imageHeight / 2.0 + imageY);
        zoomAT.scale(zoom, zoom);
        zoomAT.translate(-(imageWidth / 2.0 + imageX), -(imageHeight / 2.0 + imageY));
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
