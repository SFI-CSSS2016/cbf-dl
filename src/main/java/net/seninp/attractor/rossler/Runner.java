package net.seninp.attractor.rossler;

import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import org.apache.commons.math3.ode.sampling.StepNormalizer;

public class Runner {

  public static void main(String[] args) {

    // FirstOrderIntegrator integrator = new DormandPrince853Integrator(1.0E-8, 10E-5, 1.0E-20,
    // 1.0E-20);
    ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.01);

    RosslerEquations equations = new RosslerEquations(0.432, 2., 4);

    RosslerStepHandler lz = new RosslerStepHandler();

    integrator.addStepHandler(new StepNormalizer(0.01, lz));

    integrator.integrate(equations, 0, new double[] { 1., 1., 1. }, 100, new double[3]);

  }

}
