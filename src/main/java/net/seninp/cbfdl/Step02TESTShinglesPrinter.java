package net.seninp.cbfdl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
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

public class Step02TESTShinglesPrinter {

  // discretization parameters
  private final static int WINDOW_SIZE = 60;
  private final static int PAA_SIZE = 6;
  private final static int ALPHABET_SIZE = 5;
  private final static double NORM_THRESHOLD = 0.01;
  private static final NumerosityReductionStrategy NR_STRATEGY = NumerosityReductionStrategy.NONE;

  // processors
  private final static SAXProcessor sp = new SAXProcessor();
  private final static Alphabet ALPHABET = new NormalAlphabet();
  private final static String[] alphabet = String
      .copyValueOf(Arrays.copyOfRange(NormalAlphabet.ALPHABET, 0, ALPHABET_SIZE)).split("");

  // shingling params
  private final static int SHINGLE_SIZE = 4;

  // the CBF train data
  private static final String TRAIN_DATA = "src/resources/data/CBF/CBF_TRAIN";

  // constants
  private static final String SEPARATOR = ",";
  private static final String CR = "\n";

  // the logger
  private static final Logger LOGGER = LoggerFactory.getLogger(TSProcessor.class);

  // the main executable
  //
  public static void main(String[] args) throws NumberFormatException, IOException, SAXException {

    // 0.0 -- read the data
    //
    Map<String, List<double[]>> CBFData = UCRUtils.readUCRData(TRAIN_DATA);
    LOGGER.info("read " + UCRUtils.datasetStats(CBFData, "\"" + TRAIN_DATA + "\" "));

    // 0.1 -- iterate over the training classes and series
    //
    Hashtable<String, String> CBFStrings = new Hashtable<String, String>();

    // fix the class
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

        CBFStrings.put(seriesKey + "_" + String.valueOf(seriesIdx), theString.toString());

        seriesIdx++;

      }

    }

    Shingles allShingles = new Shingles(ALPHABET_SIZE, SHINGLE_SIZE);

    for (java.util.Map.Entry<String, String> e : CBFStrings.entrySet()) {
      Map<String, Integer> shingles = stringToShingles(e.getValue(), PAA_SIZE);
      allShingles.addShingledSeries(e.getKey(), shingles);
    }

    ArrayList<String> keys = new ArrayList<String>();
    keys.addAll(allShingles.getShingles().keySet());
    Collections.sort(keys);

    PrintWriter writer = new PrintWriter(new File("src/resources/data/CBF/CBF_TRAIN_shingled.txt"),
        "UTF-8");

    ArrayList<String> shingles = new ArrayList<String>();
    shingles.addAll(allShingles.getShinglesIndex().keySet());
    Collections.sort(shingles);

    StringBuffer header = new StringBuffer();
    for (String s : shingles) {
      header.append(s).append(SEPARATOR);
    }
    writer.print(header.deleteCharAt(header.length() - 1) + SEPARATOR + "key" + CR);

    for (String k : keys) {
      int[] freqArray = allShingles.get(k);
      StringBuffer line = new StringBuffer();
      for (String s : shingles) {
        line.append(freqArray[allShingles.getShinglesIndex().get(s)]).append(SEPARATOR);
      }
      writer.print(line.deleteCharAt(line.length() - 1) + SEPARATOR
          + String.valueOf(Integer.valueOf(k.substring(0, 1)) - 1) + CR);
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
