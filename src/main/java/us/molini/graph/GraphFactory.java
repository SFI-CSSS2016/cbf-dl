package us.molini.graph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import be.humphreys.simplevoronoi.GraphEdge;
import be.humphreys.simplevoronoi.Point;
import be.humphreys.simplevoronoi.Site;
import be.humphreys.simplevoronoi.Voronoi;

public class GraphFactory extends Voronoi {

  List<Site> allNeighbors;
  boolean gotNeighbors;

  public GraphFactory() {
    this(0.0);
  }

  public GraphFactory(double minDistanceBetweenSites) {
    super(minDistanceBetweenSites);
  }

  /**
   * Build a graph containing the given set of points.
   * 
   * @param p the points for the graph.
   * @param minX The minimum X of the bounding box around the graph.
   * @param maxX The maximum X of the bounding box around the graph.
   * @param minY The minimum Y of the bounding box around the graph.
   * @param maxY The maximum Y of the bounding box around the graph.
   * @return the Voronoi partitioning of the graph.
   */
  public List<GraphEdge> generateVoronoi(Point[] p, double minX, double maxX, double minY,
      double maxY) {
    allEdges = new LinkedList<GraphEdge>();
    allNeighbors = new ArrayList<Site>();

    int count = p.length;

    // Copy the inputs so we don't modify the originals
    for (int i = 0; i < count; i++) {
      Neighborhood hood = new Neighborhood(p[i], i);
      allNeighbors.add(hood);
    }
    sortNode(allNeighbors);

    setBorder(minX, maxX, minY, maxY);

    voronoi_bd();
    gotNeighbors = false;

    return allEdges;
  }

  /** Return the number of sites in the latest graph. */
  public int size() {
    return allNeighbors.size();
  }

  /**
   * Get a neighborhood.
   * 
   * @return the neighborhood contains a point of the graph and a list of the adjacent points.
   */
  public Neighborhood getNeighborhood(int n) {
    if (!gotNeighbors)
      findNeighbors();

    return (Neighborhood) allNeighbors.get(n);
  }

  // Extract the site information from the Voronoi edges and save them as references.
  private void findNeighbors() {
    for (GraphEdge e : allEdges) {
      Neighborhood hood1 = (Neighborhood) allNeighbors.get(e.site1);
      Neighborhood hood2 = (Neighborhood) allNeighbors.get(e.site2);
      hood1.neighbor.add(hood2);
      hood2.neighbor.add(hood1);
    }
    // Clear the visit values.
    for (Site s : allNeighbors) {
      Neighborhood hood = (Neighborhood) s;
      hood.lastVisit = -1;
    }
    gotNeighbors = true;
  }

  private void visit(Neighborhood origin, double radius, Site s, List<Site> result) {
    Neighborhood hood = (Neighborhood) s;
    // System.out.println("Checking "+s.sitenbr+"("+hood.lastVisit+") against "+origin.sitenbr);
    if (hood.lastVisit == origin.sitenbr)
      return;

    hood.lastVisit = origin.sitenbr;
    if (hood.distance(origin) > radius)
      return;
    result.add(hood);
    for (Site next : hood.neighbor) {
      visit(origin, radius, next, result);
    }
  }

  /**
   * Find the points within a given distance.
   * 
   * @param n the site to be checked
   * @param radius the distance to extend the search.
   * @return the list of other sites within the given distance.
   */
  public List<Site> closeTo(int n, double radius) {
    List<Site> result = new ArrayList<Site>();
    if (!gotNeighbors)
      findNeighbors();
    Neighborhood origin = (Neighborhood) allNeighbors.get(n);
    origin.lastVisit = n;
    for (Site s : origin.neighbor) {
      visit(origin, radius, s, result);
    }

    return result;
  }

}
