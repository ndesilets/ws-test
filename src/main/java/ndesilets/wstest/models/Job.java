package ndesilets.wstest.models;

import java.util.Objects;

public class Job {
    private String name;
    private long work;

    public Job(String name, long work) {
        this.name = name;
        this.work = work;
    }

    public String getName() {
        return name;
    }

    public long getWork() {
        return work;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return name.equals(job.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
