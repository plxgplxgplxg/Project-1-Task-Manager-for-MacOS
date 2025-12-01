package luyen_java.Bai12;

public class Main {
    public static void main(String[] args) {
        
        Car car = new Car();

        car.setBrand("Toyota");
        car.setSpeed(120);

        System.out.println("Brand: " + car.getBrand());
        System.out.println("Speed: " + car.getSpeed());
    }
}
