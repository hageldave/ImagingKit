package hageldave.imagingkit.core.interaction;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;

public abstract class PanelInteraction extends MouseAdapter implements KeyListener {
    int pressedKeycode = -1;
    @Override
    public void keyPressed(KeyEvent e) {
        this.pressedKeycode = e.getKeyCode();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        this.pressedKeycode = -1;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    public abstract PanelInteraction register();

    public abstract PanelInteraction deRegister();
}
