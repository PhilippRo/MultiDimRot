package malte0811.multiDim.solids;

import java.util.Arrays;

public class TMPSolid extends Solid {
	boolean colored;
	float[][] colors;

	public TMPSolid(int[][] edgesA, double[][] verticesA, int[][] sidesA) {
		edges = edgesA;
		vertices = verticesA;
		sides = sidesA;
	}

	public TMPSolid(int[][] edgesA, double[][] verticesA, int[][] sidesA,
			float[][] colorsA) {
		edges = edgesA;
		vertices = verticesA;
		sides = sidesA;
		colors = colorsA;
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
		return colors;
	}

	@Override
	public boolean isColored() {
		return colored;
	}

	@Override
	public void setMinDim(int dim) {
		if (vertices == null || vertices.length == 0) {
			return;
		}
		if (vertices[0].length < dim) {
			for (double[] v : vertices) {
				v = Arrays.copyOf(v, dim);
			}
		}
	}

}
