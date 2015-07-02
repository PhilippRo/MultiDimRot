package malte0811.multiDim.commands.ser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InvalidClassException;
import java.util.ArrayList;

import malte0811.multiDim.addons.Command;
import malte0811.multiDim.addons.DimRegistry;

public class CommandDeserialize extends Command {

	@Override
	public String getCommandName() {
		return "DESERIALIZE";
	}

	@Override
	public void processCommand(String[] args) throws Exception {
		String sep = DimRegistry.getFileSeperator();

		try {
			File f = new File(DimRegistry.getUserDir() + sep + "solids" + sep
					+ args[0]);
			FileInputStream fis = new FileInputStream(f);
			DimRegistry.getCalcThread()
					.setSolid(SolidSerializer.readSolid(fis));
		} catch (FileNotFoundException e) {
			System.out.println("The file " + System.getProperty("user.dir")
					+ sep + "solids" + sep + args[0] + " does not exist.");
			return;
		} catch (ClassNotFoundException e) {
			System.out.println("The file: " + System.getProperty("user.dir")
					+ sep + "solids" + sep + args[0]
					+ " does not contain a valid solid.");
			return;
		} catch (InvalidClassException x) {
			System.out.println("The file: " + System.getProperty("user.dir")
					+ sep + "solids" + sep + args[0]
					+ " does not contain a valid solid.");
			return;
		}

	}

	@Override
	public String getCommandUsage() {
		return "\"deserialize <file>\" loads the solid stored in file (relative path)";
	}

	@Override
	public ArrayList<String> getCompletion(int i, String toComplete) {
		if (i == 0) {
			String s = DimRegistry.getFileSeperator();
			return getFiles(new File(DimRegistry.getUserDir() + s + "solids"),
					toComplete);
		}
		return new ArrayList<>();
	}

	@Override
	public int getMinParameterCount() {
		return 1;
	}
}
