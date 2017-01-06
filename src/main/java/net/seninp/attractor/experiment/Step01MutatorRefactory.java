package net.seninp.attractor.experiment;

import java.io.IOException;
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

public class Step01MutatorRefactory {

  private static final String CR = "\n";
  private static final String COMMA = ", ";
  private static final String TAB = "\t";

  // discretization parameters
  private final static int WINDOW_SIZE = 70;
  private final static int PAA_SIZE = 6;
  private final static int ALPHABET_SIZE = 5;
  private final static double NORM_THRESHOLD = 0.01;
  private static final NumerosityReductionStrategy NR_STRATEGY = NumerosityReductionStrategy.NONE;
  private final static Alphabet ALPHABET = new NormalAlphabet();
  private final static TextProcessor tp = new TextProcessor();
  private final static SAXProcessor sp = new SAXProcessor();

  // the data
  private static final String TRAIN_DATA = "src/resources/data/CBF/CBF_TRAIN";
  private static final String TEST_DATA = "src/resources/data/CBF/CBF_TEST";

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
    String key = "1";
    int index = 0;
    double[] series = CBFData.get(key).get(index);
    LOGGER.info(
        "fixed series of class " + key + ", index " + index + ", " + series.length + " points");

    // 0.2 disretize the series to a string
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

    Hashtable<String, String> mutatedStrings = mutateStringRossler(theString.toString(), key, 100);

    LOGGER.info("training the classifier");

    Map<String, List<double[]>> trainData = UCRUtils.readUCRData(TRAIN_DATA);
    Map<String, List<double[]>> testData = UCRUtils.readUCRData(TEST_DATA);
    Params params = new Params(WINDOW_SIZE, PAA_SIZE, ALPHABET_SIZE, NORM_THRESHOLD, NR_STRATEGY);
    List<WordBag> bags = tp.labeledSeries2WordBags(trainData, params);
    HashMap<String, HashMap<String, Double>> tfidf = tp.computeTFIDF(bags);
    int testSampleSize = 0;
    int positiveTestCounter = 0;
    for (String label : tfidf.keySet()) {
      List<double[]> testD = testData.get(label);
      for (double[] ts : testD) {
        positiveTestCounter = positiveTestCounter + tp.classify(label, ts, tfidf, params);
        testSampleSize++;
      }
    }
    double accuracy = (double) positiveTestCounter / (double) testSampleSize;
    double error = 1.0d - accuracy;
    System.out
        .println("STANADARD CBF classification results: accuracy " + accuracy + ", error " + error);

    // classifying
    //
    testSampleSize = 0;
    positiveTestCounter = 0;

    for (java.util.Map.Entry<String, String> e : mutatedStrings.entrySet()) {
      String predictedLabel = tp.classify(toWordBag(e.getKey(), e.getValue(), PAA_SIZE), tfidf);
      if (predictedLabel.equalsIgnoreCase(key)) {
        positiveTestCounter++;
      }
      testSampleSize++;
    }
    // accuracy and error
    accuracy = (double) positiveTestCounter / (double) testSampleSize;
    error = 1.0d - accuracy;
    // report results
    System.out.println("mutants classification results: accuracy " + accuracy + "; error " + error);

  }

  private static WordBag toWordBag(String bagLabel, String str, int paaSize) {
    WordBag wb = new WordBag(bagLabel);
    int ctr = 0;
    while (ctr < str.length()) {
      CharSequence word = str.subSequence(ctr, ctr + PAA_SIZE);
      wb.addWord(word.toString());
      ctr = ctr + PAA_SIZE;
    }
    return wb;
  }

  private static Hashtable<String, String> mutateStringRossler(String theString, String keyPrefix,
      int mutantsNum) {

    // 0.3 generate the curve
    //
    ArrayList<double[]> theCurve = new ArrayList<double[]>();
    RosslerEquations equations = new RosslerEquations(BASE_A, BASE_B, BASE_C);
    RosslerStepHandler stepHandler = new RosslerStepHandler("e01_original_curve.txt", theCurve);
    INTEGRATOR.addStepHandler(new StepNormalizer(0.1, stepHandler));
    INTEGRATOR.integrate(equations, 0, new double[] { 1., 1., 1. }, theString.length() * 0.1,
        new double[3]);

    // 0.5 build the tree of the original series points
    //
    RTree<String, Geometry> tree = RTree.create();
    for (int i = 0; i < Math.min(theCurve.size(), theString.length()); i++) {
      double[] pp = theCurve.get(i);
      tree = tree.add(String.valueOf(theString.charAt(i)), Geometries.point(pp[1], pp[2]));
    }
    // tree.visualize(600, 600).save("target/mytree.png");

    Hashtable<String, String> res = new Hashtable<String, String>();

    for (int j = 0; j < mutantsNum; j++) {

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
      StringBuffer theNewString = new StringBuffer(theString.length());
      for (int i = 0; i < Math.min(theMutatedCurve.size(), theString.length()); i++) {
        double[] newP = theMutatedCurve.get(i);
        com.github.davidmoten.rtree.geometry.Point point = Geometries.point(newP[1], newP[2]);
        Entry<String, Geometry> pp = tree.nearest(point, 3, 1).toBlocking().single();
        theNewString.append(pp.value());
      }

      res.put(keyPrefix + "_" + String.valueOf(j), theNewString.toString());

    }

    return res;
  }

}
