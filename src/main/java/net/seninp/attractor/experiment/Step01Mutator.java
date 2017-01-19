package net.seninp.attractor.experiment;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
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
import net.seninp.jmotif.text.Params;
import net.seninp.jmotif.text.TextProcessor;
import net.seninp.jmotif.text.WordBag;
import net.seninp.util.UCRUtils;
import us.molini.graph.GraphFactory;

public class Step01Mutator {

  // private static final String CR = "\n";
  // private static final String COMMA = ", ";
  // private static final String TAB = "\t";

  // discretization parameters
  private final static int WINDOW_SIZE = 60;
  private final static int PAA_SIZE = 6;
  private final static int ALPHABET_SIZE = 6;
  private final static double NORM_THRESHOLD = 0.01;
  private static final NumerosityReductionStrategy NR_STRATEGY = NumerosityReductionStrategy.NONE;
  private final static Alphabet ALPHABET = new NormalAlphabet();
  private final static TextProcessor tp = new TextProcessor();
  private final static SAXProcessor sp = new SAXProcessor();

  // the data
  private static final String TRAIN_DATA = "src/resources/data/CBF/CBF_TRAIN";
  // private static final String TEST_DATA = "src/resources/data/CBF/CBF_TEST";

  // the curve
  private static final double BASE_A = 0.20;
  private static final double BASE_B = 0.20;
  private static final double BASE_C = 5.0;
  private static final ClassicalRungeKuttaIntegrator INTEGRATOR = new ClassicalRungeKuttaIntegrator(
      0.01);

  // the logger
  private static final Logger LOGGER = LoggerFactory.getLogger(TSProcessor.class);

  public static void main(String[] args) throws NumberFormatException, IOException, SAXException {

    // 0.0 -- read the data
    //
    Map<String, List<double[]>> CBFData = UCRUtils.readUCRData(TRAIN_DATA);
    LOGGER.info("read " + UCRUtils.datasetStats(CBFData, "\"" + TRAIN_DATA + "\" "));

    // 0.1 -- take the only class #1 and series #0 and mutate it ... later
    //
    // TODO: wrap into the loop
    String key = "3";
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
    PrintWriter writer = new PrintWriter(new File("e01_string.txt"), "UTF-8");
    for (int i = 0; i < theString.length(); i++) {
      writer.println(theString.charAt(i));
    }
    writer.close();

    // 0.3 generate the curve
    //
    ArrayList<double[]> theCurve = new ArrayList<double[]>();
    RosslerEquations equations = new RosslerEquations(BASE_A, BASE_B, BASE_C);
    RosslerStepHandler stepHandler = new RosslerStepHandler("e01_original_curve.txt", theCurve);
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
    List<GraphEdge> result = alg.generateVoronoi(p, -3, 5, -5.5, 2.5);
    writer = new PrintWriter(new File("e01_tesselation.txt"), "UTF-8");
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

    Hashtable<String, WordBag> mutatedSeries = new Hashtable<String, WordBag>();

    LOGGER.info("producing mutants");

    for (int j = 0; j < 30; j++) {

      // 0.6 mutate the curve a bit
      //
      double a = BASE_A + (0.05 - Math.random() / 10.0);
      double b = BASE_B + (0.05 - Math.random() / 10.0);
      double c = BASE_C + (0.05 - Math.random() / 10.0);

      ArrayList<double[]> theMutatedCurve = new ArrayList<double[]>();
      equations = new RosslerEquations(a, b, c);
      stepHandler = new RosslerStepHandler("e01_mutated_curve.txt", theMutatedCurve);
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

      // writer = new PrintWriter(new File("e01_mutated_string.txt"), "UTF-8");
      // for (int i = 0; i < theNewString.length(); i++) {
      // writer.println(theNewString.charAt(i));
      // }
      // writer.close();

      // save string into a word bag
      //
      WordBag wb = new WordBag(key + "_" + String.valueOf(j));
      int ctr = 0;
      while (ctr < theNewString.length()) {
        CharSequence word = theNewString.subSequence(ctr, ctr + PAA_SIZE);
        wb.addWord(word.toString());
        ctr = ctr + PAA_SIZE;
      }

      mutatedSeries.put(wb.getLabel(), wb);

      System.out.println(theNewString.toString());

    }

    LOGGER.info("training the classifier");

    Map<String, List<double[]>> trainData = UCRUtils.readUCRData(TRAIN_DATA);
    // Map<String, List<double[]>> testData = UCRUtils.readUCRData(TEST_DATA);

    Params params = new Params(WINDOW_SIZE, PAA_SIZE, ALPHABET_SIZE, NORM_THRESHOLD, NR_STRATEGY);

    List<WordBag> bags = tp.labeledSeries2WordBags(trainData, params);

    HashMap<String, HashMap<String, Double>> tfidf = tp.computeTFIDF(bags);

    // classifying
    int testSampleSize = 0;
    int positiveTestCounter = 0;

    for (java.util.Map.Entry<String, WordBag> e : mutatedSeries.entrySet()) {

      String predictedLabel = tp.classify(e.getValue(), tfidf);

      if (predictedLabel.equalsIgnoreCase(key)) {
        positiveTestCounter++;
      }

      testSampleSize++;

    }

    // accuracy and error
    double accuracy = (double) positiveTestCounter / (double) testSampleSize;
    double error = 1.0d - accuracy;

    // report results
    System.out.println("classification results: " + accuracy + "; " + error);

  }

}
