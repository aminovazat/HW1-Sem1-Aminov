package com.mipt.azataminov.concurrency;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class BankTest {

    @Test
    void testSendToAccountSuccess() {
        Bank bank = new Bank();
        BankAccount account1 = new BankAccount(1, 1000);
        BankAccount account2 = new BankAccount(2, 500);

        bank.sendToAccount(account1, account2, 300);

        assertEquals(700, account1.getBalance());
        assertEquals(800, account2.getBalance());
    }

    @Test
    void testSendToAccountInsufficientFunds() {
        Bank bank = new Bank();
        BankAccount account1 = new BankAccount(1, 100);
        BankAccount account2 = new BankAccount(2, 500);

        assertThrows(IllegalStateException.class, () -> {
            bank.sendToAccount(account1, account2, 200);
        });
    }

    @Test
    void testSendToAccountInvalidAmount() {
        Bank bank = new Bank();
        BankAccount account1 = new BankAccount(1, 1000);
        BankAccount account2 = new BankAccount(2, 500);

        assertThrows(IllegalArgumentException.class, () -> {
            bank.sendToAccount(account1, account2, -100);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            bank.sendToAccount(account1, account2, 0);
        });
    }

    @Test
    void testSendToAccountNullAccounts() {
        Bank bank = new Bank();
        BankAccount account1 = new BankAccount(1, 1000);

        assertThrows(IllegalArgumentException.class, () -> {
            bank.sendToAccount(null, account1, 100);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            bank.sendToAccount(account1, null, 100);
        });
    }

    @Test
    void testConcurrentTransfers() throws InterruptedException {
        Bank bank = new Bank();
        BankAccount account1 = new BankAccount(1, 10000);
        BankAccount account2 = new BankAccount(2, 10000);

        int threadCount = 10;
        int transfersPerThread = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    for (int j = 0; j < transfersPerThread; j++) {
                        bank.sendToAccount(account1, account2, 10);
                        bank.sendToAccount(account2, account1, 10);
                    }
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await(5, TimeUnit.SECONDS);

        assertEquals(10000, account1.getBalance());
        assertEquals(10000, account2.getBalance());
    }

    @Test
    void testDeadlockScenarioWithTimeout() throws InterruptedException {
        Bank bank = new Bank();
        BankAccount account1 = new BankAccount(1, 1000);
        BankAccount account2 = new BankAccount(2, 1000);

        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                bank.sendToAccountDeadlock(account1, account2, 10);
            }
        });

        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                bank.sendToAccountDeadlock(account2, account1, 10);
            }
        });

        thread1.start();
        thread2.start();

        thread1.join(2000);
        thread2.join(2000);

        assertFalse(thread1.isAlive());
        assertFalse(thread2.isAlive());
    }

    @Test
    void testNoDeadlockWithCorrectMethod() throws InterruptedException {
        Bank bank = new Bank();
        BankAccount account1 = new BankAccount(1, 1000);
        BankAccount account2 = new BankAccount(2, 1000);

        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                bank.sendToAccount(account1, account2, 10);
            }
        });

        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                bank.sendToAccount(account2, account1, 10);
            }
        });

        thread1.start();
        thread2.start();

        thread1.join(2000);
        thread2.join(2000);

        assertFalse(thread1.isAlive());
        assertFalse(thread2.isAlive());
        assertEquals(1000, account1.getBalance());
        assertEquals(1000, account2.getBalance());
    }

    @Test
    void testMultipleAccountsTransfer() throws InterruptedException {
        Bank bank = new Bank();
        BankAccount[] accounts = new BankAccount[5];
        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = new BankAccount(i, 1000);
        }

        int threadCount = 5;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int fromIndex = i;
            new Thread(() -> {
                try {
                    for (int j = 0; j < 50; j++) {
                        int toIndex = (fromIndex + 1) % accounts.length;
                        bank.sendToAccount(accounts[fromIndex], accounts[toIndex], 10);
                    }
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await(5, TimeUnit.SECONDS);

        int totalBalance = 0;
        for (BankAccount account : accounts) {
            totalBalance += account.getBalance();
        }

        assertEquals(5000, totalBalance);
    }
}