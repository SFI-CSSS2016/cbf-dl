package net.seninp.cbfdl.paperworks;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import net.seninp.attractor.rossler.RosslerPrintingStepHandler;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;
import net.seninp.util.UCRUtils;
import us.molini.graph.GraphFactory;

public class CBFFigureTesselation {

  // discretization parameters
  private final static int WINDOW_SIZE = 60;
  private final static int PAA_SIZE = 6;
  private final static int ALPHABET_SIZE = 5;
  private final static String[] alphabet = String
      .copyValueOf(Arrays.copyOfRange(NormalAlphabet.ALPHABET, 0, ALPHABET_SIZE)).split("");

  private final static double NORM_THRESHOLD = 0.01;
  private static final NumerosityReductionStrategy NR_STRATEGY = NumerosityReductionStrategy.NONE;

  private final static Alphabet ALPHABET = new NormalAlphabet();
  private final static SAXProcessor sp = new SAXProcessor();

  private final static int SHINGLE_SIZE = 4;

  // the data
  private static final String TRAIN_DATA = "src/resources/data/CBF/CBF_TRAIN";

  // the curve
  private static final double BASE_A = 0.20;
  private static final double BASE_B = 0.20;
  private static final double BASE_C = 5.0;

  // the logger
  private static final Logger LOGGER = LoggerFactory.getLogger(TSProcessor.class);
  private static final String SEPARATOR = ",";

  public static void main(String[] args) throws NumberFormatException, IOException, SAXException {

    // 0.0 -- read the data
    //
    Map<String, List<double[]>> CBFData = UCRUtils.readUCRData(TRAIN_DATA);
    LOGGER.info("read " + UCRUtils.datasetStats(CBFData, "\"" + TRAIN_DATA + "\" "));

    // 0.1 -- take the only class #1 and series #0 and mutate it ... later
    String key = "1";
    int index = 0;
    double[] series = CBFData.get(key).get(index);
    LOGGER.info(
        "fixed series of class " + key + ", index " + index + ", " + series.length + " points");

    // 0.2 disretize
    //
    SAXRecords sax = sp.ts2saxViaWindow(series, WINDOW_SIZE, PAA_SIZE,
        ALPHABET.getCuts(ALPHABET_SIZE), NR_STRATEGY, NORM_THRESHOLD);
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
    PrintWriter writer = new PrintWriter(new File("RCode/data/e01_string.txt"), "UTF-8");
    for (int i = 0; i < theString.length(); i++) {
      writer.println(theString.charAt(i));
    }
    writer.close();

    // 0.3 generate the curve
    //
    ArrayList<double[]> theCurve = new ArrayList<double[]>();
    RosslerEquations equations = new RosslerEquations(BASE_A, BASE_B, BASE_C);
    RosslerPrintingStepHandler stepHandler = new RosslerPrintingStepHandler(
        "RCode/data/e01_original_curve.txt", theCurve);

    ClassicalRungeKuttaIntegrator INTEGRATOR = new ClassicalRungeKuttaIntegrator(0.01);
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
    List<GraphEdge> result = alg.generateVoronoi(p, -8.5, 11, -10, 8);
    writer = new PrintWriter(new File("RCode/data/e01_tesselation.txt"), "UTF-8");
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
    tree.visualize(600, 600).save("RCode/data/e01_Rtree.png");

    LOGGER.info("producing a mutant");

    // 0.6 mutate the curve a bit
    //
    double a = BASE_A - 0.02;
    double b = BASE_B - 0.02;
    double c = BASE_C - 0.02;

    ArrayList<double[]> theMutatedCurve = new ArrayList<double[]>();
    equations = new RosslerEquations(a, b, c);
    stepHandler = new RosslerPrintingStepHandler("RCode/data/e01_mutated_curve.txt",
        theMutatedCurve);
    INTEGRATOR.addStepHandler(new StepNormalizer(0.1, stepHandler));
    INTEGRATOR.integrate(equations, 0, new double[] { 1., 1., 1. }, theString.length(),
        new double[3]);

    // 0.6 extract the new string
    //
    StringBuffer theNewString = new StringBuffer(indexes.size() * PAA_SIZE);
    for (int i = 0; i < Math.min(theMutatedCurve.size(), theString.length()); i++) {
      double[] newP = theMutatedCurve.get(i);
      com.github.davidmoten.rtree.geometry.Point point = Geometries.point(newP[1], newP[2]);
      Entry<String, Geometry> pp = tree.nearest(point, 3, 1).toBlocking().single();
      theNewString.append(pp.value());
    }

    writer = new PrintWriter(new File("RCode/data/e01_mutated_string.txt"), "UTF-8");
    for (int i = 0; i < theNewString.length(); i++) {
      writer.println(theNewString.charAt(i));
    }
    writer.close();

    //
    // get a shingled bitmaps
    Map<String, Integer> theOriginal = stringToShingles(theString.toString(), PAA_SIZE);

    ArrayList<String> keys = new ArrayList<String>();
    keys.addAll(theOriginal.keySet());
    Collections.sort(keys);

    double[][] heatmapData = new double[25][25];
    int counter = 0;
    for (String shingle : keys) {
      Integer value = theOriginal.get(shingle);
      heatmapData[counter / 25][counter % 25] = value;
      counter++;
    }
    writer = new PrintWriter(new File("RCode/data/e01_bitmap.txt"), "UTF-8");
    for (int i = 0; i < 25; i++) {
      StringBuffer sb = new StringBuffer();
      for (int j = 0; j < 25; j++) {
        sb.append(String.valueOf(heatmapData[i][j])).append(SEPARATOR);
      }
      writer.println(sb.delete(sb.length() - 1, sb.length()).toString());
    }
    writer.close();

    Map<String, Integer> theMutant = stringToShingles(theNewString.toString(), PAA_SIZE);

    heatmapData = new double[25][25];
    counter = 0;
    for (String shingle : keys) {
      Integer value = theMutant.get(shingle);
      heatmapData[counter / 25][counter % 25] = value;
      counter++;
    }
    writer = new PrintWriter(new File("RCode/data/e01_mutated_bitmap.txt"), "UTF-8");
    for (int i = 0; i < 25; i++) {
      StringBuffer sb = new StringBuffer();
      for (int j = 0; j < 25; j++) {
        sb.append(String.valueOf(heatmapData[i][j])).append(SEPARATOR);
      }
      writer.println(sb.delete(sb.length() - 1, sb.length()).toString());
    }
    writer.close();
  }

  private static Map<String, Integer> stringToShingles(String str, int paaSize) {

    String[] allShingles = SAXProcessor.getAllPermutations(alphabet, SHINGLE_SIZE);
    // result
    HashMap<String, Integer> res = new HashMap<String, Integer>(allShingles.length);
    for (String s : allShingles) {
      res.put(s, 0);
    }

    int ctr = 0;
    while (ctr < str.length()) {
      String word = str.subSequence(ctr, ctr + PAA_SIZE).toString();
      for (int i = 0; i <= word.length() - SHINGLE_SIZE; i++) {
        String shingle = word.substring(i, i + SHINGLE_SIZE);
        res.put(shingle, res.get(shingle) + 1);
      }
      ctr = ctr + PAA_SIZE;
    }

    return res;
  }

}
