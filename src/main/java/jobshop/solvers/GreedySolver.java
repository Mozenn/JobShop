package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class GreedySolver implements Solver {

    @Override
    public Result solve(Instance instance, long deadline) {

        ResourceOrder sol = new ResourceOrder(instance) ;

        int[][] tasksState = new int[instance.numJobs][instance.numTasks];

        for( int[] task : tasksState){
            Arrays.fill(task,-1) ;
        }

        List<Task> remainingTasks = getAllTasks(sol) ;

        while(!remainingTasks.isEmpty()){

            List<Task> executableTasks = getExecutableTasks(sol,remainingTasks,tasksState) ;

            //System.out.println(executableTasks);

            Task task = getHighestPriorityTask(executableTasks,sol,tasksState ) ;
            int machine = instance.machine(task.job,task.task) ;

            sol.addTask(task,machine);

            remainingTasks.remove(task);
            tasksState[task.job][task.task] = 1 ;

        }

        //System.out.println(sol);

        return new Result(instance,sol.toSchedule(),Result.ExitCause.Blocked);
    }

    protected abstract Task getHighestPriorityTask(List<Task> tasks, ResourceOrder resourceOrder, int[][] tasksState) ;

    private List<Task> getExecutableTasks(ResourceOrder resourceOrder, List<Task> remainingTasks,int[][] tasksState){

        ArrayList<Task> tasks = new ArrayList<>() ;

        for( Task task : remainingTasks){

            if(task.task != 0 && tasksState[task.job][task.task-1] == -1)
                continue ;

            tasks.add(task) ;
        }

        return tasks ;
    }

    private List<Task> getAllTasks(ResourceOrder resourceOrder){

        ArrayList<Task> tasks = new ArrayList<>() ;

        for(int job = 0 ; job < resourceOrder.instance.numJobs ; job++){

            for(int task = 0 ; task < resourceOrder.instance.numTasks ; task++){

                tasks.add(new Task(job,task)) ;
            }
        }

        return tasks ;
    }

}
