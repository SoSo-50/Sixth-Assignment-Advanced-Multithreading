package Banking;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BankAccount {
    private final int id;
    private volatile long balance;
    private final Lock lock = new ReentrantLock();

    public BankAccount(int id, long initialBalance) {
        this.id = id;
        this.balance = initialBalance;
    }

    public int getId() {
        return id;
    }

    public long getBalance() {
        return balance;
    }

    public Lock getLock() {
        return lock;
    }

    public void deposit(int amount) {
        if (amount < 0) {
            System.err.println(Thread.currentThread().getName() + ": Cannot deposit negative amount into Account " + id);
            return;
        }
        lock.lock();
        try {
            balance += amount;
            // System.out.println(Thread.currentThread().getName() + " deposited " + amount + " to Account " + id + ". New balance: " + balance);
        } finally {
            lock.unlock();
        }
    }

    public void withdraw(int amount) {
        if (amount < 0) {
            System.err.println(Thread.currentThread().getName() + ": Cannot withdraw negative amount from Account " + id);
            return;
        }
        lock.lock();
        try {
            if (balance >= amount) {
                balance -= amount;
                // System.out.println(Thread.currentThread().getName() + " withdrew " + amount + " from Account " + id + ". New balance: " + balance);
            } else {
                System.out.println(Thread.currentThread().getName() + ": Insufficient funds in Account " + id + " for withdrawal of " + amount);
            }
        } finally {
            lock.unlock();
        }
    }

    public void transfer(BankAccount target, int amount) {
        if (this == target) {
            return;
        }
        if (amount < 0) {
            System.err.println(Thread.currentThread().getName() + ": Cannot transfer negative amount from Account " + this.id + " to Account " + target.id);
            return;
        }

        BankAccount firstLockAccount = (this.id < target.id) ? this : target;
        BankAccount secondLockAccount = (this.id < target.id) ? target : this;

        firstLockAccount.getLock().lock();
        try {
            secondLockAccount.getLock().lock();
            try {
                if (this.balance >= amount) {
                    this.balance -= amount;
                    target.balance += amount;

                } else {
                    System.out.println(Thread.currentThread().getName() + ": Insufficient funds in Account " + this.id + " for transfer of " + amount);
                }
            } finally {
                secondLockAccount.getLock().unlock();
            }
        } finally {
            firstLockAccount.getLock().unlock();
        }
    }
}