package ndesilets.wstest.models;

public class APIJobCompletion {
    private Job job;
    private String status;

    public APIJobCompletion(Job job, String status) {
        this.job = job;
        this.status = status;
    }

    public Job getJob() {
        return job;
    }

    public String getStatus() {
        return status;
    }
}
