package aunmag.shooter.core.gui;

import aunmag.shooter.core.gui.font.FontStyle;
import aunmag.shooter.core.utilities.TimeFlow;
import aunmag.shooter.core.utilities.Timer;
import aunmag.shooter.core.utilities.UtilsMath;
import org.joml.Vector4f;

public class Notification extends Component {

    public static final float TIME_FADE_IN = 0.125f;
    public static final float TIME_FADE_OUT = 0.5f;

    private final Timer timer;
    private final Label title;
    private final Label details;
    private final Vector4f color = new Vector4f(1, 1, 1, 0);

    public Notification(TimeFlow time, String title, String details) {
        this.timer = new Timer(time, 3.0f);
        this.title = new Label(5, 4, 2, 1, title);
        this.details = new Label(5, 5, 2, 1, details, FontStyle.LABEL_LIGHT);

        timer.next();
    }

    @Override
    public void render() {
        color.w = getFade();

        title.setTextColour(color);
        title.render();

        details.setTextColour(color);
        details.render();
    }

    @Override
    protected void onRemove() {
        title.remove();
        details.remove();
        super.onRemove();
    }

    @Override
    public boolean isActive() {
        return super.isActive() && !timer.isDone();
    }

    public float getFade() {
        // TODO: Use envelope
        var passed = timer.getPassed();
        var left = timer.getRemaining();

        var fade = 0f;

        if (passed < left) {
            fade = passed / TIME_FADE_IN;
        } else {
            fade = left / TIME_FADE_OUT;
        }

        return UtilsMath.limit(fade, 0, 1);
    }

}
