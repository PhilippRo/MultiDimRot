package malte0811.multiDim.commands;

import malte0811.multiDim.addons.Command;
import malte0811.multiDim.addons.DimRegistry;
import malte0811.multiDim.commands.programs.Programm;

public class CommandZoom extends Command {

	@Override
	public String getCommandName() {
		return "ZOOM";
	}

	@Override
	public void processCommand(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("2 arguments are required");
			return;
		}
		double max = Programm.getValue(args[0]);
		double step = Programm.getValue(args[1]);
		DimRegistry.getCalcThread().zoomMax = max;
		DimRegistry.getCalcThread().zoomStep = step;
	}

	@Override
	public String getCommandUsage() {
		return "\"zoom <max> <step>\" zooms in/out in steps of step 10 times per second until max is reached";
	}

}