package jobshop.solvers;

import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.List;

public class GreedySPTSolver extends GreedySolver {

    @Override
    protected Task getHighestPriorityTask(List<Task> tasks, ResourceOrder resourceOrder, int[][] tasksState) {

        int currentSPT =  Integer.MAX_VALUE ;
        Task highestPriorityTask = null ;

        for( Task task : tasks){

            if(resourceOrder.instance.duration(task.job,task.task) < currentSPT){
                currentSPT = resourceOrder.instance.duration(task.job,task.task) ;
                highestPriorityTask = task ;
            }

        }
        return highestPriorityTask;
    }
}
