package com.example.collectcircles;

public final class OfflineProgressCalculator {

    /** Prevents creating objects from this utility class. */
    private OfflineProgressCalculator() {
    }

    /**
     * Calculates whole circles and the remaining fraction for an offline period.
     *
     * @param elapsedMillis elapsed wall-clock time, in milliseconds
     * @param pusherCount number of owned Pushers
     * @param previousFraction unfinished progress from the previous calculation
     * @return calculated whole circles and the fraction to keep
     */
    public static Result calculate(long elapsedMillis, int pusherCount,
                                   double previousFraction) {
        long safeElapsedMillis = Math.max(0, elapsedMillis);
        double safeFraction = Math.max(0, Math.min(previousFraction, 0.999999));
        double produced = safeFraction
                + safeElapsedMillis / 1000.0
                * ProductionRates.offlineRate(pusherCount);

        if (produced >= Long.MAX_VALUE) {
            return new Result(Long.MAX_VALUE, 0);
        }

        long wholeCircles = (long) Math.floor(produced);
        return new Result(wholeCircles, produced - wholeCircles);
    }

    public static final class Result {
        private final long wholeCircles;
        private final double fraction;

        /**
         * Creates an immutable calculation result.
         *
         * @param wholeCircles number of complete circles produced
         * @param fraction unfinished part of the next circle
         */
        private Result(long wholeCircles, double fraction) {
            this.wholeCircles = wholeCircles;
            this.fraction = fraction;
        }

        /** @return number of complete circles produced */
        public long getWholeCircles() {
            return wholeCircles;
        }

        /** @return unfinished part of the next circle */
        public double getFraction() {
            return fraction;
        }
    }
}