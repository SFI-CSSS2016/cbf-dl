package net.seninp.cbfdl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.util.UCRUtils;

public class Ts2ImgStack {

  private static final String CBF_TRAIN = "src/resources/data/CBF/CBF_TRAIN";

  private static final SAXProcessor sp = new SAXProcessor();

  public static void main(String[] args) throws NumberFormatException, IOException, SAXException {

    Map<String, List<double[]>> dat = UCRUtils.readUCRData(CBF_TRAIN);

    System.out.println(UCRUtils.datasetStats(dat, "CBF"));

    for (Entry<String, List<double[]>> le : dat.entrySet()) {
      for (double[] series : le.getValue()) {
        Map<String, Integer> shingles = sp.ts2Shingles(series, 60, 8, 4,
            NumerosityReductionStrategy.NONE, 0.001, 4);
      }
    }

    // shingles = sp.ts2Shingles(series, windowSize, paaSize, alphabetSize,
    // strategy, nrThreshold, shingleSize)

  }
}
