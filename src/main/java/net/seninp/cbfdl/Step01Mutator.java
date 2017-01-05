package net.seninp.cbfdl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import org.apache.commons.math3.ode.sampling.StepNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import be.humphreys.simplevoronoi.GraphEdge;
import be.humphreys.simplevoronoi.Point;
import net.seninp.attractor.rossler.RosslerEquations;
import net.seninp.attractor.rossler.RosslerStepHandler;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;
import net.seninp.util.UCRUtils;
import us.molini.graph.GraphFactory;

public class Step01Mutator {

  private static final String CR = "\n";
  private static final String COMMA = ", ";
  private static final String TAB = "\t";

  // discretization parameters
  private final static int WINDOW_SIZE = 60;
  private final static int PAA_SIZE = 6;
  private final static int ALPHABET_SIZE = 6;
  private final static double NORM_THRESHOLD = 0.01;
  private final static Alphabet ALPHABET = new NormalAlphabet();
  private final static SAXProcessor sp = new SAXProcessor();

  // the data
  private static final String DATA_FILE = "src/resources/data/CBF/CBF_TRAIN";

  // the curve
  private static final ClassicalRungeKuttaIntegrator INTEGRATOR = new ClassicalRungeKuttaIntegrator(
      0.01);

  // the logger
  private static final Logger LOGGER = LoggerFactory.getLogger(TSProcessor.class);

  private static final double MAX_DIST = 0.2;

  public static void main(String[] args) throws NumberFormatException, IOException, SAXException {

    // 0.0 -- read the data
    //
    Map<String, List<double[]>> CBFData = UCRUtils.readUCRData(DATA_FILE);
    LOGGER.info("read " + UCRUtils.datasetStats(CBFData, "\"" + DATA_FILE + "\" "));

    // 0.1 -- take the only class #1 and series #0 and mutate it ... later
    //
    // TODO: wrap into the loop
    String key = "1";
    int index = 0;
    double[] series = CBFData.get(key).get(index);
    LOGGER.info(
        "fixed series of class " + key + ", index " + index + ", " + series.length + " points");

    // 0.2 disretize
    //
    SAXRecords sax = sp.ts2saxViaWindow(series, WINDOW_SIZE, PAA_SIZE,
        ALPHABET.getCuts(ALPHABET_SIZE), NumerosityReductionStrategy.NONE, NORM_THRESHOLD);
    ArrayList<Integer> indexes = new ArrayList<Integer>();
    indexes.addAll(sax.getIndexes());
    Collections.sort(indexes);
    StringBuffer theString = new StringBuffer(indexes.size() * PAA_SIZE);
    for (Integer idx : indexes) {
      char[] str = sax.getByIndex(idx).getPayload();
      for (char s : str) {
        theString.append(s);
      }
    }

    // 0.3 generate the curve
    //
    ArrayList<double[]> theCurve = new ArrayList<double[]>();
    RosslerEquations equations = new RosslerEquations(0.441, 1.99, 3.75);
    RosslerStepHandler stepHandler = new RosslerStepHandler("test01.txt", theCurve);
    INTEGRATOR.addStepHandler(new StepNormalizer(0.1, stepHandler));
    INTEGRATOR.integrate(equations, 0, new double[] { 1., 1., 1. }, theString.length() * 0.1,
        new double[3]);

    // 0.4 produce and output the tesselation (and labels?)
    //
    GraphFactory alg = new GraphFactory(0.001);
    Point p[] = new Point[theCurve.size()];
    for (int i = 0; i < Math.min(theCurve.size(), theString.length()); i++) {
      double[] pp = theCurve.get(i);
      p[i] = new Point(pp[1], pp[2]);
    }
    List<GraphEdge> result = alg.generateVoronoi(p, -5, 7, -8, 4);
    PrintWriter writer = new PrintWriter(new File("voronoi_edges.txt"), "UTF-8");
    for (GraphEdge e : result) {
      writer.println(
          "  " + e.x1 + ", " + e.y1 + ", " + e.x2 + ", " + e.y2 + ", " + e.site1 + ", " + e.site2);
    }
    writer.close();

    // 0.5 build the tree of the original series points
    //
    RTree<String, Geometry> tree = RTree.create();
    for (int i = 0; i < Math.min(theCurve.size(), theString.length()); i++) {
      double[] pp = theCurve.get(i);
      tree = tree.add(String.valueOf(theString.charAt(i)), Geometries.point(pp[1], pp[2]));
    }
    tree.visualize(600, 600).save("target/mytree.png");

    // 0.6 mutate the curve a bit
    //
    ArrayList<double[]> theMutatedCurve = new ArrayList<double[]>();
    equations = new RosslerEquations(0.437, 1.99, 3.75);
    stepHandler = new RosslerStepHandler("test02.txt", theMutatedCurve);
    INTEGRATOR.addStepHandler(new StepNormalizer(0.1, stepHandler));
    INTEGRATOR.integrate(equations, 0, new double[] { 1., 1., 1. }, theString.length(),
        new double[3]);

    // 0.6 extract the new string
    //
    StringBuffer theNewString = new StringBuffer(indexes.size() * PAA_SIZE);
    for (int i = 0; i < Math.min(theMutatedCurve.size(), theString.length()); i++) {

      double[] newP = theMutatedCurve.get(i);
      com.github.davidmoten.rtree.geometry.Point point = Geometries.point(newP[1], newP[2]);

      Entry<String, Geometry> pp = tree.nearest(point, 1, 1).toBlocking().single();

      theNewString.append(pp.value());

    }

    System.out.println(theString + CR + CR);
    System.out.println(theNewString);

  }

}
