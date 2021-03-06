package aunmag.shooter.game.environment.weapon;

import aunmag.shooter.core.gui.font.FontStyle;
import aunmag.shooter.core.gui.font.Text;
import aunmag.shooter.core.math.BodyCircle;
import aunmag.shooter.core.math.CollisionCC;
import aunmag.shooter.core.utilities.Operative;
import aunmag.shooter.core.utilities.Timer;
import aunmag.shooter.core.utilities.UtilsMath;
import aunmag.shooter.game.client.Context;
import aunmag.shooter.game.environment.actor.Actor;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

public class WeaponBonus extends Operative {

    private static final float LIFETIME = 30;
    private static final float ROTATION_RATE = 2f;
    private static final float RADIUS = 0.12f;
    private static final float RADIUS_EXPAND = 0.06f;
    private static final float RADIUS_PULSE_RATE = 0.8f;

    public final BodyCircle body;
    public final Weapon weapon;
    @Nullable
    private Actor giver;
    private final Text text;
    private final Timer timer;

    public WeaponBonus(float x, float y, Weapon weapon, @Nullable Actor giver) {
        this.weapon = weapon;
        this.giver = giver;

        body = new BodyCircle(x, y, 0, 0);
        text = new Text(x, y, weapon.type.name, FontStyle.SIMPLE);
        text.setOnWorldRendering(true);

        timer = new Timer(weapon.world.time, LIFETIME);
        timer.next();
    }

    public WeaponBonus(float x, float y, Weapon weapon) {
        this(x, y, weapon, null);
    }

    public WeaponBonus(Actor giver, Weapon weapon) {
        this(
            giver.body.position.x,
            giver.body.position.y,
            weapon,
            giver
        );
    }

    private void drop() {
        if (giver != null) {
            body.position.x = giver.body.position.x;
            body.position.y = giver.body.position.y;
            text.position.x = giver.body.position.x;
            text.position.y = giver.body.position.y;
        }

        timer.next();
        giver = null;
    }

    @Override
    public void update() {
        if (giver == null) {
            updateColor();
            updateRadius();
            updateWeapon();
            updatePickup();
        } else if (!giver.isAlive() || !giver.isActive()) {
            drop();
        }
    }

    private void updateColor() {
        var alpha = UtilsMath.limit(
                4.0f * (1 - timer.getProgressLimited()),
                0.0f,
                0.8f
        );

        body.color.set(0.9f, 0.9f, 0.9f, alpha / 2);
        text.setColour(new Vector4f(1, 1, 1, alpha));
    }

    private void updateRadius() {
        body.radius = RADIUS + RADIUS_EXPAND * UtilsMath.oscillateSaw(
            weapon.world.time.getCurrent(),
            RADIUS_PULSE_RATE
        );
    }

    private void updateWeapon() {
        weapon.body.positionTail.set(body.position);
        weapon.body.radians = (float) UtilsMath.PIx2 * (1 - UtilsMath.oscillateTriangle(
            weapon.world.time.getCurrent(),
            ROTATION_RATE
        ));
        weapon.update();
    }

    private void updatePickup() {
        Context.main.getPlayerActor().ifPresent(actor -> {
            var collision = new CollisionCC(body, actor.hands.coverage);
            var input = Context.main.getInput();

            if (input.keyboard.isKeyPressed(GLFW.GLFW_KEY_E) && collision.isTrue()) {
                var previousWeapon = actor.getWeapon();

                if (previousWeapon != null) {
                    actor.world.bonuses.all.add(new WeaponBonus(
                            body.position.x,
                            body.position.y,
                            previousWeapon
                    ));
                }

                actor.setWeapon(weapon);
                remove();
            }
        });
    }

    @Override
    public void render() {
        if (giver == null) {
            body.render();
            text.orderRendering();

            if (!Context.main.isDebug()) {
                Context.main.getShader().bind();
                weapon.render();
            }
        }
    }

    @Override
    protected void onRemove() {
        text.remove();
        super.onRemove();
    }

    @Override
    public boolean isActive() {
        return super.isActive() && (giver != null || !timer.isDone());
    }

}
