package luyen_java.Bai14;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        //thread chay theo lich
        ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();

        ex.scheduleAtFixedRate(() -> {
            System.out.println("Update data...");
        }, 
        0,
        2,
        TimeUnit.SECONDS);

        System.out.println("Main kthuc nhug scheduler vx chay");
    }
}
