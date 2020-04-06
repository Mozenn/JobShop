package jobshop.utility;

import java.util.Comparator;

import jobshop.Instance ;
import jobshop.encodings.Task;

public class SortByDuration implements Comparator<Task> {

    private final Instance instance ;

    public SortByDuration(Instance instance){
        this.instance = instance ;
    }

    @Override
    public int compare(Task t1, Task t2) {
        return instance.duration(t1.job,t1.task) - instance.duration(t2.job,t2.task) ;
    }
}
