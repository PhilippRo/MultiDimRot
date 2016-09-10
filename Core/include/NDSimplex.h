#ifndef SRC_MULTIDIMROT_POLYTOPE_NDSIMPLEX_H_
#define SRC_MULTIDIMROT_POLYTOPE_NDSIMPLEX_H_

#include "Polytope.h"

class NDSimplex: public Polytope {
public:
	NDSimplex(int i);
	virtual ~NDSimplex();
	virtual std::vector<VecN>& getVertices();
	virtual std::vector<Edge>& getEdges();
	virtual std::vector<Triangle>& getFaces();
	virtual int getDimensions();
	virtual void update();
private:
	int dimensions;
	std::vector<Edge> edges;
	std::vector<VecN> vertices;
	std::vector<Triangle> faces;
};

#endif /* SRC_MULTIDIMROT_POLYTOPE_NDSIMPLEX_H_ */
