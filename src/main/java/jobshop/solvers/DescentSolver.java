package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.List;

public class DescentSolver implements Solver {

    /** A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
     * This class identifies a block in a ResourceOrder representation.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The block with : machine = 1, firstTask= 0 and lastTask = 1
     * Represent the task sequence : [(0,2) (2,1)]
     *
     * */
    static class Block {
        /** machine on which the block is identified */
        final int machine;
        /** index of the first task of the block */
        final int firstTask;
        /** index of the last task of the block */
        final int lastTask;

        Block(int machine, int firstTask, int lastTask) {
            this.machine = machine;
            this.firstTask = firstTask;
            this.lastTask = lastTask;
        }
    }

    /**
     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The swam with : machine = 1, t1= 0 and t2 = 1
     * Represent inversion of the two tasks : (0,2) and (2,1)
     * Applying this swap on the above resource order should result in the following one :
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (2,1) (0,2) (1,1)
     * machine 2 : ...
     */
    static class Swap {
        // machine on which to perform the swap
        final int machine;
        // index of one task to be swapped
        final int t1;
        // index of the other task to be swapped
        final int t2;

        Swap(int machine, int t1, int t2) {
            this.machine = machine;
            this.t1 = t1;
            this.t2 = t2;
        }

        /** Apply this swap on the given resource order, transforming it into a new solution. */
        public void applyOn(ResourceOrder order) {

            Task temp = order.resources[this.machine][this.t1] ;
            order.resources[this.machine][this.t1] = order.resources[this.machine][this.t2] ;
            order.resources[this.machine][this.t2] =  temp ;
        }
    }


    @Override
    public Result solve(Instance instance, long deadline) {

        long startTime = System.currentTimeMillis();

        Solver solver = new GreedyESTLRPTSolver() ;
        Schedule bestSchedule  = solver.solve(instance,deadline).schedule ;
        Schedule bestTempSchedule = bestSchedule  ;

        boolean end = false ;

        while(!end /*&& (System.currentTimeMillis() - startTime < deadline)*/){

            ResourceOrder currentOrder = new ResourceOrder(bestSchedule) ;

            List<ResourceOrder> neighbors = getNeighbors(currentOrder) ;

            // Get best neighbor
            Schedule bestNeighbor = null  ;
            int minDuration = Integer.MAX_VALUE ;

            for(ResourceOrder neighbor : neighbors){

                Schedule schedule = neighbor.toSchedule() ;

                if(schedule.makespan() < minDuration){

                    bestNeighbor = schedule ;
                    minDuration = schedule.makespan() ;
                }
            }

            if(bestNeighbor == null  || bestNeighbor.makespan() < bestSchedule.makespan()){
                bestSchedule = bestNeighbor ;
            }
            else{
                end = true ;
            }

        }

        return new Result(instance,bestSchedule,Result.ExitCause.Timeout);
    }

    public List<ResourceOrder> getNeighbors(ResourceOrder order){

        ArrayList<ResourceOrder> result = new ArrayList<>() ;

        List<Block> blocks = blocksOfCriticalPath(order) ;

        for(Block block : blocks){

            List<Swap> swaps = neighbors(block) ;

            for(Swap swap : swaps){

                ResourceOrder newOrder = order.copy() ;

                swap.applyOn(newOrder);

                result.add(newOrder) ;
            }
        }

        return result ;
    }

    /** Returns a list of all blocks of the critical path. */
    List<Block> blocksOfCriticalPath(ResourceOrder order) {

        ArrayList<Block> blocks = new ArrayList<Block>() ;
        Schedule schedule = order.toSchedule() ;

        List<Task> criticalPath = schedule.criticalPath() ;

        int currentMachine = -1;
        int firstTask = -1 ;
        int lastTask = -1 ;

        for(Task task : criticalPath){

            if(schedule.pb.machine(task) == currentMachine){
                lastTask = order.getIndex(task) ;//TODO check return value (use exception ? )
            }
            else{
                if(lastTask != firstTask){
                    blocks.add(new Block(currentMachine,firstTask,lastTask)) ;
                }

                currentMachine = schedule.pb.machine(task) ;
                firstTask = order.getIndex(task) ; //TODO check return value
                lastTask = firstTask ;
            }
        }

        return blocks ;
    }

    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    List<Swap> neighbors(Block block) {

        ArrayList<Swap> swaps = new ArrayList<>() ;

        if(block.lastTask - block.firstTask == 1){ // 2 tasks in block

            swaps.add(new Swap(block.machine,block.firstTask,block.lastTask)) ;
        }
        else{
            swaps.add(new Swap(block.machine,block.firstTask,block.firstTask+1)) ;
            swaps.add(new Swap(block.machine,block.lastTask-1,block.lastTask)) ;
        }

        return swaps ;
    }

}
