import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ExpenseTracker {
    private static final List<Transaction> transactions = new ArrayList<>();
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n1. Add Transaction");
            System.out.println("2. Load Transactions from File");
            System.out.println("3. Show Monthly Summary");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");
            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> addTransaction();
                case 2 -> loadFromFile();
                case 3 -> showMonthlySummary("sample_data.txt");
                case 4 -> System.exit(0);
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private static void addTransaction() {
        System.out.print("Enter type (income/expense): ");
        String typeStr = scanner.nextLine().trim().toUpperCase();
        TransactionType type = TransactionType.valueOf(typeStr);

        String category = "";
        if (type == TransactionType.INCOME) {
            System.out.print("Enter category (Salary/Business): ");
        } else {
            System.out.print("Enter category (Food/Rent/Travel): ");
        }
        category = scanner.nextLine();

        System.out.print("Enter amount: ");
        double amount = Double.parseDouble(scanner.nextLine());

        System.out.print("Enter date (yyyy-MM-dd): ");
        LocalDate date = LocalDate.parse(scanner.nextLine(), formatter);

        transactions.add(new Transaction(type, category, amount, date));
        saveTransactionToFile(new Transaction(type, category, amount, date),"sample_data.txt");
        System.out.println("Transaction added.");
    }

    private static void loadFromFile() {
        System.out.print("Enter filename: ");
        String filename = scanner.nextLine();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                TransactionType type = TransactionType.valueOf(parts[0]);
                String category = parts[1];
                double amount = Double.parseDouble(parts[2]);
                LocalDate date = LocalDate.parse(parts[3], formatter);
                System.out.println(type+","+category+","+amount+","+date);
                transactions.add(new Transaction(type, category, amount, date));
            }
            if(transactions.isEmpty()){
                System.out.println("No transaction found!!");
                return;
            }
            System.out.println("File loaded successfully.");
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Failed to load file: " + e.getMessage());
        }
    }

    private static void saveTransactionToFile(Transaction tx, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            String line = String.format("%s,%s,%.2f,%s",
                    tx.getType(), tx.getCategory(), tx.getAmount(), tx.getDate().format(formatter));
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Failed to save transaction: " + e.getMessage());
        }
    }


    private static void showMonthlySummary(String filename) {
        Map<String, Double> incomeMap = new HashMap<>();
        Map<String, Double> expenseMap = new HashMap<>();
        Map<String, Integer> transactionCountMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 4) continue;

                TransactionType type = TransactionType.valueOf(parts[0]);
                double amount = Double.parseDouble(parts[2]);
                LocalDate date = LocalDate.parse(parts[3], formatter);

                String month = date.getYear() + "-" + String.format("%02d", date.getMonthValue());

                transactionCountMap.put(month, transactionCountMap.getOrDefault(month, 0) + 1);

                if (type == TransactionType.INCOME) {
                    incomeMap.put(month, incomeMap.getOrDefault(month, 0.0) + amount);
                } else {
                    expenseMap.put(month, expenseMap.getOrDefault(month, 0.0) + amount);
                }
            }

            Set<String> allMonths = new TreeSet<>();
            allMonths.addAll(incomeMap.keySet());
            allMonths.addAll(expenseMap.keySet());

            System.out.println("\n--- Monthly Summary from File ---");
            System.out.println("Month    | Income    |  Expense    | Balance    | Total Transactions");
            for (String month : allMonths) {
                double income = incomeMap.getOrDefault(month, 0.0);
                double expense = expenseMap.getOrDefault(month, 0.0);
                double balance = income - expense;
                int count = transactionCountMap.getOrDefault(month, 0);

                System.out.printf("%-8s | %10.2f | %10.2f | %10.2f | %6d%n", month, income, expense, balance, count);

            }

        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Failed to read file or parse data: " + e.getMessage());
        }
    }
}
