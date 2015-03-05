package malte0811.multiDim.commands;

import malte0811.multiDim.addons.Command;
import malte0811.multiDim.addons.DimRegistry;
import malte0811.multiDim.commands.programs.Programm;

public class CommandRotInst extends Command {

	@Override
	public String getCommandName() {
		return "ROTINST";
	}

	@Override
	public void processCommand(String[] args) {
		if (args.length != 3) {
			System.out.println("3 arguments are required");
			return;
		}
		int a1 = (int) Programm.getValue(args[0]);
		int a2 = (int) Programm.getValue(args[1]);
		int degree = (int) Programm.getValue(args[2]);
		DimRegistry.getCalcThread().solid.rotate(a1, a2, degree);
	}

	@Override
	public String getCommandUsage() {
		return "\"rotinst <axis1> <axis2> <degrees>\" rotates the solid once degrees degrees in the plane defined by axis1 and axis2";
	}

}