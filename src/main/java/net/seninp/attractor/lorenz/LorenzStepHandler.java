package net.seninp.attractor.lorenz;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.apache.commons.math3.ode.sampling.FixedStepHandler;

public class LorenzStepHandler implements FixedStepHandler {

  ArrayList<String> steps = new ArrayList<String>();

  public void handleStep(double t, double[] y, double[] yDot, boolean isLast) {

    steps.add(t + " " + y[0] + " " + y[1] + " " + y[2]);

    if (isLast) {
      try {
        PrintWriter writer = new PrintWriter(new File("results.txt"), "UTF-8");
        for (String step : steps) {
          writer.println(step);
        }
        writer.close();
      }
      catch (Exception e) {
      }

    }

  }

  public void init(double t0, double[] y0, double t) {
    // TODO Auto-generated method stub

  }

}
