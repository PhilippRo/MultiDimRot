package malte0811.multiDim.solids;

import java.util.Arrays;

public class NDSphere extends Solid {
	public NDSphere(int dim, int res) {
		if (dim < 1 || res <= 0) {
			System.out
					.println("The dimension and resolution has to be positive.");
			vertices = new double[0][0];
			edges = new int[0][0];
			return;
		}
		edges = new int[1][2];
		if (dim == 1) {
			Solid tmp = new TMPSolid(new int[0][2], new double[][] { { 1 },
					{ -1 } }, new int[0][3]);
			edges = tmp.getEdges();
			vertices = tmp.getCopyOfVertices(dim);
			sides = tmp.getSides();
			return;
		}
		final Solid oldSphere = new NDSphere(dim - 1, res);
		Solid tmp = new TMPSolid(new int[0][2], new double[0][dim],
				new int[0][3]);
		for (int rot = 0; rot < 180 + res; rot += res) {
			oldSphere.rotate(0, dim - 1, res);
			tmp = Solid.add(tmp, oldSphere, true);
		}
		this.vertices = tmp.getCopyOfVertices(dim);
		this.edges = tmp.getEdges();
		this.sides = tmp.getSides();
		int oldLength = oldSphere.getCopyOfVertices(0).length;
		int oldELength = edges.length;
		int oldSLength = sides.length;
		sides = Arrays.copyOf(sides,
				Math.max(sides.length + 2 * oldELength - 2 * oldLength + 4, 0));
		int z = 0;
		for (int i = 0; i < edges.length && 2 * z + oldSLength < sides.length; i++) {
			if (edges[i][0] < vertices.length - oldLength
					&& edges[i][1] < vertices.length - oldLength) {
				sides[2 * z + oldSLength] = new int[3];
				sides[2 * z + oldSLength + 1] = new int[3];
				sides[2 * z + oldSLength][0] = edges[i][0];
				sides[2 * z + oldSLength][1] = edges[i][1];
				sides[2 * z + oldSLength][2] = edges[i][0] + oldLength;
				sides[2 * z + oldSLength + 1][0] = edges[i][1];
				sides[2 * z + oldSLength + 1][1] = edges[i][1] + oldLength;
				sides[2 * z + oldSLength + 1][2] = edges[i][0] + oldLength;
				z++;
			}
		}
		while (2 * z + oldSLength < sides.length) {
			sides[2 * z + oldSLength] = new int[3];
			sides[2 * z + oldSLength + 1] = new int[3];
			z++;
		}
		edges = Arrays.copyOf(edges, oldELength + vertices.length - oldLength);
		int i = 0;
		for (; i < vertices.length - oldLength; i++) {
			edges[i + oldELength] = new int[2];
			edges[i + oldELength][0] = i;
			edges[i + oldELength][1] = (i + oldLength) % vertices.length;
		}
		for (; i + oldELength < edges.length; i++) {
			edges[i + oldELength] = new int[2];
		}
	}

	@Override
	public double[][] getCopyOfVertices(int minDim) {
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
		if (vertices[0].length < dim) {
			for (double[] v : vertices) {
				v = Arrays.copyOf(v, dim);
			}
		}
	}
}