package aunmag.shooter.game.ai.strategies;

import aunmag.shooter.core.utilities.Timer;
import aunmag.shooter.core.utilities.UtilsMath;
import aunmag.shooter.game.ai.Ai;
import aunmag.shooter.game.ai.memory.Bypass;
import aunmag.shooter.game.ai.memory.Enemy;
import aunmag.shooter.game.environment.actor.Actor;
import aunmag.shooter.game.environment.actor.ActorType;

public abstract class Strategy {

    public static final int TIME_LIMIT = 30;

    public final Ai ai;
    public final float closeDistanceToDestination = UtilsMath.randomizeBetween(1, 2);
    public final float closeDistanceToEnemy = closeDistanceToDestination * 2;
    private final Timer timer;

    public Strategy(Ai ai) {
        this.ai = ai;
        this.timer = new Timer(ai.actor.world.getTime(), TIME_LIMIT);
    }

    public void update() {
        analyze();
        proceed();
    }

    public void analyze() {}

    public void proceed() {}

    public boolean isExpired() {
        return timer.isDone();
    }

    /* Analyzing methods */

    public void findEnemy() {
        var previous = (Actor) null;

        if (ai.enemy != null) {
            previous = ai.enemy.actor;
        }

        ai.enemy = null;

        for (var actor: ai.actor.world.getActors().all) {
            if (actor.isAlive() && actor.type == ActorType.human) {
                ai.enemy = new Enemy(ai, actor);
                break;
            }
        }

        if (ai.enemy != null && ai.enemy.actor != previous) {
            findEnemyBypass();
        }
    }

    public void findEnemyBypass() {
        if (ai.enemy == null) {
            ai.bypass = null;
        } else {
            ai.bypass = new Bypass(
                    ai,
                    ai.enemy,
                    UtilsMath.randomizeBetween(0, (float) UtilsMath.PIx2),
                    UtilsMath.randomizeBetween(
                            closeDistanceToEnemy,
                            closeDistanceToEnemy * 2
                    )
            );
        }
    }

    public boolean isClose(Bypass destination) {
        return destination.distance.get() < closeDistanceToDestination;
    }

    public boolean isClose(Enemy enemy) {
        return enemy.distance.get() < closeDistanceToEnemy;
    }

    public boolean isContact(Enemy enemy) {
        return Math.abs(enemy.angleRelative.get()) > UtilsMath.PIx0_5;
    }

    /* Proceeding methods */

    public void keepAttacking() {
        ai.actor.control.attack();
    }

    public void keepChasingEnemy() {
        var enemy = ai.enemy;

        if (enemy == null) {
            return;
        }

        var direction = ai.enemy.direction.get();

        if (ai.bypass != null) {
            direction = ai.bypass.direction.get();
        }

        ai.actor.control.turnTo(direction);
        ai.actor.control.walkForward();

        if (isContact(enemy) && isClose(enemy)) {
            ai.actor.control.sprint();
        }
    }

}