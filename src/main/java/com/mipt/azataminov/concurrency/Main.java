package com.mipt.azataminov.concurrency;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Демонстрация работы банковских переводов ===\n");

        Bank bank = new Bank();

        BankAccount alice = new BankAccount(1, 1000);
        BankAccount bob = new BankAccount(2, 500);

        System.out.println("До перевода:");
        System.out.println("Alice баланс: " + alice.getBalance());
        System.out.println("Bob баланс: " + bob.getBalance());

        bank.sendToAccount(alice, bob, 300);

        System.out.println("\nПосле перевода 300 от Alice к Bob:");
        System.out.println("Alice баланс: " + alice.getBalance());
        System.out.println("Bob баланс: " + bob.getBalance());

        System.out.println("\n=== Многопоточные переводы ===");

        BankAccount account1 = new BankAccount(3, 10000);
        BankAccount account2 = new BankAccount(4, 10000);

        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    bank.sendToAccount(account1, account2, 1);
                    bank.sendToAccount(account2, account1, 1);
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("После многопоточных переводов:");
        System.out.println("Account1 баланс: " + account1.getBalance());
        System.out.println("Account2 баланс: " + account2.getBalance());

        System.out.println("\n=== Демонстрация deadlock (запуск с таймаутом) ===");

        BankAccount x = new BankAccount(5, 1000);
        BankAccount y = new BankAccount(6, 1000);

        Thread deadlockThread1 = new Thread(() -> {
            bank.sendToAccountDeadlock(x, y, 100);
        });

        Thread deadlockThread2 = new Thread(() -> {
            bank.sendToAccountDeadlock(y, x, 100);
        });

        deadlockThread1.start();
        deadlockThread2.start();

        deadlockThread1.join(2000);
        deadlockThread2.join(2000);

        if (deadlockThread1.isAlive() || deadlockThread2.isAlive()) {
            System.out.println("Обнаружен potential deadlock!");
        } else {
            System.out.println("Переводы выполнены успешно");
        }
    }
}