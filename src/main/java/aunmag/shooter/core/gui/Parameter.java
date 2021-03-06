package aunmag.shooter.core.gui;

import aunmag.shooter.core.Context;
import aunmag.shooter.core.graphics.Graphics;
import aunmag.shooter.core.gui.font.FontStyle;
import aunmag.shooter.core.utilities.UtilsMath;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

public class Parameter extends Component {

    public static final Grid GRID = new Grid(36);
    public static final float PULSE_RATE = 0.4f;
    public static final float PULSE_BOUND = 0.5f;
    public static final Vector4f TEXT_COLOR = new Vector4f(1.0f, 1.0f, 1.0f, 0.5f);

    public boolean isPulsing = false;
    public float value;
    private final Label label;

    public Parameter(String label, float value, int x, int y) {
        this.value = value;
        this.label = new Label(GRID, x, y, 1, 1, label, FontStyle.LABEL_LIGHT);
        this.label.setTextColour(TEXT_COLOR);
    }

    @Override
    public void render() {
        var pulse = getPulse();
        var window = Context.main.getWindow();

        var x = GRID.getStepX() * (label.quad.position.x + 2.0f);
        var y = GRID.getStepY() * (label.quad.position.y + 0.3f);
        var sizeX = GRID.getStepX() * 3.0f;
        var sizeY = GRID.getStepY() / 2.0f;

        var a = sizeX * value;
        var b = sizeX * (1 - value);

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.75f * pulse);
        Graphics.draw.quad(x + 0, y, x + 0 + a, y + sizeY, true, window::project);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.25f * pulse);
        Graphics.draw.quad(x + a, y, x + a + b, y + sizeY, true, window::project);

        label.render();
    }

    @Override
    protected void onRemove() {
        label.remove();
        super.onRemove();
    }

    public float getPulse() {
        if (isPulsing) {
            return UtilsMath.bound(
                UtilsMath.oscillateSaw(Context.main.getTime().getCurrent(), PULSE_RATE),
                PULSE_BOUND
            );
        } else {
            return 1.0f;
        }
    }

}
