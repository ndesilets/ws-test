package ndesilets.wstest;

import ndesilets.wstest.interfaces.JobCompletionAlert;
import ndesilets.wstest.models.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;

public class ProcessJobThread implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ProcessJobThread.class);
    private int id;
    private BlockingQueue<Job> pendingQueue;
    private BlockingQueue<Job> processingQueue;
    private JobCompletionAlert callback;

    public ProcessJobThread(int id, BlockingQueue<Job> pendingQueue, BlockingQueue<Job> processingQueue, JobCompletionAlert callback) {
        this.id = id;
        this.pendingQueue = pendingQueue;
        this.processingQueue = processingQueue;
        this.callback = callback;
    }

    public void run() {
        try {
            while (true) {
                while (!pendingQueue.isEmpty()) {
                    Job job = pendingQueue.take();
                    log.info("[{}] - Got job: {}", id, job.getName());

                    processingQueue.add(job);

                    Thread.sleep(job.getWork() * 1000);

                    processingQueue.remove(job);
                    log.info("[{}] - Finished job: {} after {}s", id, job.getName(), job.getWork());

                    callback.onJobComplete(job);
                }

                log.info("[{}] - Sleeping...", id);
                Thread.sleep(5000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
