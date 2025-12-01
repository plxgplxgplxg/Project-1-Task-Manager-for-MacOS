package luyen_java.Bai13;

public class Main {
    public static void main(String[] args) {
        
        Thread t = new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(1000); //dung 1 giay
                } catch (InterruptedException e) {
                    System.out.println("Thread bi gian doan");
                    break;
                }

                System.out.println("Dang chay...");
            }
        });

        t.start();
        System.out.println("Main kthuc nhug thr t van chay //");
    }
}
