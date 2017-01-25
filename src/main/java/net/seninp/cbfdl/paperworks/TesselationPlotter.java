package net.seninp.cbfdl.paperworks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import be.humphreys.simplevoronoi.GraphEdge;
import be.humphreys.simplevoronoi.Point;
import us.molini.graph.GraphFactory;

public class TesselationPlotter {

  public static void main(String[] args) throws NumberFormatException, IOException {

    ArrayList<double[]> dat = readData("rossler_results_01.txt");

    GraphFactory alg = new GraphFactory(0.001);

    Point p[] = new Point[1024];

    for (int i = 0; i < 1024; i++) {
      double[] pp = dat.get(i);
      p[i] = new Point(pp[1], pp[2]);
    }

    List<GraphEdge> result = alg.generateVoronoi(p, -5, 7, -8, 4);

    // Print out the edge points.
    PrintWriter writer = new PrintWriter(new File("voronoi_edges.txt"), "UTF-8");
    for (GraphEdge e : result) {
      writer.println(
          "  " + e.x1 + ", " + e.y1 + ", " + e.x2 + ", " + e.y2 + ", " + e.site1 + ", " + e.site2);
    }
    writer.close();

  }

  private static ArrayList<double[]> readData(String fileName)
      throws NumberFormatException, IOException {

    ArrayList<double[]> res = new ArrayList<double[]>();
    BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
    String line = "";

    while ((line = br.readLine()) != null) {

      if (line.trim().length() == 0) {
        continue;
      }

      String[] split = line.trim().split("[\\,\\s]+");

      double[] series = new double[split.length];
      for (int i = 0; i < split.length; i++) {
        series[i] = Double.valueOf(split[i].trim()).doubleValue();
      }
      res.add(series);
    }

    br.close();

    return res;
  }
}
