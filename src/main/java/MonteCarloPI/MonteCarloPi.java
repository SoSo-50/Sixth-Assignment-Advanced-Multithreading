package MonteCarloPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner; // اضافه شده برای ورودی کاربر
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong; // برای شمارنده thread-safe

public class MonteCarloPi {

    static class MonteCarloTask implements Callable<Long> {
        private final long numberOfPoints;
        private final Random random;

        public MonteCarloTask(long numberOfPoints, Random random) {
            this.numberOfPoints = numberOfPoints;
            this.random = random;
        }

        @Override
        public Long call() throws Exception {
            long pointsInCircle = 0;
            for (long i = 0; i < numberOfPoints; i++) {
                double x = random.nextDouble() * 2 - 1;
                double y = random.nextDouble() * 2 - 1;
                if (x * x + y * y <= 1) {
                    pointsInCircle++;
                }
            }
            return pointsInCircle;
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter total number of points for simulation (e.g., 10000000): ");
        long totalPoints = scanner.nextLong();

        System.out.print("Enter number of threads (e.g., 4): ");
        int numberOfThreads = scanner.nextInt();

        // ----------  (Single threaded) ----------
        System.out.println("\n--- Single-threaded calculation started ---");
        long startTime = System.nanoTime();
        double piWithoutThreads = estimatePiWithoutThreads(totalPoints);
        long endTime = System.nanoTime();
        System.out.println("Monte Carlo Pi Approximation (single thread): " + piWithoutThreads);
        System.out.println("Time taken (single thread): " + (endTime - startTime) / 1_000_000.0 + " ms");

        // ----------  (Multi-threaded) ----------
        System.out.printf("\n--- Multi-threaded calculation started: (using %d threads) ---\n", numberOfThreads);
        startTime = System.nanoTime();
        double piWithThreads = estimatePiWithThreads(totalPoints, numberOfThreads);
        endTime = System.nanoTime();
        System.out.println("Monte Carlo Pi Approximation (Multi-threaded): " + piWithThreads);
        System.out.println("Time taken (Multi-threaded): " + (endTime - startTime) / 1_000_000.0 + " ms");

        scanner.close();

    }

    // Monte Carlo Pi Approximation without threads
    public static double estimatePiWithoutThreads(long numPoints) {
        long pointsInCircle = 0;
        Random random = new Random();

        for (long i = 0; i < numPoints; i++) {
            double x = random.nextDouble() * 2 - 1;
            double y = random.nextDouble() * 2 - 1;
            if (x * x + y * y <= 1) {
                pointsInCircle++;
            }
        }
        return 4.0 * pointsInCircle / numPoints;
    }

    // Monte Carlo Pi Approximation with threads
    public static double estimatePiWithThreads(long numPoints, int numThreads) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<Long>> futures = new ArrayList<>();

        long pointsPerThread = numPoints / numThreads;
        int remainingPoints = (int) (numPoints % numThreads);

        Random sharedRandom = new Random();

        for (int i = 0; i < numThreads; i++) {
            long currentPointsForTask = pointsPerThread;
            if (i < remainingPoints) {
                currentPointsForTask++;
            }

            MonteCarloTask task = new MonteCarloTask(currentPointsForTask, new Random(sharedRandom.nextLong()));
            futures.add(executor.submit(task));
        }

        long totalPointsInCircle = 0;
        for (Future<Long> future : futures) {
            totalPointsInCircle += future.get();
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.NANOSECONDS);

        return 4.0 * totalPointsInCircle / numPoints;
    }
}