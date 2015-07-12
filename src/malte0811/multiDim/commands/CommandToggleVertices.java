package malte0811.multiDim.commands;

import malte0811.multiDim.addons.Command;
import malte0811.multiDim.render.RenderAlgo;

public class CommandToggleVertices extends Command {

	@Override
	public String getCommandName() {
		return "TOGGLEVERTICES";
	}

	@Override
	public String getCommandUsage() {
		return "\"toggleVertices [true|false]\" turns the extra rendering of vertices on/off. On by default.";
	}

	@Override
	public int getMinParameterCount() {
		return 0;
	}

	@Override
	public void processCommand(String[] args) throws Exception {
		if (args.length == 0) {
			RenderAlgo.renderVertices = !RenderAlgo.renderVertices;
		} else {
			boolean active = Boolean.parseBoolean(args[0]);
			RenderAlgo.renderVertices = active;
		}
	}

}
