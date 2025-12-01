package luyen_java;

public class Student {
    String name;
    int age;

    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    void introduce() {
        System.out.println("My name is " + name + " and I am " + age + " years old");
    }
}
