
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Run {

    public static void main(String[] args) throws InterruptedException {

        Service service = new Service();
        CookThread[] cookThread = new CookThread[60];
        CustomerThread[] customerThread = new CustomerThread[60];

        for(int i = 0; i < 60; i++){
            cookThread[i] = new CookThread(service);
            customerThread[i] = new CustomerThread(service);

        }
        Thread.sleep(2000);
        for(int i = 0; i < 60; i++){
            cookThread[i].start();
            customerThread[i].start();;
        }
    }
}



/*
 * 厨师服务线程
 */
class CookThread extends Thread {

    private Service service;

    public CookThread(Service service) {
        super();
        this.service = service;
    }

    public void run() {
        service.set();
    }
}
/*
 * 顾客服务线程
 */
class CustomerThread extends Thread {

    private Service service;

    public CustomerThread(Service service) {
        super();
        this.service = service;
    }

    public void run() {
        service.get();
        }
}

/*
 * 用于线程调用方方法
 * 服务于线程
 */
class Service {
    //厨师信号
    volatile private Semaphore setSemaphore = new Semaphore(10);
    //就餐者信号
    volatile private Semaphore getSemaphore = new Semaphore(20);
    volatile private ReentrantLock lock = new ReentrantLock();
    //一个接口，必须通过lock实例一个对象，锁，相当于synchronized 的作用
    volatile private Condition setCondition = lock.newCondition();
    volatile private Condition getCondition = lock.newCondition();
    //创建四个盘子对象用来装厨师产出的数据
    volatile private Object[] producePosition = new Object[4];

    /*
   判断是否有空盘子，空盘子可以启动厨师的线程
     */
    private boolean isEmpty() {

        boolean isEmpty = true;
        for (int i = 0; i < producePosition.length; i++) {
            if (producePosition[i] != null) {
                isEmpty = false;
                break;
            }
        }
        return isEmpty;
    }

    /*
    判断是否是满的，满的就停止厨师的线程
     */
    private boolean isFull() {

        boolean isFull = true;
        for (int i = 0; i < producePosition.length; i++) {
            if (producePosition[i] == null) {
                isFull = false;
                break;
            }
        }
        return isFull;
    }

    /*
    厨师的方法
     */
    public void set() {

        try {
            setSemaphore.acquire();
            lock.lock();
            while (isFull()) {
                //生产者线程等待
                setCondition.await();
            }
            for (int i = 0; i < producePosition.length; i++) {
                if (producePosition[i] == null) {
                    String list[] = {"水果蛋糕", "奶油面包", "肉松面包", "特色蛋糕"};
                    int j = (int) (Math.random() * 4);
                    producePosition[i] = list[j];

                    System.out.println(Thread.currentThread().getName() + "做出了" + producePosition[i]);
                    break;
                }
            }
            getCondition.signalAll();
            lock.unlock();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            setSemaphore.release();
        }
    }

    /*
    顾客吃的方法
     */
    public void get() {
        try {
            getSemaphore.acquire();
            lock.lock();
            while (isEmpty()) {
                //没有菜，顾客等待
                getCondition.await();
            }
            for (int i = 0; i < producePosition.length; i++) {
                if (producePosition[i] != null) {
                    System.out.println(Thread.currentThread().getName() + "消费了" + producePosition[i]);
                    producePosition[i] = null;
                    break;
                }
            }
            setCondition.signalAll();
            lock.unlock();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            getSemaphore.release();
        }
    }
}




