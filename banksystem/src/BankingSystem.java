import java.io.IOException;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BankingSystem {
    static Set<String> accountNumbers = new HashSet<>();
    private static final String USERS_FILE = "users.json";
    private List<User> users = new ArrayList<>();
    private User currentUser = null;
    private Scanner scanner = new Scanner(System.in);
    private JsonParser jsonParser = new JsonParser();

    public void start() {
        loadUsers();
        showMainMenu();
    }

    private void loadUsers() {
        try {
            if (Files.exists(Paths.get(USERS_FILE))) {
                users = jsonParser.loadUsers(USERS_FILE);
            }
        } catch (IOException e) {
            System.out.println("خطا در دسترسی به فایل: " + e.getMessage());
        } catch (JsonParser.JsonParseException e) {
            System.out.println("خطا در پردازش فایل: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("خطای ناشناخته: " + e.getMessage());
        }
    }

    private void saveUsers() {
        try {
            jsonParser.saveUsers(users, USERS_FILE);
        } catch (Exception e) {
            System.out.println("خطا در ذخیره کاربران: " + e.getMessage());
        }
    }

    private boolean checkForBackCommand(String input) {
        if (input.equalsIgnoreCase("back")) {
            System.out.println("بازگشت به منوی قبلی...");
            return true;
        }
        return false;
    }

    private void showMainMenu() {
        while (true) {
            System.out.println("\n--- سیستم بانکی ---");
            System.out.println("1. ثبت نام");
            System.out.println("2. ورود");
            System.out.println("3. خروج");
            System.out.print("گزینه را انتخاب کنید (یا back برای بازگشت): ");

            String input = scanner.nextLine().trim();
            if (checkForBackCommand(input)) {
                System.out.println("در حال خروج از برنامه...");
                saveUsers();
                System.exit(0);
            }

            try {
                int choice = Integer.parseInt(input);
                switch (choice) {
                    case 1:
                        handleSignup();
                        break;
                    case 2:
                        handleLogin();
                        break;
                    case 3:
                        saveUsers();
                        System.exit(0);
                    default:
                        System.out.println("گزینه نامعتبر!");
                }
            } catch (NumberFormatException e) {
                System.out.println("گزینه نامعتبر! لطفا عدد وارد کنید.");
            }
        }
    }

    private void handleSignup() {
        System.out.print("نام کاربری (یا back برای بازگشت): ");
        String username = scanner.nextLine();
        if (checkForBackCommand(username)) return;

        System.out.print("رمز عبور (یا back برای بازگشت): ");
        String password = scanner.nextLine();
        if (checkForBackCommand(password)) return;

        if (users.stream().anyMatch(u -> u.getUsername().equals(username))) {
            System.out.println("این نام کاربری قبلاً ثبت شده است");
            return;
        }

        users.add(new User(username, password));
        saveUsers();
        System.out.println("ثبت نام موفق! شماره حساب شما: " +
                users.get(users.size() - 1).getAccountNumber());
    }

    private void handleLogin() {
        System.out.print("نام کاربری (یا back برای بازگشت): ");
        String username = scanner.nextLine();
        if (checkForBackCommand(username)) return;

        System.out.print("رمز عبور (یا back برای بازگشت): ");
        String password = scanner.nextLine();
        if (checkForBackCommand(password)) return;

        for (User user : users) {
            if (user.getUsername().equals(username) && user.validatePassword(password)) {
                currentUser = user;
                showAccountMenu();
                return;
            }
        }
        System.out.println("نام کاربری یا رمز عبور نادرست");
    }

    private void showAccountMenu() {
        while (currentUser != null) {
            System.out.println("\n--- منوی حساب ---");
            System.out.println("1. نمایش موجودی");
            System.out.println("2. واریز");
            System.out.println("3. برداشت");
            System.out.println("4. انتقال وجه");
            System.out.println("5. تاریخچه تراکنش‌ها");
            System.out.println("6. لغو تراکنش");
            System.out.println("7. نمایش شماره حساب");
            System.out.println("8. خروج از حساب");
            System.out.print("گزینه را انتخاب کنید (یا back برای بازگشت): ");

            String input = scanner.nextLine().trim();
            if (checkForBackCommand(input)) {
                currentUser = null;
                return;
            }

            try {
                int choice = Integer.parseInt(input);
                switch (choice) {
                    case 1:
                        showBalance();
                        break;
                    case 2:
                        handleDeposit();
                        break;
                    case 3:
                        handleWithdraw();
                        break;
                    case 4:
                        handleTransfer();
                        break;
                    case 5:
                        showTransactionHistory();
                        break;
                    case 6:
                        handleUndo();
                        break;
                    case 7:
                        showAccountNumber();
                        break;
                    case 8:
                        currentUser = null;
                        return;
                    default:
                        System.out.println("گزینه نامعتبر!");
                }
            } catch (NumberFormatException e) {
                System.out.println("گزینه نامعتبر! لطفا عدد وارد کنید.");
            }
        }
    }

    private void showBalance() {
        Account account = currentUser.getAccounts().get(0);
        System.out.println("موجودی فعلی: " + account.getBalance());
    }

    private void handleDeposit() {
        try {
            System.out.print("مبلغ واریز (یا back برای بازگشت): ");
            String input = scanner.nextLine();
            if (checkForBackCommand(input)) return;
            double amount = Double.parseDouble(input);

            currentUser.getAccounts().get(0).deposit(amount, true);
            saveUsers();
            System.out.println("واریز با موفقیت انجام شد!");
        } catch (NumberFormatException e) {
            System.out.println("خطا: مبلغ وارد شده معتبر نیست");
        } catch (Exception e) {
            System.out.println("خطا: " + e.getMessage());
        }
    }

    private void handleWithdraw() {
        try {
            System.out.print("مبلغ برداشت (یا back برای بازگشت): ");
            String input = scanner.nextLine();
            if (checkForBackCommand(input)) return;
            double amount = Double.parseDouble(input);

            currentUser.getAccounts().get(0).withdraw(amount, true);
            saveUsers();
            System.out.println("برداشت با موفقیت انجام شد!");
        } catch (NumberFormatException e) {
            System.out.println("خطا: مبلغ وارد شده معتبر نیست");
        } catch (InsufficientFundsException e) {
            System.out.println("خطا: موجودی کافی نیست");
        }
    }

    private void handleTransfer() {
        try {
            System.out.print("شماره حساب مقصد (یا back برای بازگشت): ");
            String destAccountNumber = scanner.nextLine();
            if (checkForBackCommand(destAccountNumber)) return;

            if (destAccountNumber.equals(currentUser.getAccountNumber())) {
                throw new Exception("امکان انتقال به خود کاربر وجود ندارد");
            }

            System.out.print("مبلغ انتقال (یا back برای بازگشت): ");
            String amountInput = scanner.nextLine();
            if (checkForBackCommand(amountInput)) return;
            double amount = Double.parseDouble(amountInput);

            User destUser = null;
            for (User user : users) {
                if (user.getAccountNumber().equals(destAccountNumber)) {
                    destUser = user;
                    break;
                }
            }

            if (destUser == null) {
                throw new Exception("شماره حساب معتبر نیست");
            }

            Account sourceAccount = currentUser.getAccounts().get(0);
            Account destAccount = destUser.getAccounts().get(0);

            if (sourceAccount.getBalance() < amount) {
                throw new InsufficientFundsException("موجودی حساب شما کافی نیست");
            }

            sourceAccount.withdraw(amount, true);
            destAccount.deposit(amount, true);

            sourceAccount.getTransactions().add(new Transaction(
                    Transaction.Type.TRANSFER,
                    amount,
                    currentUser.getUsername(),
                    destUser.getUsername()
            ));

            destAccount.getTransactions().add(new Transaction(
                    Transaction.Type.TRANSFER,
                    amount,
                    currentUser.getUsername(),
                    destUser.getUsername()
            ));

            System.out.printf("%,.2f تومان با موفقیت منتقل شد\n", amount);
            saveUsers();
        } catch (NumberFormatException e) {
            System.out.println("خطا: مبلغ وارد شده معتبر نیست");
        } catch (InsufficientFundsException e) {
            System.out.println("خطا: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("خطا در انتقال وجه: " + e.getMessage());
        }
    }

    private void showTransactionHistory() {
        Account account = currentUser.getAccounts().get(0);
        System.out.println("\n--- تاریخچه تراکنش‌ها ---");
        for (Transaction transaction : account.getTransactions()) {
            System.out.printf("[ID: %d] ", transaction.getId());
            String direction = transaction.getAmount() > 0 ? "دریافتی از" : "ارسال به";
            String party = transaction.getAmount() > 0 ?
                    transaction.getSource() :
                    transaction.getDestination();

            System.out.printf("[%s] %s: %.2f تومان (%s %s)%n",
                    transaction.getFormattedTimestamp(),
                    getPersianType(transaction.getType()),
                    Math.abs(transaction.getAmount()),
                    direction,
                    party);
        }
    }

    private void handleUndo() {
        try {
            System.out.print("شناسه تراکنش (یا back برای بازگشت): ");
            String input = scanner.nextLine();
            if (checkForBackCommand(input)) return;
            long transactionId = Long.parseLong(input);

            Account account = currentUser.getAccounts().get(0);
            Transaction transaction = findTransactionById(account, transactionId);

            if (transaction == null) {
                throw new Exception("تراکنشی با این شناسه یافت نشد");
            }

            if (!isTransactionOwned(transaction)) {
                throw new Exception("اجازه لغو این تراکنش را ندارید");
            }

            undoTransaction(transaction);
            account.getTransactions().remove(transaction);
            saveUsers();
            System.out.println("تراکنش با موفقیت لغو شد");

        } catch (NumberFormatException e) {
            System.out.println("خطا: شناسه باید عددی باشد");
        } catch (Exception e) {
            System.out.println("خطا در لغو تراکنش: " + e.getMessage());
        }
    }

    private Transaction findTransactionById(Account account, long id) {
        for (Transaction t : account.getTransactions()) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }

    private boolean isTransactionOwned(Transaction transaction) {
        return transaction.getSource().equals(currentUser.getUsername()) ||
                transaction.getDestination().equals(currentUser.getUsername());
    }

    private void undoTransaction(Transaction transaction) throws Exception {
        switch (transaction.getType()) {
            case DEPOSIT:
                currentUser.getAccounts().get(0).withdraw(transaction.getAmount(), false);
                break;
            case WITHDRAW:
                currentUser.getAccounts().get(0).deposit(transaction.getAmount(), false);
                break;
            case TRANSFER:
                undoTransfer(transaction);
                break;
        }
    }

    private void undoTransfer(Transaction transaction) throws Exception {
        currentUser.getAccounts().get(0).deposit(transaction.getAmount(), false);

        User destUser = null;
        for (User user : users) {
            if (user.getUsername().equals(transaction.getDestination())) {
                destUser = user;
                break;
            }
        }

        if (destUser == null) {
            throw new Exception("کاربر مقصد یافت نشد");
        }

        destUser.getAccounts().get(0).withdraw(transaction.getAmount(), false);
    }

    private void showAccountNumber() {
        System.out.println("شماره حساب شما: " + currentUser.getAccountNumber());
    }

    private String getPersianType(Transaction.Type type) {
        switch (type) {
            case DEPOSIT:
                return "واریز";
            case WITHDRAW:
                return "برداشت";
            case TRANSFER:
                return "انتقال";
            default:
                throw new IllegalArgumentException("نوع ناشناخته");
        }
    }
}