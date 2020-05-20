package jobshop.encodings;

import jobshop.Encoding;
import jobshop.Instance;
import jobshop.Schedule;
import java.util.stream.IntStream ;


import java.util.Arrays;
import java.util.Comparator;

public class ResourceOrder extends Encoding {

    public final Task resources[][] ;

    /* Indicate the current start time for each machine*/
    public int[] nextFreeTimeResource ;

    public int[][] startTimes ;

    // for each machine, indicate on many tasks have been initialized
    final int[] nextFreeSlot;

    public ResourceOrder(Instance instance) {
        super(instance);

        this.resources = new Task[instance.numMachines][instance.numJobs];

        for(int index = 0 ; index < resources.length ; index++){
            Arrays.fill(resources[index],new Task(-1,-1)) ;
        }

        nextFreeTimeResource = new int[instance.numMachines];
        Arrays.fill(nextFreeTimeResource,0) ;

        startTimes = new int[instance.numJobs][instance.numTasks];

        nextFreeSlot = new int[instance.numMachines];
    }

    public ResourceOrder(Schedule schedule)
    {
        super(schedule.pb);
        Instance pb = schedule.pb;

        this.resources = new Task[pb.numMachines][];
        this.nextFreeSlot = new int[instance.numMachines];
        nextFreeTimeResource = new int[instance.numMachines];
        Arrays.fill(nextFreeTimeResource,0) ;


        for(int m = 0 ; m<schedule.pb.numMachines ; m++) {
            final int machine = m;

            // for thi machine, find all tasks that are executed on it and sort them by their start time
            resources[m] =
                    IntStream.range(0, pb.numJobs) // all job numbers
                            .mapToObj(j -> new Task(j, pb.task_with_machine(j, machine))) // all tasks on this machine (one per job)
                            .sorted(Comparator.comparing(t -> schedule.startTime(t.job, t.task))) // sorted by start time
                            .toArray(Task[]::new); // as new array and store in tasksByMachine

            // indicate that all tasks have been initialized for machine m
            nextFreeSlot[m] = instance.numJobs;
        }
    }

    /* plan a task for a given machine at the smallest rank available*/
    public void addTask(Task task, int machine) {

        for (int rank = 0; rank < resources[machine].length; rank++) {
            if (resources[machine][rank].job == -1) {
                resources[machine][rank] = task;

                int est = task.task == 0 ? 0 : startTimes[task.job][task.task-1] + instance.duration(task.job, task.task-1);
                est = Math.max(est, this.nextFreeTimeResource[machine]);

                startTimes[task.job][task.task] = est ;

                nextFreeTimeResource[machine] = est + instance.duration(task.job,task.task);

                nextFreeSlot[machine] +=1 ;
                break;
            }
        }
    }

    public int getIndex(Task task) {

        int machine = instance.machine(task) ;

        for (int rank = 0; rank < resources[machine].length; rank++) {
            if (resources[machine][rank].equals(task)) {
                return rank ;
            }
        }

        return -1 ;
    }

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

        for(int m = 0 ; m<schedule.pb.numMachines ; m++) {
            final int machine = m;


            // for thi machine, find all tasks that are executed on it and sort them by their start time
            resources[m] =
                    IntStream.range(0, this.instance.numJobs) // all job numbers
                            .mapToObj(j -> new Task(j, this.instance.task_with_machine(j, machine))) // all tasks on this machine (one per job)
                            .sorted(Comparator.comparing(t -> schedule.startTime(t.job, t.task))) // sorted by start time
                            .toArray(Task[]::new); // as new array and store in tasksByMachine

            // indicate that all tasks have been initialized for machine m
            nextFreeSlot[m] = instance.numJobs;
        }

    }

    /** Creates an exact copy of this resource order. */
    public ResourceOrder copy() {
        return new ResourceOrder(this.toSchedule());
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
