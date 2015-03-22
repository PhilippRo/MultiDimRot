package malte0811.multiDim.render;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.glu.GLU;

public abstract class RenderAlgo {
	public static int mainShaderId;
	public static int sideShaderId;
	final static String vertexShaderEdges = "#version 150 core\r\n"
			+ "in vec4 in_Position;\r\n" + "in vec4 in_Color;\r\n"
			+ "out vec4 pass_Color;\r\n" + "void main(void) {\r\n"
			+ "gl_Position = in_Position;\r\n" + "pass_Color = in_Color;\r\n"
			+ "}";
	final static String vertexShaderSides = "#version 150 core\r\n"
			+ "in vec2 in_Position;\r\n" + "in float in_dens;\r\n"
			+ "out vec4 pass_Color;\r\n" + "void main(void) {\r\n"
			+ "gl_Position = vec4(in_Position[0], in_Position[1], 0, 1);\r\n"
			+ "pass_Color = vec4(0, 0, 1-2/(2+in_dens), 1);\r\n" + "}";
	final static String fragShader = "#version 150 core\r\n"
			+ "in vec4 pass_Color;\r\n" + "out vec4 out_Color;\r\n"
			+ "void main(void) {\r\n" + "out_Color = pass_Color;\r\n" + "}";

	public abstract double[] getInitialParams();

	public abstract void render(double[][] vertices, int[][] edges,
			double[] options, float[][] colors, int[][] sides);

	public static void init() {
		int vsId = loadShader(vertexShaderEdges, GL20.GL_VERTEX_SHADER);
		int frId = loadShader(fragShader, GL20.GL_FRAGMENT_SHADER);
		mainShaderId = GL20.glCreateProgram();
		GL20.glAttachShader(mainShaderId, vsId);
		GL20.glAttachShader(mainShaderId, frId);
		GL20.glBindAttribLocation(mainShaderId, 0, "in_Position");
		GL20.glBindAttribLocation(mainShaderId, 1, "in_Color");
		GL20.glLinkProgram(mainShaderId);
		GL20.glValidateProgram(mainShaderId);
		int error;
		if ((error = GL11.glGetError()) != GL11.GL_NO_ERROR) {
			System.out.println("ERROR - Could not create edge shaders:"
					+ GLU.gluErrorString(error));
			System.exit(-1);
		}
		System.out.println("Loaded edge shaders: " + mainShaderId);
		vsId = loadShader(vertexShaderSides, GL20.GL_VERTEX_SHADER);
		sideShaderId = GL20.glCreateProgram();
		GL20.glAttachShader(sideShaderId, vsId);
		GL20.glAttachShader(sideShaderId, frId);
		GL20.glBindAttribLocation(sideShaderId, 0, "in_Position");
		GL20.glBindAttribLocation(sideShaderId, 1, "in_dens");
		GL20.glLinkProgram(sideShaderId);
		GL20.glValidateProgram(sideShaderId);
		if ((error = GL11.glGetError()) != GL11.GL_NO_ERROR) {
			System.out.println("ERROR - Could not create side shaders:"
					+ GLU.gluErrorString(error));
			System.exit(-1);
		}
		System.out.println("Loaded side shaders: " + sideShaderId);
	}

	public static int loadShader(String shader, int type) {
		int shaderID = 0;
		shaderID = GL20.glCreateShader(type);
		GL20.glShaderSource(shaderID, shader);
		GL20.glCompileShader(shaderID);
		return shaderID;
	}

	protected void renderNoColor(float[][] rV, int[][] edges, int shader) {
		IntBuffer indices = BufferUtils.createIntBuffer(2 * edges.length);
		float[] tmp = { 0, 1 };
		FloatBuffer interL = BufferUtils.createFloatBuffer(8 * rV.length);
		int[] vId = new int[rV.length];
		int i2 = 0;
		for (int i = 0; i < rV.length; i++) {
			if (rV[i] != null) {
				interL.put(rV[i]);
				vId[i] = i2;
				interL.put(tmp);
				interL.put(tmp);
				interL.put(tmp);
				i2++;
			}
		}
		interL.flip();
		i2 = 0;
		for (int[] i : edges) {
			if (rV[i[0]] != null && rV[i[1]] != null) {
				i2 += 2;
				indices.put(vId[i[0]]);
				indices.put(vId[i[1]]);
			}
		}
		indices.flip();
		int vaoId = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vaoId);
		int vboiId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices,
				GL15.GL_STATIC_DRAW);
		int vboInterId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboInterId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, interL, GL15.GL_STATIC_DRAW);
		// Put the VBO in the attributes list at index 0
		GL20.glVertexAttribPointer(0, 4, GL11.GL_FLOAT, false, 32, 0);
		GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 32, 16);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL20.glUseProgram(shader);
		GL30.glBindVertexArray(vaoId);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);
		// Draw the vertices
		GL11.glDrawElements(GL11.GL_LINES, i2, GL11.GL_UNSIGNED_INT, 0);
		GL20.glUseProgram(0);
		// deselect
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		// Deselect (bind to 0) the VAO
		GL30.glBindVertexArray(0);
		// Disable the VBO index from the VAO attributes list
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		// Delete the VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(vboInterId);
		// Delete the VAO
		GL30.glBindVertexArray(0);
		GL30.glDeleteVertexArrays(vaoId);
		// Delete the index VBO
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(vboiId);
	}

	protected void renderColor(float[][] rV, int[][] edges, float[][] colors,
			int shader) {
		IntBuffer vboI = BufferUtils.createIntBuffer(2 * edges.length);
		for (int i = 0; i < 2 * edges.length; i++) {
			vboI.put(i);
		}
		vboI.flip();
		float[][] renderV = new float[rV.length][4];
		for (int i = 0; i < rV.length; i++) {
			renderV[i][0] = rV[i][0];
			renderV[i][1] = rV[i][1];
			renderV[i][3] = 1;
		}
		FloatBuffer verticesBuffer = BufferUtils
				.createFloatBuffer(8 * edges.length);
		FloatBuffer colorsBuf = BufferUtils.createFloatBuffer(8 * edges.length);
		int anzV = 0;
		for (int i = 0; i < edges.length; i++) {
			if (renderV[edges[i][0]] == null || renderV[edges[i][1]] == null) {
				continue;
			}
			anzV += 2;
			verticesBuffer.put(renderV[edges[i][0]]);
			verticesBuffer.put(renderV[edges[i][1]]);
			colorsBuf.put(colors[i]);
			colorsBuf.put(1);
			colorsBuf.put(colors[i]);
			colorsBuf.put(1);
		}
		colorsBuf.flip();
		verticesBuffer.flip();
		int vaoId = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vaoId);
		int vboId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer,
				GL15.GL_STATIC_DRAW);
		// Put the VBO in the attributes list at index 0
		GL20.glVertexAttribPointer(0, 4, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		// colors
		int vbocId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbocId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorsBuf, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 0, 0);
		// Deselect (bind to 0) the VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
		int vboiId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, vboI,
				GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL20.glUseProgram(shader);
		GL30.glBindVertexArray(vaoId);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);
		// Draw the vertices
		GL11.glDrawElements(GL11.GL_LINES, anzV, GL11.GL_UNSIGNED_INT, 0);
		GL20.glUseProgram(0);
		// deselect
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		// Deselect (bind to 0) the VAO
		GL30.glBindVertexArray(0);
		// Disable the VBO index from the VAO attributes list
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL20.glDisableVertexAttribArray(0);
		// Delete the VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(vboId);
		// Delete the VAO
		GL30.glBindVertexArray(0);
		GL30.glDeleteVertexArrays(vaoId);
		// Delete the index VBO
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(vbocId);
	}

	public int[][] getDensity(int[][][] triangles) {
		int[][] dens = new int[Display.getWidth()][Display.getHeight()];
		for (int[][] tri : triangles) {
			if (tri == null) {
				continue;
			}
			int[] min = {
					(int) Math.min(tri[0][0], Math.min(tri[1][0], tri[2][0])),
					(int) Math.min(tri[0][1], Math.min(tri[1][1], tri[2][1])) };
			int[] max = {
					(int) Math.max(tri[0][0], Math.max(tri[1][0], tri[2][0])),
					(int) Math.max(tri[0][1], Math.max(tri[1][1], tri[2][1])) };
			boolean middleAbove = false;
			int middle = 0;
			for (int i = 0; i < 3; i++) {
				if ((tri[i][0] >= tri[(i + 1) % 3][0] && tri[i][0] <= tri[(i + 2) % 3][0])
						|| (tri[i][0] <= tri[(i + 1) % 3][0] && tri[i][0] >= tri[(i + 2) % 3][0])) {
					middle = i;
					break;
				}
			}
			int[] mp = tri[middle];
			int[] s1 = tri[(middle + 1) % 3];
			int[] s2 = tri[(middle + 2) % 3];
			if (mp[1] > valueAt(mp[0], s1[0], s1[1], s2[0], s2[1], 0)) {
				middleAbove = true;
			}
			for (int x = Math.max(min[0], 0); x < Math.min(max[0],
					Display.getWidth()); x++) {
				for (int y = Math.max(min[1], 0); y < Math.min(max[1],
						Display.getHeight()); y++) {
					float vOp = valueAt(x, s1[0], s1[1], s2[0], s2[1],
							middleAbove ? min[1] : max[1]);
					float vS1 = valueAt(x, s1[0], s1[1], mp[0], mp[1],
							middleAbove ? max[1] : min[1]);
					float vS2 = valueAt(x, s2[0], s2[1], mp[0], mp[1],
							middleAbove ? max[1] : min[1]);
					if (middleAbove) {
						// is the current point in the triangle tri?
						if (vOp <= y && vS1 > y && vS2 > y) {
							dens[x][y]++;
						}
					} else {
						// is the current point in the triangle tri?
						if (vOp >= y && vS1 < y && vS2 < y) {
							dens[x][y]++;
						}
					}
				}
			}
		}
		return dens;
	}

	protected void renderSides(int[][] density) {
		int count = 0;
		for (int[] i : density) {
			for (int i2 : i) {
				if (i2 > 0) {
					count++;
				}
			}
		}
		FloatBuffer vertices = BufferUtils.createFloatBuffer(2 * count);
		FloatBuffer dens = BufferUtils.createFloatBuffer(count);
		int width = Display.getWidth();
		int height = Display.getHeight();
		for (int i = 0; i < density.length; i++) {
			for (int i2 = 0; i2 < density[i].length; i2++) {
				if (density[i][i2] > 0) {
					vertices.put(((float) (2 * i) / (float) width) - 1F);
					vertices.put(((float) (2 * i2) / (float) height) - 1F);
					dens.put(density[i][i2]);
				}
			}
		}
		vertices.flip();
		dens.flip();
		int vaoId = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vaoId);
		int vboId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		int densVbo = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, densVbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, dens, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(1, 1, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL20.glUseProgram(sideShaderId);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		// Draw the vertices
		GL11.glDrawArrays(GL11.GL_POINTS, 0, count);
		GL20.glUseProgram(0);
		// deselect
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		// Deselect (bind to 0) the VAO
		GL30.glBindVertexArray(0);
		// Disable the VBO index from the VAO attributes list
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL20.glDisableVertexAttribArray(0);
		// Delete the VBO
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(vboId);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glDeleteBuffers(densVbo);
		// Delete the VAO
		GL30.glBindVertexArray(0);
		GL30.glDeleteVertexArrays(vaoId);
	}

	protected double[] achsenabschnitt(double[] v1, double[] v2,
			double[] options) {
		double[] ret = new double[v1.length];
		for (int i = 0; i < v1.length - 1; i++) {
			ret[i] = achsenabschnitt(v1[v1.length - 1], v2[v2.length - 1],
					v1[i], v2[i]);
		}
		if (options != null) {
			ret[v1.length - 1] = Math.abs(options[0]) < 0.001 ? 0.001
					: options[0];
		}
		return ret;
	}

	protected double achsenabschnitt(double x1, double x2, double y1, double y2) {
		double steigung = (y2 - y1) / (x2 - x1);
		double d = y2 - steigung * x2;
		return d;
	}

	protected float[] valueAt(float x, float[] ret2, float[] ret3) {
		float[] ret = new float[ret2.length];
		double[] s1D = new double[ret2.length];
		double[] s2D = new double[ret2.length];
		for (int i = 0; i < s1D.length; i++) {
			s1D[i] = ret2[i];
			s2D[i] = ret3[i];
		}
		double[] achse = achsenabschnitt(s1D, s2D, null);
		for (int i = 0; i < ret2.length - 1; i++) {
			double steigung = (s1D[i] - s2D[i])
					/ (s1D[s1D.length - 1] - s2D[ret3.length - 1]);
			ret[i] = (int) (achse[i] + steigung * (double) x);
		}
		ret[ret.length - 1] = x;
		return ret;
	}

	protected float valueAt(float f, float x1, float y1, float x2, float y2,
			float defaultTo) {
		if (x1 == x2) {
			return defaultTo;
		}
		float steig = (y2 - y1) / (x2 - x1);
		return (float) (steig * f + achsenabschnitt(x1, x2, y1, y2));
	}
}