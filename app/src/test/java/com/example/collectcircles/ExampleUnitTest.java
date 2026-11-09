package com.example.collectcircles;

import org.junit.Test;

import static org.junit.Assert.*;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    /**
     * Verifies the Auto spawn rate and the maximum on-screen circle count.
     */
    @Test
    public void autonomousGameSpawnsSlowlyAndStopsAtTheScreenCap() {
        Game game = new Game(1000, 1000, 100,
                0, 0, 1, 0, true,0,0);

        assertEquals(3, game.getCircleCount());

        game.update(5.1, 1000, 1000, 0);
        assertEquals(4, game.getCircleCount());

        game.update(1000, 1000, 1000, 0);
        assertEquals(Game.MAX_CIRCLE_COUNT, game.getCircleCount());
    }



    /**
     * Verifies that elapsed time does not add circles to a timed game.
     */
    @Test
    public void timedGameDoesNotSpawnMoreCircles() {
        Game game = new Game(1000, 1000, 100,
                0, 0, 1, 0, false,0,0);

        game.update(1000, 1000, 1000, 0);

        assertEquals(5, game.getCircleCount());
    }

    /**
     * Verifies that prices double and stop safely before long overflow.
     */
    @Test
    public void pusherPricesDoubleWithoutOverflowing() {
        assertEquals(64, GameProgress.priceForPusherCount(0));
        assertEquals(128, GameProgress.priceForPusherCount(1));
        assertEquals(256, GameProgress.priceForPusherCount(2));
        assertEquals(Long.MAX_VALUE, GameProgress.priceForPusherCount(57));
    }
    /** Verifies that one Pusher completes exactly one five-second task. */
    @Test
    public void pusherCompletesOneCircleAfterFiveSeconds() {
        Pusher pusher = new Pusher(20, 20, 40, 0, 0);
        Circle circle = new Circle(100, 100, 20, 0);
        Target target = new Target(200, 200, 50, 0, 0, 1);

        assertTrue(pusher.assign(circle, target));
        assertNull(pusher.update(4.9));
        assertSame(circle, pusher.update(0.1));
        assertEquals(target.getCenterX(), circle.getCenterX(), 0.001);
        assertEquals(target.getCenterY(), circle.getCenterY(), 0.001);
    }

}

