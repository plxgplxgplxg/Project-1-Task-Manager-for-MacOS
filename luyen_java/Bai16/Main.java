package luyen_java.Bai16;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        
        List<Integer> list = new ArrayList<>();

        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        list.add(6);
        list.add(7);
        list.add(8);
        list.add(9);
        
        list.forEach(x -> {
            if (x%2 == 0) {
                System.out.println(x);
            }
        });
    }
}
