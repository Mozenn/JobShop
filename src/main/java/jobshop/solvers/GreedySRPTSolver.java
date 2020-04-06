package jobshop.solvers;

import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.List;

public class GreedySRPTSolver extends GreedySolver {
    @Override
    protected Task getHighestPriorityTask(List<Task> tasks, ResourceOrder resourceOrder, int[][] tasksState) {

        int currentSRPT =  Integer.MAX_VALUE ;
        Task highestPriorityTask = null ;

        for( Task task : tasks){

            int RPT = 0 ;
            int currentJob = task.job ;

            // Calculate time remaining for current job
            for(int t = 0 ; t < resourceOrder.instance.numTasks ; t++){

                int machine = resourceOrder.instance.machine(currentJob,t) ;

                if(tasksState[currentJob][t] == -1 ){
                    RPT += resourceOrder.instance.duration(currentJob,t) ;
                }
            }


            if(RPT < currentSRPT){
                currentSRPT = RPT ;
                highestPriorityTask = task ;
            }

        }
        return highestPriorityTask;
    }
}
