package net.seninp.attractor.experiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXCLIParameters;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;
import net.seninp.util.UCRUtils;

public class AttachTheDiscretization {

  final static SAXProcessor sp = new SAXProcessor();

  final static int WINDOW_SIZE = 60;
  final static int PAA_SIZE = 6;
  final static int ALPHABET_SIZE = 6;
  final static double NORM_THRESHOLD = 0.01;

  final static Alphabet ALPHABET = new NormalAlphabet();

  private static final String CR = "\n";
  private static final String SEP = "\t";

  public static void main(String[] args) throws NumberFormatException, IOException, SAXException {

    ArrayList<double[]> dat = readData("rossler_results_01.txt");

    Map<String, List<double[]>> data = UCRUtils.readUCRData("src/resources/data/CBF/CBF_TRAIN");

    double[] series = data.get("1").get(0);
    // System.out.print(Arrays.toString(series));

    SAXRecords sax = sp.ts2saxViaWindow(series, WINDOW_SIZE, PAA_SIZE,
        ALPHABET.getCuts(ALPHABET_SIZE), NumerosityReductionStrategy.NONE, NORM_THRESHOLD);

    ArrayList<Integer> indexes = new ArrayList<Integer>();
    indexes.addAll(sax.getIndexes());
    Collections.sort(indexes);

    PrintWriter writer = new PrintWriter(new File("rossler_results_01_named.txt"), "UTF-8");

    int gCounter = 0;
    for (Integer idx : indexes) {
      String str = String.valueOf(sax.getByIndex(idx).getPayload());
      for (char s : str.toCharArray()) {
        writer.write(String.valueOf(gCounter + SEP + dat.get(gCounter)[0] + SEP
            + dat.get(gCounter)[1] + SEP + dat.get(gCounter)[2] + SEP + "\"" + s + "\"" + CR));
        gCounter++;
      }
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
