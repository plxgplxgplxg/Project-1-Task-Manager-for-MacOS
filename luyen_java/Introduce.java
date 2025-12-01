package luyen_java;

public class Introduce {
    public static void main(String[] args) {
        
        Student s1 = new Student("Hoa", 20);
        Student s2 = new Student("An", 20);
        Student s3 = new Student("Bao", 21);

        s1.introduce();
        s2.introduce();
        s3.introduce();
    }
}
