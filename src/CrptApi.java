import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CrptApi {
    private final TimeUnit timeUnit;
    private final int requestLimit;
    private final Object monitor = new Object();
    private int requestCount;
    private long lastRequestTime;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
        this.requestCount = 0;
        this.lastRequestTime = System.currentTimeMillis();
    }

    public void makeRequest() throws InterruptedException {
        synchronized (monitor){
            long now = System.currentTimeMillis();
            long timeElapsed = now - lastRequestTime;

            if(timeElapsed < timeUnit.toMillis(1)){
                if(requestCount >= requestLimit){
                    long timeToWait = timeUnit.toMillis(1) - timeElapsed;
                    monitor.wait(timeToWait);
                    // После ожидания пересчитываем количество запросов и время последнего запроса
                    requestCount = 0;
                    lastRequestTime = System.currentTimeMillis();
                }
            }else {
                // Если прошло больше времени, чем определено timeUnit, пересчитываем количество запросов и время последнего запроса
                requestCount = 0;
                lastRequestTime = now;
            }
            requestCount++;
        }
        // Выполнение запроса к API
        System.out.println("API request made.");
    }
    public static void main(String[] args) {
        CrptApi api = new CrptApi(TimeUnit.SECONDS, 2);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 10; i++) {
            executor.execute(() -> {
                try {
                    api.makeRequest();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        executor.shutdown();
    }
}