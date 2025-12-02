package luyen_java.Bai18;

import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        
        try {
            FileWriter writer = new FileWriter("luyen_java/Bai18/output.txt");

            writer.write("Hello Java\n");
            writer.write("Ghi de file thanh cong\n");
            writer.write("Dong thu 3 \n");

            writer.close();

            System.out.println("Da ghi xog");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
