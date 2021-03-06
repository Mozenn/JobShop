package jobshop.solvers;

import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.List;

public class GreedyESTLRPTSolver extends GreedySolver {

    @Override
    protected Task getHighestPriorityTask(List<Task> tasks, ResourceOrder resourceOrder, int[][] tasksState) {

        Task highestPriorityTask = null ;
        int currentSmallestStartTime = Integer.MAX_VALUE ;

        for( Task task : tasks){

            int machine = resourceOrder.instance.machine(task.job,task.task) ;

            int est = task.task == 0 ? 0 : resourceOrder.startTimes[task.job][task.task-1] + resourceOrder.instance.duration(task.job, task.task-1);
            int startTime = Math.max(est, resourceOrder.nextFreeTimeResource[machine]);


            if(startTime < currentSmallestStartTime){
                currentSmallestStartTime = startTime ;
                highestPriorityTask = task ;
            }
            else if(startTime == currentSmallestStartTime){

                int RPT = 0 ;
                int HPT_RPT = 0 ;

                // Calculate time remaining for current task job and HPT job
                for(int t = 0 ; t < resourceOrder.instance.numTasks ; t++){

                    if(tasksState[task.job][t] == -1){
                        RPT += resourceOrder.instance.duration(task.job,t) ;
                    }

                    if(tasksState[highestPriorityTask.job][t] == -1){
                        HPT_RPT += resourceOrder.instance.duration(highestPriorityTask.job,t) ;
                    }
                }

                if(RPT > HPT_RPT){
                    highestPriorityTask = task ;
                    currentSmallestStartTime = startTime ;
                }
            }

        }
        return highestPriorityTask;
    }
}
