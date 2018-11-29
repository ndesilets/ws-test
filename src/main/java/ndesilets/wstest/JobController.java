package ndesilets.wstest;

import ndesilets.wstest.interfaces.JobCompletionAlert;
import ndesilets.wstest.models.APIJobCompletion;
import ndesilets.wstest.models.APIResponse;
import ndesilets.wstest.models.Job;
import ndesilets.wstest.models.JobRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;

@Controller
public class JobController {
    private static final Logger log = LoggerFactory.getLogger(JobController.class);
    private static final int NUM_THREADS = 2;

    private SimpMessagingTemplate template;

    private ExecutorService executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(NUM_THREADS);
    private BlockingQueue<Job> pendingQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<Job> processingQueue = new LinkedBlockingQueue<>();

    private JobCompletionAlert callback = new JobCompletionAlert() {
        @Override
        public void onJobComplete(Job job) {
            log.info("[Main] - onJobComplete: {}", job);
            template.convertAndSend("/jobs/completed", new APIResponse(job.getName()));
        }

        @Override
        public void onJobFailure(Job job) {
            log.info("[Main] - onJobFailure: {}", job);
            template.convertAndSend("/jobs/failed", new APIResponse(job.getName()));
        }
    };

    @PostConstruct
    public void initialize() {
        for(int i = 0; i < NUM_THREADS; i++) {
            executor.execute(new ProcessJobThread(i, pendingQueue, processingQueue, callback));
        }
        log.info("[Main] - Started {} background worker(s)", NUM_THREADS);
    }

    @Autowired
    public JobController(SimpMessagingTemplate template) {
        this.template = template;
    }

    private long genRandomWaitTime() {
        return Math.round((Math.random() * 100) % 15); // ~ 0 - 15s
    }

    private void sendJobsStatus() {
        template.convertAndSend("/jobs/status", new APIResponse(pendingQueue.size() + " jobs remaining."));
    }

    private boolean isJobUnique(Job job) {
        return !(pendingQueue.contains(job) || processingQueue.contains(job));
    }

    @MessageMapping("/jobs/new")
    @SendTo("/jobs/pending")
    public APIResponse handleNewJob(JobRequest jobRequest) throws Exception {
        Job job = new Job(jobRequest.getName(), genRandomWaitTime());

        if (isJobUnique(job)) {
            log.info("[Main] - Adding job to queue: " + job.getName());
            pendingQueue.add(job);

            return new APIResponse(job.getName());
        } else {
            log.info("[Main] - Job already exists: " + job.getName());

            return new APIResponse(null);
        }
    }
}
