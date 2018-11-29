package ndesilets.wstest.interfaces;

import ndesilets.wstest.models.Job;

public interface JobCompletionAlert {
    public void onJobComplete(Job job);
    public void onJobFailure(Job job);
}