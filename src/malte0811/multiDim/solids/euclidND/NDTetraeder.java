package malte0811.multiDim.solids.euclidND;

import java.util.Arrays;

import malte0811.multiDim.solids.Solid;
import malte0811.multiDim.solids.TMPSolid;

public class NDTetraeder extends Solid {

	public NDTetraeder(int dim) {
		if (dim == 1) {
			int[][] tmp = { { 0, 1 } };
			double[][] tmpV = { { -0.5 }, { 0.5 } };
			vertices = tmpV;
			edges = tmp;
			return;
		}

		Solid oldTetra = new NDTetraeder(dim - 1);
		double[][] oldV = oldTetra.getCopyOfVertices(dim - 1);
		double height = Math.sqrt(1 - oldV[oldV.length - 1][dim - 2]
				* oldV[oldV.length - 1][dim - 2]);
		double[][] newV = new double[1][dim];
		newV[0][dim - 1] = height * (((double) dim) / ((double) dim + 1));
		int[][] newEs = new int[dim][2];
		for (int i = 0; i < oldTetra.getCopyOfVertices(0).length; i++) {
			newEs[i][0] = i;
			newEs[i][1] = dim;
		}
		Solid tmp = new TMPSolid(newEs, newV);
		oldTetra.translate(dim - 1, -height / (double) (dim + 1));
		tmp = Solid.add(oldTetra, tmp, false);
		edges = tmp.getEdges();
		vertices = tmp.getCopyOfVertices(dim);
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