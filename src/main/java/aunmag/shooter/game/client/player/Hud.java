package aunmag.shooter.game.client.player;

import aunmag.shooter.core.gui.Layer;
import aunmag.shooter.core.gui.Parameter;
import aunmag.shooter.core.gui.font.FontStyle;
import aunmag.shooter.core.gui.font.Text;
import aunmag.shooter.core.utilities.Operative;
import aunmag.shooter.game.client.Context;

public class Hud extends Operative {

    public final Layer layer = new Layer();
    private final Parameter health = new Parameter("Health", 0.0f, 30, 32);
    private final Parameter stamina = new Parameter("Stamina", 0.0f, 30, 33);
    private final Parameter ammo = new Parameter("Ammo", 0.0f, 30, 34);
    private final Text debug = new Text(10f, 10f, "", FontStyle.SIMPLE);

    public Hud() {
        layer.add(health);
        layer.add(stamina);
        layer.add(ammo);
    }

    @Override
    public void update() {
        layer.update();

        Context.main.getPlayerActor().ifPresent(actor -> {
            health.value = actor.getHealth();
            stamina.value = actor.stamina.get();

            var weapon = actor.getWeapon();

            if (weapon == null) {
                ammo.value = 0;
                ammo.isPulsing = false;
            } else {
                ammo.value = weapon.magazine.getVolumeRatio();
                ammo.isPulsing = weapon.magazine.isReloading();
            }
        });
    }

    @Override
    public void render() {
        layer.render();

        if (Context.main.application.isDebug()) {
            renderDebug();
        }
    }

    private void renderDebug() {
        Context.main.getGame().ifPresent(game -> {
            var world = game.getWorld();

            var message = "";
            message += String.format("\nAIs: %s", world.ais.all.size());
            message += String.format("\nActors: %s", world.actors.all.size());
            message += String.format("\nBullets: %s", world.projectiles.all.size());
            message += String.format("\nGround: %s", world.ground.all.size());
            message += String.format("\nTrees: %s", world.trees.all.size());

            debug.load(message);
            debug.orderRendering();
        });
    }

    @Override
    protected void onRemove() {
        layer.remove();
        debug.remove();
        super.onRemove();
    }

}
