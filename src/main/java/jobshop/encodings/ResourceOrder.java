package jobshop.encodings;

import jobshop.Encoding;
import jobshop.Instance;
import jobshop.Schedule;


import java.util.Arrays;
import java.util.Comparator;

public class ResourceOrder extends Encoding {

    public final Task resources[][] ;

    /* Indicate the current start time for each machine*/
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

    /* plan a task for a given machine at the smallest rank available*/
    public void addTask(Task task, int machine) {

        for (int rank = 0; rank < resources[machine].length; rank++) {
            if (resources[machine][rank].job == -1) {
                resources[machine][rank] = task;
                nextFreeTimeResource[machine] += instance.duration(task.job,task.task);
                break;
            }
        }
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
