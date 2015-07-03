package malte0811.multiDim.solids;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

public class SolidSerializer {
	public static final int version = 0;

	// int: serializerVersion
	// int: dimensions
	// int: vertexCount
	// int: edgeCount
	// int: sideCount
	// int: propertyCount
	// boolean: colored
	// double*(dimensions*vertexCount) vertices: vertices[0][0], vertices[0][1],
	// etc
	// int*(2*edgeCount) edges: edges[0][0], edges[0][1], etc
	// int*(3*sideCount) sides: sides[0][0], sides[0][1], etc
	// if colored (float*(3*edgeCount): colors)
	// String, Serializeable: Properties
	public static Solid readSolid(InputStream in) throws IOException,
			ClassNotFoundException {
		ObjectInputStream dis = new ObjectInputStream(in);
		int version = dis.readInt();
		int dimensions = dis.readInt();
		int vertexCount = dis.readInt();
		int edgeCount = dis.readInt();
		int sideCount = dis.readInt();
		int propertyCount = dis.readInt();
		boolean colored = dis.readBoolean();
		// vertices
		double[][] vertices = new double[vertexCount][dimensions];
		for (int v = 0; v < vertexCount; v++) {
			for (int d = 0; d < dimensions; d++) {
				vertices[v][d] = dis.readDouble();
			}
		}
		// edges
		int[][] edges = new int[edgeCount][2];
		for (int e = 0; e < edgeCount; e++) {
			for (int v = 0; v < 2; v++) {
				edges[e][v] = dis.readInt();
			}
		}
		// sides
		int[][] sides = null;
		if (sideCount >= 0) {
			sides = new int[sideCount][3];
			for (int s = 0; s < sideCount; s++) {
				for (int v = 0; v < 3; v++) {
					sides[s][v] = dis.readInt();
				}
			}
		}
		float[][] colors = new float[edgeCount][3];
		if (colored) {
			for (int e = 0; e < edgeCount; e++) {
				for (int c = 0; c < 3; c++) {
					colors[e][c] = dis.readFloat();
				}
			}
		}
		// properties
		Solid ret;
		if (colored) {
			ret = new TMPSolid(edges, vertices, sides, colors);
		} else {
			ret = new TMPSolid(edges, vertices, sides);
		}
		for (int p = 0; p < propertyCount; p++) {
			ret.addProperty((String) dis.readObject(),
					(Serializable) dis.readObject());
		}
		return ret;
	}

	public static void writeSolid(OutputStream out, Solid s) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(out);
		oos.writeInt(version);
		double[][] vertices = s.getVertices();
		int[][] edges = s.getEdges();
		int[][] sides = s.getSides();
		float[][] colors = s.getColors();
		boolean colored = colors != null;
		HashMap<String, Serializable> prop = new HashMap<>();
		int dimensions = vertices.length > 0 ? vertices[0].length : 0;
		oos.writeInt(dimensions);
		oos.writeInt(vertices.length);
		oos.writeInt(edges.length);
		oos.writeInt(sides != null ? sides.length : -1);
		oos.writeInt(prop.size());
		oos.writeBoolean(colored);
		// vertices
		for (double[] v : vertices) {
			for (double i : v) {
				oos.writeDouble(i);
			}
		}
		// edges
		for (int[] edge : edges) {
			oos.writeInt(edge[0]);
			oos.writeInt(edge[1]);
		}
		// sides
		if (sides != null) {
			for (int[] side : sides) {
				oos.writeInt(side[0]);
				oos.writeInt(side[1]);
				oos.writeInt(side[2]);
			}
		}
		// colors
		if (colored) {
			for (float[] e : colors) {
				oos.writeFloat(e[0]);
				oos.writeFloat(e[1]);
				oos.writeFloat(e[2]);
			}
		}
		// properties
		Set<String> keys = prop.keySet();
		for (String k : keys) {
			oos.writeObject(k);
			oos.writeObject(prop.get(k));
		}
		oos.flush();
		oos.close();
	}
}
