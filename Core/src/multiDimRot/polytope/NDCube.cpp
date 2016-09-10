#include <NDCube.h>
#include <Polytope.h>
#include <vector>
#include <iostream>
NDCube::NDCube(int dims) {
	int vCount = 1<<dims;
	int eCount = dims*(vCount>>1);
	dimensions = dims;
	edges = std::vector<Edge>(eCount);
	vertices = std::vector<VecN>(vCount);
	int edgeId = 0;
	for (int i = 0;i<vCount;i++) {
		//vertex
		VecN next(dims);
		for (int digit = 0;digit<dims;digit++) {
			int tmp = i&(1<<digit);
			next.setElement(digit, tmp>0?.5:-.5);
			//edge
			if (tmp==0) {
				edges[edgeId].start = i;
				edges[edgeId].end = i+(1<<digit);
				edgeId++;
			}
		}
		vertices[i] = next;
	}
	//temporary: normals for edges
	for (int i = 0;i<eCount;i++) {
		VecN normal(dims);
		VecN start = vertices[edges[i].start];
		VecN end = vertices[edges[i].end];
		for (int j = 0;j<dims;j++) {
			if (start[j]==end[j]) {
				normal.setElement(j, start[j]);
			}
		}
		normal.scaleToLength(1);
		edges[i].normal = normal;
	}
}

NDCube::~NDCube() {}

int NDCube::getDimensions() {
	return dimensions;
}

std::vector<Edge>& NDCube::getEdges() {
	return edges;
}

std::vector<VecN>& NDCube::getVertices() {
	return vertices;
}
std::vector<Triangle>& NDCube::getFaces() {
	return faces;
}

void NDCube::update() {}
