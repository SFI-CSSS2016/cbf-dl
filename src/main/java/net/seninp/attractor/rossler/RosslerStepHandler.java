package net.seninp.attractor.rossler;

import java.util.ArrayList;
import org.apache.commons.math3.ode.sampling.FixedStepHandler;

public class RosslerStepHandler implements FixedStepHandler {

  private ArrayList<double[]> steps;

  public RosslerStepHandler(String outFName, ArrayList<double[]> theCurve) {
    super();
    this.steps = theCurve;
    this.steps.clear();
  }

  public void handleStep(double t, double[] y, double[] yDot, boolean isLast) {
    double[] arr = { t, y[0], y[1], y[2] };
    steps.add(arr);
  }

  public void init(double arg0, double[] arg1, double arg2) {
    // TODO Auto-generated method stub
    assert true;
  }

}
