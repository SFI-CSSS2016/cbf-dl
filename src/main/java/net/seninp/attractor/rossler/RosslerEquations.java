package net.seninp.attractor.rossler;

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;

public class RosslerEquations implements FirstOrderDifferentialEquations {

  public double a;
  public double b;
  public double c;

  public RosslerEquations(double a, double b, double c) {
    this.a = a;
    this.b = b;
    this.c = c;
  }

  public void computeDerivatives(double t, double[] y, double[] yDot) {
    yDot[0] = -y[1] - y[2]; // dx/dt = - y - z)
    yDot[1] = y[0] + a * y[1]; // dy/dt = x + ay
    yDot[2] = b + y[2] * (y[0] - c); // dz/dt = b + z(x-c)
  }

  public int getDimension() {
    return 3;
  }

}