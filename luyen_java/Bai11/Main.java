package luyen_java.Bai11;

public class Main {
    
    static double sqrt(double x) {
        if (x < 0) {
            throw new IllegalArgumentException("Khong the lay can bac 2 so am");
        }
        return Math.sqrt(x);
    }

    public static void main(String[] args) {
        
        try {
            System.out.println(sqrt(9));
            System.out.println(sqrt(-5));
        } catch (IllegalArgumentException e) {
            System.out.println("Loi xay ra: " + e.getMessage());
        }
    }
}
