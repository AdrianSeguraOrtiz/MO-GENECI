package eagrn.operator.mutationwithrepair.impl;

import eagrn.operator.mutationwithrepair.MutationWithRepair;
import eagrn.operator.repairer.WeightRepairer;
import org.uma.jmetal.operator.mutation.impl.NullMutation;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;
import org.uma.jmetal.util.errorchecking.JMetalException;

public class NullMutationWithRepair extends NullMutation<DoubleSolution> implements MutationWithRepair<DoubleSolution> {
    private WeightRepairer repairer;

    public NullMutationWithRepair(WeightRepairer repairer) {
        this.repairer = repairer;
    }

    @Override
    public DoubleSolution execute(DoubleSolution solution) throws JMetalException {
        repairer.repairSolution(solution);
        return solution;
    }
    
    @Override
    public WeightRepairer getRepairer() {
        return repairer;
    }
}
