package net.seninp.attractor.experiment;

import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import org.apache.commons.math3.ode.sampling.StepNormalizer;
import net.seninp.attractor.rossler.RosslerEquations;
import net.seninp.attractor.rossler.RosslerStepHandler;

public class PrintTheDots {

  public static void main(String[] args) {

    ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.01);

    RosslerEquations equations = new RosslerEquations(0.441, 1.99, 3.75);

    RosslerStepHandler lz = new RosslerStepHandler();

    integrator.addStepHandler(new StepNormalizer(0.1, lz));

    integrator.integrate(equations, 0, new double[] { 1., 1., 1. }, 1000, new double[3]);

  }

}
