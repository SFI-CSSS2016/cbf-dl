package net.seninp.cbfdl;

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
import net.seninp.attractor.rossler.RosslerEquations;
import net.seninp.attractor.rossler.RosslerStepHandler;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.bitmap.Shingles;
import net.seninp.jmotif.sax.datastructure.SAXRecords;
import net.seninp.util.UCRUtils;

public class Step02ShinglesPrinter {

  // private static final String CR = "\n";
  // private static final String COMMA = ", ";
  // private static final String TAB = "\t";

  // discretization parameters
  private final static int WINDOW_SIZE = 60;
  private final static int PAA_SIZE = 6;
  private final static int ALPHABET_SIZE = 5;
  private final static double NORM_THRESHOLD = 0.01;
  private static final NumerosityReductionStrategy NR_STRATEGY = NumerosityReductionStrategy.NONE;
  private final static Alphabet ALPHABET = new NormalAlphabet();
  private final static SAXProcessor sp = new SAXProcessor();

  private final static String[] alphabet = { "a", "b", "c", "d", "e" };
  private final static int SHINGLE_SIZE = 4;

  // the data
  private static final String TRAIN_DATA = "src/resources/data/CBF/CBF_TRAIN";
  // private static final String TEST_DATA = "src/resources/data/CBF/CBF_TEST";

  // the curve
  private static final double BASE_A = 0.20;
  private static final double BASE_B = 0.20;
  private static final double BASE_C = 5.0;

  // other constants
  private static final int MUTANTS_NUMBER = 100;

  private static final String SEPARATOR = ",";
  private static final String CR = "\n";

  // the logger
  private static final Logger LOGGER = LoggerFactory.getLogger(TSProcessor.class);

  public static void main(String[] args) throws NumberFormatException, IOException, SAXException {

    // 0.0 -- read the data
    //
    Map<String, List<double[]>> CBFData = UCRUtils.readUCRData(TRAIN_DATA);
    LOGGER.info("read " + UCRUtils.datasetStats(CBFData, "\"" + TRAIN_DATA + "\" "));

    // 0.1 -- iterate over the training classes and series
    //
    Hashtable<String, String> CBFMutants = new Hashtable<String, String>();

    for (java.util.Map.Entry<String, List<double[]>> trainEntry : CBFData.entrySet()) {

      String seriesKey = trainEntry.getKey();

      // iterate over the class' series
      int seriesIdx = 0;
      while (seriesIdx < trainEntry.getValue().size()) {

        double[] series = CBFData.get(seriesKey).get(seriesIdx);

        LOGGER.info("processing series of class " + seriesKey + ", index " + seriesIdx);

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
        System.out.println(theString.toString());

        // 0.3 obtain the list of mutants
        //
        Hashtable<String, String> mutatedStrings = mutateStringRossler(theString.toString(),
            seriesKey + "_" + String.valueOf(seriesIdx), MUTANTS_NUMBER);

        CBFMutants.putAll(mutatedStrings);

        seriesIdx++;

      }

    }

    LOGGER.info("done mutations");

    Shingles allShingles = new Shingles(ALPHABET_SIZE, SHINGLE_SIZE);

    for (java.util.Map.Entry<String, String> e : CBFMutants.entrySet()) {

      Map<String, Integer> shingles = stringToShingles(e.getValue(), PAA_SIZE);
      allShingles.addShingledSeries(e.getKey(), shingles);
      
    }

    ArrayList<String> keys = new ArrayList<String>();
    keys.addAll(allShingles.getShingles().keySet());
    Collections.sort(keys);

    PrintWriter writer = new PrintWriter(new File("shingled_mutant_CBF.txt"), "UTF-8");

    ArrayList<String> shingles = new ArrayList<String>();
    shingles.addAll(allShingles.getShinglesIndex().keySet());
    Collections.sort(shingles);

    StringBuffer header = new StringBuffer();
    for (String s : shingles) {
      header.append(s).append(SEPARATOR);
    }
    // writer.print("key" + TAB + header.deleteCharAt(header.length() - 1) + CR);

    for (String k : keys) {
      int[] freqArray = allShingles.get(k);
      StringBuffer line = new StringBuffer();
      for (String s : shingles) {
        line.append(freqArray[allShingles.getShinglesIndex().get(s)]).append(SEPARATOR);
      }
      writer.print(line.deleteCharAt(line.length() - 1) + SEPARATOR
          + (Integer.valueOf(k.substring(0, 1)) - 1) + CR);
    }
    // for (double[] step : steps) {
    // writer.println(step[0] + TAB + step[1] + TAB + step[2] + TAB + step[3]);
    // }
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

  private static Hashtable<String, String> mutateStringRossler(String theString, String keyPrefix,
      int mutantsNum) {

    // 0.3 generate the curve
    //
    ArrayList<double[]> theCurve = new ArrayList<double[]>();
    RosslerEquations equations = new RosslerEquations(BASE_A, BASE_B, BASE_C);
    RosslerStepHandler stepHandler = new RosslerStepHandler("e01_original_curve.txt", theCurve);
    ClassicalRungeKuttaIntegrator INTEGRATOR = new ClassicalRungeKuttaIntegrator(0.01);
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
      double a = BASE_A + (0.05 - Math.random() / 7.0);
      double b = BASE_B + (0.05 - Math.random() / 7.0);
      double c = BASE_C + (0.05 - Math.random() / 7.0);

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

    equations = null;
    stepHandler = null;
    INTEGRATOR = null;
    tree = null;
    theCurve = null;
    System.gc();

    return res;
  }

}
