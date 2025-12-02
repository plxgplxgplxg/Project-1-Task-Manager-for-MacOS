package luyen_java.Bai17;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader("luyen_java/Bai17/data.txt"));

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            reader.close();
        } catch (IOException e) {
            System.out.println("Loi doc file: " + e.getMessage());
        }
    }
}
