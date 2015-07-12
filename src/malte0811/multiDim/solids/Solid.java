package malte0811.multiDim.solids;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;

import malte0811.multiDim.addons.DimRegistry;

public abstract class Solid implements Serializable {
	public double[][] vertices;
	public int[][] edges;
	public int[][] sides;
	private HashMap<String, Serializable> data = new HashMap<>();

	public void rotate(int firstAxis, int secondAxis, double degree) {
		if (firstAxis < 0) {
			System.out.println("The axis " + firstAxis + " does not exist.");
			return;
		} else if (secondAxis < 0) {
			System.out.println("The axis " + secondAxis + " does not exist.");
			return;
		}

		int maxV = firstAxis > secondAxis ? firstAxis : secondAxis;
		maxV++;
		double radians = -Math.toRadians(degree);
		double[][] vTMP = getCopyOfVertices(maxV);
		if (vTMP.length == 0 || vTMP[0].length < maxV) {
			return;
		}
		for (double[] vertex : vTMP) {
			double x = vertex[firstAxis];
			double y = vertex[secondAxis];
			vertex[firstAxis] = Math.cos(radians) * x - Math.sin(radians) * y;
			vertex[secondAxis] = Math.sin(radians) * x + Math.cos(radians) * y;
		}
		vertices = vTMP;
	}

	public static Solid add(Solid a, Solid b, boolean intelligentEdges) {
		if (a.getCopyOfVertices(0).length == 0) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos;
			try {
				oos = new ObjectOutputStream(bos);
				oos.writeObject(b);
				byte[] bStream = bos.toByteArray();
				ObjectInputStream ois = new ObjectInputStream(
						new ByteArrayInputStream(bStream));
				return (Solid) ois.readObject();
			} catch (Exception e) {
				DimRegistry.getLogger().logException(e);
			}
		}
		if (b.getCopyOfVertices(0).length == 0) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos;
			try {
				oos = new ObjectOutputStream(bos);
				oos.writeObject(a);
				byte[] bStream = bos.toByteArray();
				ObjectInputStream ois = new ObjectInputStream(
						new ByteArrayInputStream(bStream));
				return (Solid) ois.readObject();
			} catch (Exception e) {
				DimRegistry.getLogger().logException(e);
			}
		}
		int[][] edgesTMP = a.getEdges();
		int[][] edges = new int[edgesTMP.length][];
		for (int i = 0; i < edgesTMP.length; i++) {
			edges[i] = Arrays.copyOf(edgesTMP[i], edgesTMP[i].length);
		}
		double[][] verticesTMP = a
				.getCopyOfVertices(b.getCopyOfVertices(0)[0].length);
		double[][] vertices = new double[verticesTMP.length][];
		for (int i = 0; i < verticesTMP.length; i++) {
			vertices[i] = Arrays.copyOf(verticesTMP[i], verticesTMP[i].length);
		}

		int vL = vertices.length;
		int eL = edges.length;
		edges = Arrays.copyOf(edges, edges.length + b.getEdges().length);

		vertices = Arrays.copyOf(vertices,
				vertices.length + b.getCopyOfVertices(0).length);
		verticesTMP = b.getCopyOfVertices(vertices[0].length);
		double[][] verticesb = new double[verticesTMP.length][];
		for (int i = 0; i < verticesTMP.length; i++) {
			verticesb[i] = Arrays.copyOf(verticesTMP[i], verticesTMP[i].length);
		}

		edgesTMP = b.getEdges();
		int[][] edgesB = new int[edgesTMP.length][];
		for (int i = 0; i < edgesTMP.length; i++) {
			edgesB[i] = Arrays.copyOf(edgesTMP[i], edgesTMP[i].length);
		}
		for (int i = vL; i < vertices.length; i++) {
			vertices[i] = verticesb[i - vL];
		}
		for (int i = eL; i < edges.length; i++) {
			edges[i] = new int[2];
			edges[i][0] = edgesB[i - eL][0] + (intelligentEdges ? vL : 0);
			edges[i][1] = edgesB[i - eL][1] + (intelligentEdges ? vL : 0);
		}
		int[][] sides = new int[0][3];
		if (a.getSides() != null) {
			if (b.getSides() != null) {
				int[][] sidesTMP = b.getSides();
				sides = new int[a.getSides().length + sidesTMP.length][3];
				for (int i = 0; i < a.getSides().length; i++) {
					if (a.getSides()[i] != null) {
						sides[i] = Arrays.copyOf(a.getSides()[i], 3);
					}
				}
				if (sidesTMP.length > 0) {
					for (int i = a.getSides().length; i < sides.length; i++) {
						if (sides[0] != null && a.getSides()[0] != null
								&& sidesTMP[i - a.getSides().length] != null) {
							sides[i] = new int[3];
							sides[i][0] = sidesTMP[i - a.getSides().length][0]
									+ (intelligentEdges ? vL : 0);
							sides[i][1] = sidesTMP[i - a.getSides().length][1]
									+ (intelligentEdges ? vL : 0);
							sides[i][2] = sidesTMP[i - a.getSides().length][2]
									+ (intelligentEdges ? vL : 0);
						}
					}
				}
			} else {
				sides = a.getSides();
			}
		} else {
			if (b.getSides() != null) {
				sides = b.getSides();
			}
		}
		return new TMPSolid(edges, vertices, sides);
	}

	public void translate(int axis, double diff) {
		double[][] tmp = getCopyOfVertices(axis + 1);
		for (double[] i : tmp) {
			i[axis] += diff;
		}
		vertices = tmp;
	}

	public void resize(int axis, double multiPl) {
		for (double[] i : getCopyOfVertices(axis)) {
			i[axis] *= multiPl;
		}
	}

	public void resize(double d) {
		for (int i = 0; i < vertices[0].length; i++) {
			resize(i, d);
		}
	}

	public abstract void setMinDim(int dim);

	public abstract double[][] getCopyOfVertices(int minDim);

	public abstract int[][] getEdges();

	public abstract float[][] getColors();

	public abstract boolean isColored();

	public void addProperty(String name, Serializable val) {
		data.put(name, val);
	}

	public Serializable getProperty(String name) {
		if (data.containsKey(name)) {
			return data.get(name);
		}
		return null;
	}

	public boolean hasProperty(String n) {
		return data.containsKey(n);
	}

	public int[][] getSides() {
		return sides;
	}

	public double[][] getVertices() {
		return vertices;
	}
}
