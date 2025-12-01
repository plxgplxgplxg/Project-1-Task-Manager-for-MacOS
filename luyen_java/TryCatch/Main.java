package luyen_java.TryCatch;

public class Main {
    
    static double divide(int a, int b) {
        try {
            if (b == 0) {
                throw new ArithmeticException();
            }
            return (double) a / b;
        } catch (ArithmeticException e) {
            System.out.println("Khong the chia cho 0");
            return 0;
        }
    }

    public static void main(String[] args) {
        System.out.println(divide(10,2));
        System.out.println(divide(10, 0));
    }
}
