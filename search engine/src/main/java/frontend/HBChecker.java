package frontend;

public class HBChecker extends Thread {

    public HBChecker() {}

    public void run() {
        while (true) {
            try {
                Thread.sleep(8000);
                LoadBalancer.getUpdate_lock().acquire();

                for (int i = LoadBalancer.getWorker_li().size() - 1; i >= 0; i--) {
                    String s = LoadBalancer.getWorker_li().get(i);
                    SEController info = LoadBalancer.getWorker_map().get(s);
                    if (info == null || !info.checkAlive()) {
                        LoadBalancer.getWorker_li().remove(i);
                        LoadBalancer.getWorker_map().remove(s);
                    }

                }
                LoadBalancer.getUpdate_lock().release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}