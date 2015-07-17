package malte0811.multiDim.commands;

import malte0811.multiDim.addons.Command;
import malte0811.multiDim.addons.DimRegistry;
import malte0811.multiDim.commands.programs.Programm;

public class CommandMirror extends Command {

	@Override
	public String getCommandName() {
		return "MIRROR";
	}

	@Override
	public String getCommandUsage() {
		return "\"mirror [dimension]\" mirrors the solid. If no dimension  is given, the solid will be mirrored in every direction, otherwise all coordinates of the given dimension are multiplied by -1.";
	}

	@Override
	public int getMinParameterCount() {
		return 0;
	}

	@Override
	public void processCommand(String[] args) throws Exception {
		double[][] v = DimRegistry.getCalcThread().getSolid().getVertices();
		if (args.length >= 1) {
			int a = (int) Programm.getDoubleValue(args[0]);
			for (int ver = 0; ver < v.length; ver++) {
				v[ver][a] *= -1;
			}
		} else {
			for (int ver = 0; ver < v.length; ver++) {
				for (int i = 0; i < v[ver].length; i++) {
					v[ver][i] *= -1;
				}
			}
		}
	}

}
