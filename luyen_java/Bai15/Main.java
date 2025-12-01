package luyen_java.Bai15;

public class Main {
    public static void main(String[] args) {
        new Car();
        new Car();
        new Car();

        System.out.println("So xe da tao: " + Car.count);

        System.out.println(MathUtil.sum(5,6));

        MathUtil maths = new MathUtil();
        System.out.println(maths.devide(7, 2));
        }


}
