package jobshop.solvers;

import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.List;

public class GreedyESTSPTSolver extends GreedySolver {

    @Override
    protected Task getHighestPriorityTask(List<Task> tasks, ResourceOrder resourceOrder, int[][] tasksState) {

        Task highestPriorityTask = null ;
        int currentSmallestStartTime = Integer.MAX_VALUE ;

        for( Task task : tasks){

            int machine = resourceOrder.instance.machine(task.job,task.task) ;

            int startTime = resourceOrder.nextFreeTimeResource[machine] ;

            if(startTime < currentSmallestStartTime){
                currentSmallestStartTime = startTime ;
                highestPriorityTask = task ;
            }
            else if(startTime == currentSmallestStartTime){

                int HPTTaskDuration = resourceOrder.instance.duration(highestPriorityTask.job,highestPriorityTask.task) ;
                int currentTaskDuration =  resourceOrder.instance.duration(task.job,task.task) ;

                if(currentTaskDuration < HPTTaskDuration){
                    currentSmallestStartTime = startTime ;
                    highestPriorityTask = task ;
                }
            }

        }
        return highestPriorityTask;
    }
}