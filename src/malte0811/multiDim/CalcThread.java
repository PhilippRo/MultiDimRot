package malte0811.multiDim;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import malte0811.multiDim.addons.AddonLoader;
import malte0811.multiDim.addons.Command;
import malte0811.multiDim.addons.DimRegistry;
import malte0811.multiDim.addons.TickHandler;
import malte0811.multiDim.commands.programs.Programm;
import malte0811.multiDim.render.CentralThree;
import malte0811.multiDim.render.ParallelRender;
import malte0811.multiDim.render.RenderAlgo;
import malte0811.multiDim.solids.Solid;
import malte0811.multiDim.tickHandlers.DebugHandler;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

public class CalcThread implements Runnable {
	private Solid solid = null;
	private double[][] rotations = new double[0][3];
	private double[] renderOptions = { 5, 3 };
	private Programm currentProgram = null;
	private double zoomMax = 5;
	private double zoomStep = 0;
	private RenderAlgo renderAlgo = null;
	private ArrayList<TickHandler> handlers = new ArrayList<>();
	private CommandListener c;
	private ArrayDeque<String> commands = new ArrayDeque<>();
	private boolean showSides = true;
	private boolean lagLogged;
	private int lagDelay;

	public CalcThread(Solid s) {
		solid = s;
		DimRegistry.instance = this;
	}

	private void init() throws LWJGLException {
		Display.setVSyncEnabled(true);
		Display.setDisplayMode(new DisplayMode(800, 600));
		Display.setTitle("MultiDimRot");
		Display.create();
		GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());
		try {
			renderAlgo = DimRegistry.getAlgoInstance(3);
		} catch (Exception e) {
			DimRegistry.getLogger().logException(e);
		}
		Mouse.create();
		Mouse.getDWheel();
	}

	@Override
	public void run() {
		try {
			init();
		} catch (LWJGLException e1) {
			System.out.println("An error occured while initializing LWJGL");
			DimRegistry.getLogger().logException(e1);
			System.exit(0);
		}
		RenderAlgo.init();

		// create directories
		String[] dirs = { "tmp", "addons", "logs", "screenshots", "videos",
				"scripts", "solids" };
		for (String s : dirs) {
			Path p = Paths.get(DimRegistry.getUserDir()
					+ DimRegistry.getFileSeperator() + s);
			if (!Files.exists(p)) {
				try {
					Files.createDirectories(p);
				} catch (IOException e) {
					System.out
							.println("Could not create directories for logs etc. :");
					DimRegistry.getLogger().logException(e);
				}
			}
		}

		c = new CommandListener();
		try {
			AddonLoader.load();
		} catch (Exception e1) {
			System.out.println("An error occured while loading addons");
			DimRegistry.getLogger().logException(e1);
		}
		try {
			String v = getVersion();
			System.out.println("This is MultiDimRot version " + v);
		} catch (IOException e1) {
			System.out.println("Could not check version.");
			DimRegistry.getLogger().logException(e1);
		}
		// version check - master
		try {
			if (isNewerVersion("master")) {
				System.out.println("A new version is available");
			}
		} catch (IOException x) {
			DimRegistry.getLogger().logException(x);
		}
		// version check - dev
		try {
			if (isNewerVersion("dev")) {
				System.out.println("A new development version is available");
			}
		} catch (IOException x) {
			DimRegistry.getLogger().logException(x);
		}

		System.out
				.println("Type \"help\" to view a list of all commands. Type \"help <command name>\" to view help for a specific command.");
		int timer = 0;
		long time = System.currentTimeMillis();
		new CleaningThread().start();
		while (!Display.isCloseRequested()) {
			for (TickHandler th : handlers) {
				if (th.isActive()) {
					th.handleTick(solid, renderOptions);
				}
			}
			processCommands();
			time = System.currentTimeMillis();
			for (double[] i : rotations) {
				solid.rotate((int) i[0], (int) i[1], i[2]);
			}
			if (timer > 0) {
				timer--;
			}
			if (timer == 0 && currentProgram != null) {
				timer = currentProgram.step();
				if (Programm.stop) {
					timer = 0;
					currentProgram = null;
					Programm.stop = false;
					c.input.active = true;
				}
				if (timer == 0) {
					currentProgram = null;
					c.input.active = true;
				}
			}
			if (!(renderAlgo instanceof ParallelRender)
					&& renderOptions[0] != zoomMax && zoomStep != 0) {
				if (renderOptions[0] > zoomMax) {
					renderOptions[0] -= zoomStep;
					if (renderOptions[0] <= zoomMax) {
						zoomStep = 0;
					}
				} else {
					renderOptions[0] += zoomStep;
					if (renderOptions[0] >= zoomMax) {
						zoomStep = 0;
					}
				}
			}
			paint();
			Display.update();
			try {
				long tDiff = System.currentTimeMillis() - time;
				if (tDiff > 500) {
					if (!lagLogged) {
						if (CentralThree.fancy) {
							System.out
									.println("The calculation tick took more than half a second. Type \"toggleFancySides\" to disable fancy side rendering to save calculation time.");
						} else if (showSides) {
							System.out
									.println("The calculation tick took more than half a second. Type \"toggleSides\" to disable side rendering to save calculation time.");
						} else {
							System.out
									.println("The calculation tick took more than half a second.");
						}
						lagDelay = 100;
						lagLogged = true;
					}
				} else if (lagLogged) {
					lagDelay--;
					if (lagDelay <= 0) {
						lagLogged = false;
					}
				}
				DebugHandler.getInstance().addTime(3, (int) tDiff);
				Thread.sleep(100 - (tDiff > 100 ? 0 : tDiff));
			} catch (InterruptedException e) {
				DimRegistry.getLogger().logException(e);
			}

		}
		Display.destroy();
		System.exit(0);
	}

	protected void paint() {
		if (renderAlgo == null) {
			return;
		}
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		int dims = 0;
		if (renderAlgo instanceof ParallelRender) {
			dims = (int) (renderOptions[0] > renderOptions[1] ? renderOptions[0]
					: renderOptions[1]);
		}
		double[][] vertices = solid.getCopyOfVertices(dims > 2 ? dims : 2);
		int[][] oldEdges = solid.getEdges();
		int[][] edges = new int[oldEdges.length][];
		for (int i = 0; i < edges.length; i++) {
			edges[i] = Arrays.copyOf(oldEdges[i], 2);
		}
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		renderAlgo.render(vertices, edges, renderOptions, solid.getColors(),
				showSides ? solid.getSides() : null);
	}

	public void addRotCon(double[] value) {
		rotations = Arrays.copyOf(rotations, rotations.length + 1);
		rotations[rotations.length - 1] = value;
	}

	public void addCommand(String c) {
		commands.addLast(c);
	}

	private void processCommands() {
		while (!commands.isEmpty()) {
			String cmd = commands.pollFirst();
			if (!Command.processCommand(cmd, true)) {
				try {
					DimRegistry.getCalcThread().currentProgram = Programm
							.load(cmd);
				} catch (Exception e) {
					DimRegistry.getLogger().logException(e);
				}
				if (DimRegistry.getCalcThread().currentProgram != null) {
					c.input.toggle();
					break;
				}
			}
		}
	}

	public boolean isNewerVersion(String branch) throws IOException {
		URL oldV = ClassLoader.getSystemResource("version");
		if (oldV == null) {
			System.out
					.println("No version file for the current version was found. Not checking for updates from branch: "
							+ branch);
			return false;
		}
		URL in = new URL(
				"https://raw.githubusercontent.com/malte0811/MultiDimRot/"
						+ branch + "/version");
		Proxy proxy = getProxy();
		InputStream inStN;
		InputStream inStO = oldV.openStream();
		if (proxy != null) {
			URLConnection conn = in.openConnection(proxy);
			inStN = conn.getInputStream();
		} else {
			inStN = in.openStream();
		}
		int last = inStN.read();
		String lastN = "0";
		while (last != -1) {
			char l = (char) last;
			if (l == '.') {
				String lastO = "0";
				last = inStO.read();
				while (last != -1) {
					l = (char) last;
					if (l == '.') {
						break;
					}
					lastO += l;

					last = inStO.read();
				}
				if (Integer.parseInt(lastN) > Integer.parseInt(lastO)) {
					return true;
				}
				if (Integer.parseInt(lastN) < Integer.parseInt(lastO)) {
					return false;
				}
				if (last == -1) {
					return true;
				}
				lastN = "0";
			} else {
				lastN += l;
			}
			last = inStN.read();
		}
		return inStO.read() == -1;
	}

	public String getVersion() throws IOException {
		String ret = "";
		URL url = ClassLoader.getSystemResource("version");
		if (url == null) {
			throw new IOException("No version file was found.");
		}
		InputStream in = url.openStream();
		int r = in.read();
		while (r != -1) {
			ret += (char) r;
			r = in.read();
		}
		return ret;
	}

	public Proxy getProxy() {
		System.setProperty("java.net.useSystemProxies", "true");
		List<Proxy> l = null;
		try {
			l = ProxySelector.getDefault().select(new URI("http://github.com"));
		} catch (URISyntaxException e) {
			DimRegistry.getLogger().logException(e);
		}
		if (l != null) {
			for (Iterator<Proxy> iter = l.iterator(); iter.hasNext();) {
				java.net.Proxy proxy = (java.net.Proxy) iter.next();
				InetSocketAddress addr = (InetSocketAddress) proxy.address();
				if (addr != null) {
					return proxy;
				}
			}
		}
		return null;
	}

	public Solid getSolid() {
		return solid;
	}

	public void setSolid(Solid solid) {
		this.solid = solid;
	}

	public double[][] getRotations() {
		return rotations;
	}

	public void setRotations(double[][] rotations) {
		this.rotations = rotations;
	}

	public double[] getRenderOptions() {
		return renderOptions;
	}

	public void setRenderOptions(double[] renderOptions) {
		this.renderOptions = renderOptions;
	}

	public Programm getCurrentProgram() {
		return currentProgram;
	}

	public void setCurrentProgram(Programm currentProgram) {
		this.currentProgram = currentProgram;
	}

	public double getZoomMax() {
		return zoomMax;
	}

	public void setZoomMax(double zoomMax) {
		this.zoomMax = zoomMax;
	}

	public double getZoomStep() {
		return zoomStep;
	}

	public void setZoomStep(double zoomStep) {
		this.zoomStep = zoomStep;
	}

	public RenderAlgo getRenderAlgo() {
		return renderAlgo;
	}

	public void setRenderAlgo(RenderAlgo renderAlgo) {
		this.renderAlgo = renderAlgo;
	}

	public ArrayList<TickHandler> getTickHandlers() {
		return handlers;
	}

	public void setTickHandlers(ArrayList<TickHandler> handlers) {
		this.handlers = handlers;
	}

	public CommandListener getCommandListener() {
		return c;
	}

	public void setCommandListener(CommandListener c) {
		this.c = c;
	}

	public void setSidesActive(boolean active) {
		showSides = active;
	}

	public boolean areSidesActive() {
		return showSides;
	}

	public void toggleTickHandler(int id) {
		handlers.get(id).setActive(!handlers.get(id).isActive());
	}
}
