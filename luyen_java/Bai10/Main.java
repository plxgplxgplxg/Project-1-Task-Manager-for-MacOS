package luyen_java.Bai10;

import luyen_java.Bai10.model.Student;
import luyen_java.Bai10.service.StudentService;

public class Main {
    public static void main(String[] args) {
        
        Student s = new Student("Linh", 19);

        StudentService service = new StudentService();
        service.printInfo(s);
    }
}
