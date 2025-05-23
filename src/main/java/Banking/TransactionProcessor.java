package Banking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong; // برای جمع خالص تغییرات

public class TransactionProcessor implements Runnable {
    private final BankAccount account;
    private final String fileName;
    private final List<BankAccount> allAccounts;
    private final AtomicLong netChange = new AtomicLong(0);

    public TransactionProcessor(BankAccount account, String fileName, List<BankAccount> allAccounts) {
        this.account = account;
        this.fileName = fileName;
        this.allAccounts = allAccounts;
    }

    public long getNetChange() {
        return netChange.get();
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(
                                getClass().getClassLoader().getResourceAsStream(fileName))))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length == 0) continue;

                try {
                    switch (parts[0]) {
                        case "Deposit":
                            if (parts.length >= 2) {
                                int depositAmount = Integer.parseInt(parts[1]);
                                account.deposit(depositAmount);
                                netChange.addAndGet(depositAmount);
                            } else {
                                System.err.println(Thread.currentThread().getName() + ": Invalid Deposit format in " + fileName + ": " + line);
                            }
                            break;

                        case "Withdraw":
                            if (parts.length >= 2) {
                                int withdrawAmount = Integer.parseInt(parts[1]);

                                long currentBalanceBeforeAttempt = account.getBalance();
                                account.withdraw(withdrawAmount);
                                if (currentBalanceBeforeAttempt >= withdrawAmount) {
                                    netChange.addAndGet(-withdrawAmount);
                                }
                            } else {
                                System.err.println(Thread.currentThread().getName() + ": Invalid Withdraw format in " + fileName + ": " + line);
                            }
                            break;
                        case "Transfer":
                            if (parts.length >= 3) {
                                int targetId = Integer.parseInt(parts[1]);
                                int transferAmount = Integer.parseInt(parts[2]);

                                BankAccount targetAccount = null;
                                for (BankAccount acc : allAccounts) {
                                    if (acc.getId() == targetId) {
                                        targetAccount = acc;
                                        break;
                                    }
                                }

                                if (targetAccount != null) {
                                    account.transfer(targetAccount, transferAmount);
                                } else {
                                    System.err.println(Thread.currentThread().getName() + ": Invalid target account ID in " + fileName + ": " + line);
                                }
                            } else {
                                System.err.println(Thread.currentThread().getName() + ": Invalid Transfer format in " + fileName + ": " + line);
                            }
                            break;
                        default:
                            System.err.println(Thread.currentThread().getName() + ": Unknown operation in " + fileName + ": " + line);
                    }
                } catch (NumberFormatException e) {
                    System.err.println(Thread.currentThread().getName() + ": Invalid number format in " + fileName + ": " + line + " - " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println(Thread.currentThread().getName() + ": Error reading transaction file " + fileName + ": " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println(Thread.currentThread().getName() + ": An unexpected error occurred while processing " + fileName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}