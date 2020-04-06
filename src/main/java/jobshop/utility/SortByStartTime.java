package jobshop.utility;

import jobshop.Schedule;
import jobshop.encodings.Task;

import java.util.Comparator;

public class SortByStartTime implements Comparator<Task> {

    private final Schedule schedule ;

     public SortByStartTime(Schedule schedule){
        this.schedule = schedule ;
     }

    @Override
    public int compare(Task t1, Task t2) {
        return schedule.startTime(t1.job,t1.task) - schedule.startTime(t2.job,t2.task) ;
    }
}
