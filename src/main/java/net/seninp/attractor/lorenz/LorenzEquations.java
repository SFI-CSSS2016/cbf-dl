package net.seninp.attractor.lorenz;

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;

public class LorenzEquations implements FirstOrderDifferentialEquations {

  public double rho; // d
  public double sigma; // p
  public double beta; // B

  public LorenzEquations(double r, double s, double B) {
    this.rho = r;
    this.sigma = s;
    this.beta = B;
  }

  public void computeDerivatives(double t, double[] y, double[] yDot) {
    yDot[0] = sigma * (y[1] - y[0]); // dx/dt = d(y-x)
    yDot[1] = y[0] * (rho - y[2]) - y[1]; // dy/dt = x(p-z)-y
    yDot[2] = y[0] * y[1] - beta * y[2]; // dz/dt = xy - Bz
  }

  public int getDimension() {
    return 3;
  }

}