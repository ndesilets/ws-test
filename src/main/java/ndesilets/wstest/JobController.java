package ndesilets.wstest;

import ndesilets.wstest.models.APIResponse;
import ndesilets.wstest.models.Job;
import ndesilets.wstest.models.JobRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Controller
public class JobController {
    private static final Logger log = LoggerFactory.getLogger(JobController.class);
    private static final int NUM_THREADS = 2;

    private SimpMessagingTemplate template;

    @Autowired
    private TaskExecutor executor;
    private BlockingQueue<Job> pendingQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<Job> processingQueue = new LinkedBlockingQueue<>();
    private AtomicBoolean processingJobs = new AtomicBoolean(false);

    @PostConstruct
    public void initialize() {
        for(int i = 0; i < NUM_THREADS; i++) {
            executor.execute(new ProcessJobThread(i, pendingQueue, processingQueue));
        }
    }

    @Autowired
    public JobController(SimpMessagingTemplate template) {
        this.template = template;
    }

    private long genRandomWaitTime() {
        return Math.round((Math.random() * 100) % 15); // ~ 0 - 15s
    }

    private void sendJobsStatus() {
        template.convertAndSend("/jobs/status", new APIResponse("test 1234"));
    }

    private boolean isJobUnique(Job job) {
        return !(pendingQueue.contains(job) || processingQueue.contains(job));
    }

    @MessageMapping("/jobs/new")
    @SendTo("/jobs/status")
    public APIResponse handleNewJob(JobRequest jobRequest) throws Exception {
        Job job = new Job(jobRequest.getName(), genRandomWaitTime());

        if (isJobUnique(job)) {
            log.info("Adding job to queue: " + job.getName());
            pendingQueue.add(job);

            return new APIResponse("Job added!");
        } else {
            log.info("Job already exists: " + job.getName());

            return new APIResponse("Job already exists!");
        }
    }
}
