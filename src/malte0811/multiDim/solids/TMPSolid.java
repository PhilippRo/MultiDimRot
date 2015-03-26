package malte0811.multiDim.solids;

import java.util.Arrays;

public class TMPSolid extends Solid {

	public TMPSolid(int[][] edgesA, double[][] verticesA, int[][] sidesA) {
		edges = edgesA;
		vertices = verticesA;
		sides = sidesA;
	}

	@Override
	public double[][] getCopyOfVertices(int minDim) {
		if (vertices.length == 0) {
			return new double[0][1];
		}
		double[][] newVertices = new double[vertices.length][vertices[0].length];
		if (minDim < vertices[0].length) {
			minDim = vertices[0].length;
		}
		for (int i = 0; i < vertices.length; i++) {
			newVertices[i] = Arrays.copyOf(vertices[i], minDim);
		}
		return newVertices;
	}

	@Override
	public int[][] getEdges() {
		return edges;
	}

	@Override
	public float[][] getColors() {
		return null;
	}

	@Override
	public boolean isColored() {
		return false;
	}

	@Override
	public void setMinDim(int dim) {
		if (vertices == null || vertices.length == 0) {
			System.out.println("vertices==null||length==0");
			return;
		}
		if (vertices[0].length < dim) {
			for (double[] v : vertices) {
				v = Arrays.copyOf(v, dim);
			}
		}
	}

}
