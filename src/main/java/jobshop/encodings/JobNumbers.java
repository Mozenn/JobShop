package jobshop.encodings;

import jobshop.Encoding;
import jobshop.Instance;
import jobshop.Schedule;
import jobshop.utility.SortByStartTime;

import java.util.ArrayList;
import java.util.Arrays;

/** Représentation par numéro de job. */
public class JobNumbers extends Encoding {

    /** A numJobs * numTasks array containing the representation by job numbers. */
    public int[] jobs;

    /** In case the encoding is only partially filled, indicates the index of first
     * element of `jobs` that has not been set yet. */
    public int nextToSet = 0;

    public JobNumbers(Instance instance) {
        super(instance);

        jobs = new int[instance.numJobs * instance.numMachines];
        Arrays.fill(jobs, -1);
    }

    @Override
    public Schedule toSchedule() {
        // time at which each machine is going to be freed
        int[] nextFreeTimeResource = new int[instance.numMachines];

        // for each job, the first task that has not yet been scheduled
        int[] nextTask = new int[instance.numJobs];

        // for each task, its start time
        int[][] startTimes = new int[instance.numJobs][instance.numTasks];

        // compute the earliest start time for every task of every job
        for(int job : jobs) {
            int task = nextTask[job];
            int machine = instance.machine(job, task);
            // earliest start time for this task
            int est = task == 0 ? 0 : startTimes[job][task-1] + instance.duration(job, task-1);
            est = Math.max(est, nextFreeTimeResource[machine]);

            startTimes[job][task] = est;
            nextFreeTimeResource[machine] = est + instance.duration(job, task);
            nextTask[job] = task + 1;
        }

        return new Schedule(instance, startTimes);
    }

    @Override
    public void fromSchedule(Schedule schedule)
    {
        ArrayList<Task> tasks = new ArrayList<>() ;

        for(int job = 0 ; job < instance.numJobs ; job++){

            for(int task = 0 ; task < instance.numTasks ; task++){

                Task currentTask = new Task(job,task) ;

                tasks.add(currentTask) ;
            }
        }

        tasks.sort(new SortByStartTime(schedule));

        int[] sortedJobs = tasks.stream().mapToInt( task ->  task.job).toArray() ;

        this.jobs = Arrays.copyOf(sortedJobs,sortedJobs.length) ; // TODO loop, fill 1 by one and make jobs final again

    }

    @Override
    public String toString() {
        return Arrays.toString(Arrays.copyOfRange(jobs,0, nextToSet));
    }
}
