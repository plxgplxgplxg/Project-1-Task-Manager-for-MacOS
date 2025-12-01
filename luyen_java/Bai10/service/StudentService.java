package luyen_java.Bai10.service;

import luyen_java.Bai10.model.Student;

public class StudentService {
    public void printInfo(Student student) {
        System.out.println("Name: " + student.name);
        System.out.println("Age: " + student.age);
    }
}
