package jobshop.solver;

import jobshop.Instance;
import jobshop.Solver;
import jobshop.solvers.BasicSolver;
import jobshop.solvers.GreedySPTSolver;
import jobshop.solvers.GreedySRPTSolver;
import jobshop.solvers.GreedyLRPTSolver;
import jobshop.Result ;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class GreedySolverTests {

    @Test
    public void testSRPTSolverValidity1() throws IOException {

        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        Solver greedySolver = new GreedySRPTSolver() ;

        Result r = greedySolver.solve(instance,Long.MAX_VALUE) ;

        assert r.schedule.isValid() ;

    }

    @Test
    public void testSRPTSolverValidity2() throws IOException {

        Instance instance = Instance.fromFile(Paths.get("instances/ft06"));

        Solver greedySolver = new GreedySRPTSolver() ;

        Result r = greedySolver.solve(instance,Long.MAX_VALUE) ;

//        System.out.println(r.schedule);
//        System.out.println(r.schedule.makespan());

        assert r.schedule.isValid();

    }

    @Test
    public void testSRPTSolverValidity3() throws IOException {

        Instance instance = Instance.fromFile(Paths.get("instances/ft10"));

        Solver greedySolver = new GreedySRPTSolver() ;

        Result r = greedySolver.solve(instance,Long.MAX_VALUE) ;

        assert r.schedule.isValid() ;

    }

    @Test
    public void testLRPTSolverValidity1() throws IOException {

        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));
        Solver greedySolver = new GreedyLRPTSolver() ;

        Result r = greedySolver.solve(instance,Long.MAX_VALUE) ;

        assert r.schedule.isValid() ;

    }

    @Test
    public void testSPTSolverValidity1() throws IOException {

        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));
        Solver greedySolver = new GreedySPTSolver() ;

        Result r = greedySolver.solve(instance,Long.MAX_VALUE) ;

        assert r.schedule.isValid() ;

    }

    @Test
    public void testSPTSolverValidity2() throws IOException {

        Instance instance = Instance.fromFile(Paths.get("instances/ft06"));
        Solver greedySolver = new GreedySPTSolver() ;

        Result r = greedySolver.solve(instance,Long.MAX_VALUE) ;

        System.out.println(r.schedule.makespan());

        assert r.schedule.isValid() ;

    }

    @Test
    public void testSPTSolverValidity3() throws IOException {

        Instance instance = Instance.fromFile(Paths.get("instances/ft10"));
        Solver greedySolver = new GreedySPTSolver() ;

        Result r = greedySolver.solve(instance,Long.MAX_VALUE) ;

        assert r.schedule.isValid() ;

    }

}
