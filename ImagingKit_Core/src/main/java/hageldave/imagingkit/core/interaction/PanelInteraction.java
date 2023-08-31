package hageldave.imagingkit.core.interaction;

import hageldave.imagingkit.core.util.ImagePanel;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;

/**
 * The PanelInteraction class is an abstract class that can be used to implement interactions with an {@link ImagePanel}.
 * It implements the {@link KeyListener} interface and provides a {@link #pressedKeycode} field that can be used to
 * check which key is currently pressed.
 * <br>
 * Implementations of the abstract class can be found in the {@link hageldave.imagingkit.core.interaction} package.
 */
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

    /**
     * Registers the interaction class to the image panel (e.g., calling addMouseListener() on the image panel).
     *
     * @return {@link PanelInteraction} this for chaining
     */
    public abstract PanelInteraction register();

    /**
     * Unregisters the interaction class from the image panel (e.g., calling removeMouseListener() on the image panel).
     *
     * @return {@link PanelInteraction} this for chaining
     */
    public abstract PanelInteraction deRegister();
}
