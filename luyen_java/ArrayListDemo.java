package luyen_java;

import java.util.ArrayList;
import java.util.List;

public class ArrayListDemo {
    public static void main(String[] args) {
        
        List<String> names = new ArrayList<>();

        names.  add("Linh");
        names.add("An");
        names.add("Hung");
        names.add("Binh");
        names.add("Nam");

        System.out.println("Danh sach ban dau: ");
        System.out.println(names);

        names.remove("Hung");

        System.out.println("ds sau khi xoa hung: ");
        System.out.println(names);
        System.out.println("Duyet ds bang for each");

        for (String name: names) {
            System.out.println(name);
        }
    }
}
