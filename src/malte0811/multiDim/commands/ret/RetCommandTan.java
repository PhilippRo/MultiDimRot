package malte0811.multiDim.commands.ret;

import malte0811.multiDim.addons.ReturningCommand;
import malte0811.multiDim.commands.programs.Programm;

public class RetCommandTan extends ReturningCommand {

	@Override
	public String getRetCommandName() {
		return "TAN";
	}

	@Override
	public String getRetCommandUsage() {
		return "\"tan(x)\" returns the tangens of x (x in radians)";
	}

	@Override
	public double processCommand(String[] args) {
		return Math.tan(Programm.getDoubleValue(args[0]));
	}

	@Override
	public int getMinParameterCount() {
		return 1;
	}

}
