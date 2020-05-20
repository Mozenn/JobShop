package jobshop.solver;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.solvers.DescentSolver;
import jobshop.solvers.GreedySRPTSolver;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class DescentSolverTests {

    @Test
    public void testDescentSolverValidity1() throws IOException {

        Instance instance = Instance.fromFile(Paths.get("instances/ft20"));

        Solver solver = new DescentSolver();

        Result r = solver.solve(instance,Long.MAX_VALUE) ;

        assert r.schedule.isValid() ;

    }
}
