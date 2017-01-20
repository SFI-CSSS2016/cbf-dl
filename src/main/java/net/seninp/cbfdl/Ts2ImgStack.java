package net.seninp.cbfdl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.bitmap.Shingles;
import net.seninp.util.UCRUtils;

public class Ts2ImgStack {

  private static final String CBF_TRAIN = "src/resources/data/CBF/CBF_TEST";

  // discretization parameters
  private final static int WINDOW_SIZE = 60;
  private final static int PAA_SIZE = 6;
  private final static int ALPHABET_SIZE = 5;
  private final static double NORM_THRESHOLD = 0.01;
  private static final NumerosityReductionStrategy NR_STRATEGY = NumerosityReductionStrategy.NONE;

  private final static int SHINGLE_SIZE = 4;

  private static final SAXProcessor sp = new SAXProcessor();

  private static final String SEPARATOR = ",";
  private static final String CR = "\n";

  public static void main(String[] args) throws NumberFormatException, IOException, SAXException {

    Map<String, List<double[]>> dat = UCRUtils.readUCRData(CBF_TRAIN);

    System.out.println(UCRUtils.datasetStats(dat, "CBF"));

    Shingles allShingles = new Shingles(ALPHABET_SIZE, SHINGLE_SIZE);

    for (Entry<String, List<double[]>> le : dat.entrySet()) {
      for (int i = 0; i < le.getValue().size(); i++) {

        double[] series = le.getValue().get(i);

        Map<String, Integer> shingles = sp.ts2Shingles(series, WINDOW_SIZE, PAA_SIZE, ALPHABET_SIZE,
            NR_STRATEGY, NORM_THRESHOLD, SHINGLE_SIZE);

        String label = le.getKey() + "_" + String.valueOf(i);

        allShingles.addShingledSeries(label, shingles);

      }
    }

    ArrayList<String> keys = new ArrayList<String>();
    keys.addAll(allShingles.getShingles().keySet());
    Collections.sort(keys);

    PrintWriter writer = new PrintWriter(new File("shingled_CBF.txt"), "UTF-8");

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
      // writer.print(k + SEPARATOR + line.deleteCharAt(line.length() - 1) + CR);
      writer.print(line.deleteCharAt(line.length() - 1) + SEPARATOR
          + (Integer.valueOf(k.substring(0, 1)) - 1) + CR);
    }
    // for (double[] step : steps) {
    // writer.println(step[0] + TAB + step[1] + TAB + step[2] + TAB + step[3]);
    // }
    writer.close();

  }
}
