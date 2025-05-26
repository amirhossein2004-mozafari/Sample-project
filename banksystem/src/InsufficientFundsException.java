public class InsufficientFundsException extends Exception {

    public InsufficientFundsException(String message) {
        super(message);
    }


    public InsufficientFundsException() {
        super("موجودی حساب کافی نیست");
    }
}