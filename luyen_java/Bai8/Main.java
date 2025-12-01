package luyen_java.Bai8;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        
        List<Product> products = new ArrayList<>();

        products.add(new Product("iphone", 2000));
        products.add(new Product("Laptop", 1500));
        products.add(new Product("Airpods", 300));

        System.out.println("Danh sach san pham: ");

        for (Product p: products) {
            System.out.println(p.name + " - $" + p.price);
        }
        
    }
}
