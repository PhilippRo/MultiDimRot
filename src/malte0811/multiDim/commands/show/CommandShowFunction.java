package malte0811.multiDim.commands.show;

import java.util.HashMap;

import malte0811.multiDim.addons.Command;
import malte0811.multiDim.addons.DimRegistry;
import malte0811.multiDim.commands.programs.MathHelper;
import malte0811.multiDim.commands.programs.Programm;
import malte0811.multiDim.solids.TMPSolid;

public class CommandShowFunction extends Command {

	@Override
	public String getCommandName() {
		return "SHOWFUNCTION";
	}

	@Override
	public String getCommandUsage() {
		return "\"showFunction <dimensions> <variables> |variables|*{min max step} |dimensions-variables|*{term}\""
				+ "shows an n-dimensional function. Parameters:\r\n"
				+ "dimensions: the amount of dimensions this function has.\r\n"
				+ "variables: how many variables the function has/how many dimensions the grid the function is calculated on has\r\n"
				+ "|variables|*{min max step}: the parameters for the grid, therefore where the grid begins, ends and what resolution it has in each direction\r\n"
				+ "|dimensions-variables|*{term}: the formulas to calculate the positions on the other axes based on the position in the grid. The variables _0, _1, ..., _(dimensions-1) store the position in the grid.\r\n"
				+ "Example: showFunction 3 2 -1 1 0.1 -2 2 0.1 _0*_1";
	}

	@Override
	public int getMinParameterCount() {
		return 6;
	}

	@Override
	public void processCommand(String[] args) throws Exception {
		int dim = (int) Programm.getDoubleValue(args[0]);
		int var = (int) Programm.getDoubleValue(args[1]);
		double[] min = new double[var];
		double[] max = new double[var];
		double[] step = new double[var];
		double[] curr = new double[var];
		for (int i = 0; i < var; i++) {
			min[i] = Programm.getDoubleValue(args[2 + 3 * i]);
			curr[i] = min[i];
			max[i] = Programm.getDoubleValue(args[3 + 3 * i]);
			step[i] = Programm.getDoubleValue(args[4 + 3 * i]);
		}
		int[] lengths = new int[var];
		for (int i = 0; i < min.length; i++) {
			lengths[i] = (int) Math.floor((max[i] - min[i]) / step[i]) + 1;
		}
		double[][] vertices = new double[vertexCount(lengths, -1)][dim];
		int[][] edges = new int[edgeCount(lengths)][2];
		Programm c = DimRegistry.getCalcThread().getCurrentProgram();
		HashMap<String, Double> vars = (c == null) ? new HashMap<String, Double>()
				: c.getDoubleVariables();
		int v = 0;
		do {
			for (int i = 0; i < curr.length; i++) {
				vars.put("_" + i, curr[i]);
			}
			for (int i = 0; i < dim; i++) {
				if (i < var) {
					vertices[v][i] = curr[i];
				} else {
					vertices[v][i] = MathHelper.calculate(
							args[2 + 2 * var + i], vars);
				}
			}
			v++;
		} while (!step(curr, min, max, step));
		if (c != null) {
			for (int i = 0; i < curr.length; i++) {
				if (vars.containsKey("_" + i)) {
					vars.remove("_" + i);
				}
			}
		}

		int edge = 0;
		for (int d = 0; d < var; d++) {
			int prod = prod(lengths, 0, d);
			for (int i = 0; i < vertices.length; i++) {
				if (i + prod < vertices.length
						&& (((i + prod) / prod) % lengths[d] >= (i / prod)
								% lengths[d])) {
					edges[edge][0] = i;
					edges[edge][1] = i + prod;
					edge++;
				}
			}
		}
		DimRegistry.getCalcThread().setSolid(
				new TMPSolid(edges, vertices, null));
	}

	public boolean step(double[] curr, double[] min, double[] max, double[] step) {
		for (int i = 0; i < curr.length; i++) {
			curr[i] += step[i];
			if (curr[i] > max[i]) {
				curr[i] = min[i];
			} else {
				return false;
			}
		}
		return true;
	}

	private int vertexCount(int[] lengths, int exclude) {
		int ret = 1;
		for (int i = 0; i < lengths.length; i++) {
			if (i != exclude) {
				ret *= lengths[i];
			}
		}
		return ret;
	}

	private int edgeCount(int[] step) {
		int ret = 0;
		for (int i = 0; i < step.length; i++) {
			ret += vertexCount(step, i) * (step[i] - 1);
		}
		return ret;
	}

	private int prod(int[] in, int start, int end) {
		int ret = 1;
		for (int i = start; i < Math.min(end, in.length); i++) {
			ret *= in[i];
		}
		return ret;
	}
}
