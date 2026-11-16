package com.example.collectcircles;

public final class ProductionRates {

    private static final double BASE_SPAWN_RATE = 0.20;
    private static final double SPAWN_RATE_PER_PUSHER =
            Pusher.CIRCLES_PER_SECOND * 1.10;

    /** Prevents creating objects from this utility class. */
    private ProductionRates() {
    }

    /**
     * Calculates how many circles the game can create per second.
     *
     * @param pusherCount number of owned Pushers
     * @return circle spawn rate per second
     */
    public static double spawnRate(int pusherCount) {
        int safeCount = Math.max(0, pusherCount);
        return Math.max(BASE_SPAWN_RATE, safeCount * SPAWN_RATE_PER_PUSHER);
    }

    /**
     * Calculates how many circles all Pushers can collect per second.
     *
     * @param pusherCount number of owned Pushers
     * @return combined Pusher collection rate per second
     */
    public static double pusherRate(int pusherCount) {
        return Math.max(0, pusherCount) * Pusher.CIRCLES_PER_SECOND;
    }

    /**
     * Returns the slower of the spawn and collection rates.
     *
     * @param pusherCount number of owned Pushers
     * @return effective offline collection rate per second
     */
    public static double offlineRate(int pusherCount) {
        return Math.min(spawnRate(pusherCount), pusherRate(pusherCount));
    }
}