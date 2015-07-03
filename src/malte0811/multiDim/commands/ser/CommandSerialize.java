package malte0811.multiDim.commands.ser;

import java.io.File;
import java.io.FileOutputStream;

import malte0811.multiDim.addons.Command;
import malte0811.multiDim.addons.DimRegistry;
import malte0811.multiDim.solids.SolidSerializer;

public class CommandSerialize extends Command {

	@Override
	public String getCommandName() {
		return "SERIALIZE";
	}

	@Override
	public void processCommand(String[] args) throws Exception {
		String s = DimRegistry.getFileSeperator();
		File f = new File(DimRegistry.getUserDir() + s + "solids" + s + args[0]);
		FileOutputStream fos = new FileOutputStream(f);
		SolidSerializer.writeSolid(fos, DimRegistry.getCalcThread().getSolid());
		fos.flush();
		fos.close();
	}

	@Override
	public String getCommandUsage() {
		return "\"serialize <file>\" stores the current solid in file (relative path)";
	}

	@Override
	public int getMinParameterCount() {
		return 1;
	}
}
