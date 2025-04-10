    import java.io.*;
    import java.util.*;

    public class MonthlyFinanceTracker2 {

        static class Transaction {
            String date;
            String description;
            double amount;
            String type;

            public Transaction(String date, String description, double amount, String type) {
                this.date = date;
                this.description = description;
                this.amount = amount;
                this.type = type;
            }
        }

        public static void main(String[] args) {
            if (args.length < 1) {
                System.out.println("Usage: java MonthlyFinanceTracker2 <transactions.csv>");
                return;
            }

            String filePath = args[0];

            List<Transaction> transactions = readTransactionsFromCSV(filePath);
            if (transactions.isEmpty()) {
                System.out.println("No valid transactions found.");
                return;
            }

            double totalIncome = 0;
            double totalExpense = 0;
            Map<String, Double> categoryTotals = new HashMap<>();

            for (Transaction t : transactions) {
                String type = t.type.toUpperCase().trim();
                if (type.startsWith("INCOME")) {
                    totalIncome += t.amount;
                } else if (type.startsWith("EXPENSE")) {
                    totalExpense += t.amount;

                    if (type.contains("_")) {
                        String[] parts = type.split("_", 2);
                        if (parts.length == 2) {
                            String category = parts[1];
                            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + t.amount);
                        }
                    }
                }
            }

            double netBalance = totalIncome - totalExpense;

            appendSummaryToSameFile(filePath, totalIncome, totalExpense, netBalance, categoryTotals);

            System.out.println("Summary successfully appended to: " + filePath);
        }

        private static List<Transaction> readTransactionsFromCSV(String filePath) {
            List<Transaction> transactions = new ArrayList<>();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))) {
                String line;
                boolean isFirstLine = true;

                while ((line = br.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    String[] values = line.split(",", -1);
                    if (values.length != 4) {
                        continue;
                    }

                    try {
                        String date = values[0].trim();
                        String description = values[1].trim();
                        String amountStr = values[2].trim().replaceAll("[^0-9.\\-]", "");
                        double amount = Double.parseDouble(amountStr);
                        String type = values[3].trim().toUpperCase();

                        transactions.add(new Transaction(date, description, amount, type));
                    } catch (NumberFormatException e) {
                        System.out.println("Skipping line due to number format issue: " + line);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error reading CSV: " + e.getMessage());
            }

            return transactions;
        }

        private static void appendSummaryToSameFile(String filePath, double totalIncome, double totalExpense,
                                                    double netBalance, Map<String, Double> categoryTotals) {
            File file = new File(filePath);

            // Ensure file is not in use
            if (!file.renameTo(file)) {
                System.out.println(" Please close the file (e.g., Excel or Notepad) before running this program.");
                return;
            }

            try (FileOutputStream fos = new FileOutputStream(filePath, true)) {
                // Add BOM if file is empty
                if (file.length() == 0) {
                    fos.write(0xEF);
                    fos.write(0xBB);
                    fos.write(0xBF);
                }

                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"))) {
                    writer.newLine();
                    writer.write("===== EXPENSE BREAKDOWN BY CATEGORY =====");
                    writer.newLine();

                    for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                        writer.write(entry.getKey() + " : " + String.format("%.2f", entry.getValue()));
                        writer.newLine();
                    }

                    writer.write("-----------------------------------------");
                    writer.newLine();
                    writer.write("TOTAL EXPENSE :\t " + String.format("%.2f", totalExpense));
                    writer.newLine();
                    writer.write("TOTAL INCOME  : \t" + String.format("%.2f", totalIncome));
                    writer.newLine();
                    writer.write("NET BALANCE   : \t" + String.format("%.2f", netBalance));
                    writer.newLine();
                    writer.write("=========================================");
                    writer.newLine();
                }

            } catch (IOException e) {
                System.out.println("Error writing summary to file: " + e.getMessage());
            }
        }
    }
