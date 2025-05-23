package Banking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BankingMain {

    private static Map<Integer, BankAccount> accountMap = new HashMap<>();
    private static final String[] TRANSACTION_FILES = {
            "1.txt", "2.txt", "3.txt", "4.txt"
    };
    private static final long INITIAL_BALANCE = 20000;

    public static void main(String[] args) throws InterruptedException {

        System.out.println("--- Initializing Bank Accounts ---");
        for (int i = 1; i <= TRANSACTION_FILES.length; i++) {
            BankAccount account = new BankAccount(i, INITIAL_BALANCE);
            accountMap.put(i, account);
            System.out.println("Account " + account.getId() + " created with initial balance: " + account.getBalance());
        }

        long totalInitialBalance = calculateTotalBalance();
        System.out.println("Total initial balance across all accounts: " + totalInitialBalance);

        System.out.println("\n--- Processing Transactions ---");

        ExecutorService executor = Executors.newFixedThreadPool(TRANSACTION_FILES.length);
        List<TransactionProcessor> transactionProcessors = new ArrayList<>();

        for (int i = 0; i < TRANSACTION_FILES.length; i++) {
            BankAccount associatedAccount = accountMap.get(i + 1);
            TransactionProcessor processor = new TransactionProcessor(associatedAccount, TRANSACTION_FILES[i], new ArrayList<>(accountMap.values()));
            transactionProcessors.add(processor);
            executor.submit(processor);
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.err.println("Transaction processing interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }

        long totalExpectedNetChange = 0;
        for (TransactionProcessor processor : transactionProcessors) {
            totalExpectedNetChange += processor.getNetChange();
        }

        System.out.println("\n--- Final Account Balances ---");
        long totalFinalBalance = calculateTotalBalance();
        for (BankAccount account : accountMap.values()) {
            System.out.println("Final balance of Account " + account.getId() + ": " + account.getBalance());
        }

        System.out.println("Total final balance across all accounts: " + totalFinalBalance);
        System.out.println("Total expected net change from transactions: " + totalExpectedNetChange);

        if (totalFinalBalance == (totalInitialBalance + totalExpectedNetChange)) {
            System.out.println("\nIntegrity check: Total balance is consistent! (All operations accounted for)");
        } else {
            System.err.println("\nIntegrity check: Total balance is INCONSISTENT! (Race Conditions or other logic errors might have occurred)");
        }
    }

    private static long calculateTotalBalance() {
        long total = 0;
        for (BankAccount account : accountMap.values()) {
            total += account.getBalance();
        }
        return total;
    }
}