package jobshop.encodings;

import jobshop.Encoding;
import jobshop.Instance;
import jobshop.Schedule;

/*

import java.util.Arrays;
import java.util.Comparator;

public class ResourceOrder extends Encoding {

    public final Task resources[][] ;

    // Indicate the current start time for each machine
    public int[] nextFreeTimeResource ;

    public ResourceOrder(Instance instance) {
        super(instance);

        this.resources = new Task[instance.numMachines][instance.numJobs];

        for(int index = 0 ; index < resources.length ; index++){
            Arrays.fill(resources[index],new Task(-1,-1)) ;
        }

        nextFreeTimeResource = new int[instance.numMachines];
        Arrays.fill(nextFreeTimeResource,0) ;
    }

    // plan a task for a given machine at the smallest rank available
    public void addTask(Task task, int machine) {

        for (int rank = 0; rank < resources[machine].length; rank++) {
            if (resources[machine][rank].job == -1) {
                resources[machine][rank] = task;
                nextFreeTimeResource[machine] += instance.duration(task.job,task.task);
                break;
            }
        }
    }
*/

/*
    @Override
    public Schedule toSchedule() {

        int[][] startTimes = new int[instance.numJobs][instance.numTasks];

        for( int[] task : startTimes){
            Arrays.fill(task,-1) ;
        }

        Arrays.fill(this.nextFreeTimeResource,0) ;

        boolean bDone = false ;

        while(!bDone){

            bDone = true ;

            for(int machine = 0 ; machine < this.resources.length ; machine++){

                for(int order = 0 ; order < this.resources[machine].length ; order++){

                    Task currentTask = this.resources[machine][order] ;

                    if(!isTaskExecuted(startTimes,currentTask)){

                        if(order == 0 || isTaskExecuted(startTimes,this.resources[machine][order-1])){ // if predecessor on resource is executed

                            if(currentTask.task == 0 || startTimes[currentTask.job][currentTask.task-1] != -1){ // if predecessor on Job is executed
                                bDone = false ;

                                int est = currentTask.task == 0 ? 0 : startTimes[currentTask.job][currentTask.task-1] + instance.duration(currentTask.job, currentTask.task-1);
                                est = Math.max(est, this.nextFreeTimeResource[machine]);

                                startTimes[currentTask.job][currentTask.task] = est ;
                                this.nextFreeTimeResource[machine] = est + instance.duration(currentTask.job,currentTask.task) ;
                            }
                        }
                    }
                }
            }
        }



        return new Schedule(instance, startTimes);
    }

    private boolean isTaskExecuted(int[][] startTimes, Task task){
        return startTimes[task.job][task.task] != -1 ;
    }

    @Override
    public void fromSchedule(Schedule schedule) {

        Task[][] tasks = new Task[instance.numMachines][instance.numJobs] ;

        for(int job = 0 ; job < instance.numJobs ; job++){

            for(int task = 0 ; task < instance.numTasks ; task++){

                Task currentTask = new Task(job,task) ;

                tasks[instance.machine(job,task)][job] = currentTask ;
            }
        }

        for(int machine = 0 ; machine < tasks.length ; machine++){


            Arrays.sort(tasks[machine], Comparator.comparingInt(t -> schedule.startTime(t.job, t.task))) ;


            this.resources[machine] = tasks[machine] ;
        }
    }

    @Override
    public String toString(){

        StringBuilder sb = new StringBuilder() ;

        for(int j = 0 ; j < resources.length ; j++){

            sb.append("Machine "+ j + " : \n") ;

            for(int i = 0 ; i < resources[j].length ; i++){

                sb.append("   Task "+ i + " : " + resources[j][i] + "\n") ;
            }
        }

        return sb.toString() ;
    }


}
*/
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.IntStream;

public class ResourceOrder extends Encoding {

    // for each machine m, taskByMachine[m] is an array of tasks to be
    // executed on this machine in the same order
    public final Task[][] tasksByMachine;

    // for each machine, indicate on many tasks have been initialized
    public final int[] nextFreeSlot;

    /** Creates a new empty resource order. */
    public ResourceOrder(Instance instance)
    {
        super(instance);

        // matrix of null elements (null is the default value of objects)
        tasksByMachine = new Task[instance.numMachines][instance.numJobs];

        // no task scheduled on any machine (0 is the default value)
        nextFreeSlot = new int[instance.numMachines];
    }

    /** Creates a resource order from a schedule. */
    public ResourceOrder(Schedule schedule)
    {
        super(schedule.pb);
        Instance pb = schedule.pb;

        this.tasksByMachine = new Task[pb.numMachines][];
        this.nextFreeSlot = new int[instance.numMachines];

        for(int m = 0 ; m<schedule.pb.numMachines ; m++) {
            final int machine = m;

            // for thi machine, find all tasks that are executed on it and sort them by their start time
            tasksByMachine[m] =
                    IntStream.range(0, pb.numJobs) // all job numbers
                            .mapToObj(j -> new Task(j, pb.task_with_machine(j, machine))) // all tasks on this machine (one per job)
                            .sorted(Comparator.comparing(t -> schedule.startTime(t.job, t.task))) // sorted by start time
                            .toArray(Task[]::new); // as new array and store in tasksByMachine

            // indicate that all tasks have been initialized for machine m
            nextFreeSlot[m] = instance.numJobs;
        }
    }

    @Override
    public Schedule toSchedule() {
        // indicate for each task that have been scheduled, its start time
        int [][] startTimes = new int [instance.numJobs][instance.numTasks];

        // for each job, how many tasks have been scheduled (0 initially)
        int[] nextToScheduleByJob = new int[instance.numJobs];

        // for each machine, how many tasks have been scheduled (0 initially)
        int[] nextToScheduleByMachine = new int[instance.numMachines];

        // for each machine, earliest time at which the machine can be used
        int[] releaseTimeOfMachine = new int[instance.numMachines];


        // loop while there remains a job that has unscheduled tasks
        while(IntStream.range(0, instance.numJobs).anyMatch(m -> nextToScheduleByJob[m] < instance.numTasks)) {

            // selects a task that has noun scheduled predecessor on its job and machine :
            //  - it is the next to be schedule on a machine
            //  - it is the next to be scheduled on its job
            // if there is no such task, we have cyclic dependency and the solution is invalid
            Optional<Task> schedulable =
                    IntStream.range(0, instance.numMachines) // all machines ...
                    .filter(m -> nextToScheduleByMachine[m] < instance.numJobs) // ... with unscheduled jobs
                    .mapToObj(m -> this.tasksByMachine[m][nextToScheduleByMachine[m]]) // tasks that are next to schedule on a machine ...
                    .filter(task -> task.task == nextToScheduleByJob[task.job])  // ... and on their job
                    .findFirst(); // select the first one if any

            if(schedulable.isPresent()) {
                // we found a schedulable task, lets call it t
                Task t = schedulable.get();
                int machine = instance.machine(t.job, t.task);

                // compute the earliest start time (est) of the task
                int est = t.task == 0 ? 0 : startTimes[t.job][t.task-1] + instance.duration(t.job, t.task-1);
                est = Math.max(est, releaseTimeOfMachine[instance.machine(t)]);
                startTimes[t.job][t.task] = est;

                // mark the task as scheduled
                nextToScheduleByJob[t.job]++;
                nextToScheduleByMachine[machine]++;
                // increase the release time of the machine
                releaseTimeOfMachine[machine] = est + instance.duration(t.job, t.task);
            } else {
                // no tasks are schedulable, there is no solution for this resource ordering
                return null;
            }
        }
        // we exited the loop : all tasks have been scheduled successfully
        return new Schedule(instance, startTimes);
    }

    /** Creates an exact copy of this resource order. */
    public ResourceOrder copy() {
        return new ResourceOrder(this.toSchedule());
    }

    @Override
    public void fromSchedule(Schedule schedule) {

        Task[][] tasks = new Task[instance.numMachines][instance.numJobs] ;

        for(int job = 0 ; job < instance.numJobs ; job++){

            for(int task = 0 ; task < instance.numTasks ; task++){

                Task currentTask = new Task(job,task) ;

                tasks[instance.machine(job,task)][job] = currentTask ;
            }
        }

        for(int machine = 0 ; machine < tasks.length ; machine++){


            Arrays.sort(tasks[machine], Comparator.comparingInt(t -> schedule.startTime(t.job, t.task))) ;


            this.tasksByMachine[machine] = tasks[machine] ;
        }
    }

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        for(int m=0; m < instance.numMachines; m++)
        {
            s.append("Machine ").append(m).append(" : ");
            for(int j=0; j<instance.numJobs; j++)
            {
                s.append(tasksByMachine[m][j]).append(" ; ");
            }
            s.append("\n");
        }

        return s.toString();
    }

}
