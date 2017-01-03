package us.molini.graph;

import java.util.ArrayList;
import be.humphreys.simplevoronoi.Point;
import be.humphreys.simplevoronoi.Site;

public class Neighborhood extends Site {

  ArrayList<Neighborhood> neighbor;
  int lastVisit; // Site number of list visitor.

  Neighborhood(Point p, int number) {
    super();
    coord = p;
    sitenbr = number;
    neighbor = new ArrayList<Neighborhood>();
  }

  public double distance(Site s) {
    double d = Math.hypot(coord.x - s.coord.x, coord.y - s.coord.y);
    // System.out.println("Site "+s.sitenbr+" is "+d+" from site "+sitenbr);
    return d;
  }
}