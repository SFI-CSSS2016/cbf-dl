package net.seninp.cbfdl;

import java.util.ArrayList;
import java.util.Hashtable;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import org.apache.commons.math3.ode.sampling.StepNormalizer;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import net.seninp.attractor.rossler.RosslerEquations;
import net.seninp.attractor.rossler.RosslerStepHandler;

public class RosslerMutator {

  // the Rossler curve initial parameters
  private double BASE_A = 0.20;
  private double BASE_B = 0.20;
  private double BASE_C = 5.0;

  public RosslerMutator(double a, double b, double c) {
    BASE_A = a;
    BASE_B = b;
    BASE_C = c;
  }

  public Hashtable<String, String> mutateStringRossler(String str, String keyPrefix,
      int mutantsNum) {

    // 0.3 generate the curve
    //
    ArrayList<double[]> theCurve = new ArrayList<double[]>();
    RosslerEquations equations = new RosslerEquations(BASE_A, BASE_B, BASE_C);
    RosslerStepHandler stepHandler = new RosslerStepHandler("e01_original_curve.txt", theCurve);
    ClassicalRungeKuttaIntegrator INTEGRATOR = new ClassicalRungeKuttaIntegrator(0.01);
    INTEGRATOR.addStepHandler(new StepNormalizer(0.1, stepHandler));
    INTEGRATOR.integrate(equations, 0, new double[] { 1., 1., 1. }, str.length() * 0.1,
        new double[3]);

    // 0.5 build the tree of the original series points
    //
    RTree<String, Geometry> tree = RTree.create();
    for (int i = 0; i < Math.min(theCurve.size(), str.length()); i++) {
      double[] pp = theCurve.get(i);
      tree = tree.add(String.valueOf(str.charAt(i)), Geometries.point(pp[1], pp[2]));
    }
    // tree.visualize(600, 600).save("target/mytree.png");

    Hashtable<String, String> res = new Hashtable<String, String>();

    for (int j = 0; j < mutantsNum; j++) {

      // 0.6 mutate the curve a bit
      //
      double a = BASE_A + (0.05 - Math.random() / 7.0);
      double b = BASE_B + (0.05 - Math.random() / 7.0);
      double c = BASE_C + (0.05 - Math.random() / 7.0);

      ArrayList<double[]> theMutatedCurve = new ArrayList<double[]>();
      equations = new RosslerEquations(a, b, c);
      stepHandler = new RosslerStepHandler("e01_mutated_curve.txt", theMutatedCurve);
      INTEGRATOR = new ClassicalRungeKuttaIntegrator(0.01);
      INTEGRATOR.addStepHandler(new StepNormalizer(0.1, stepHandler));
      INTEGRATOR.integrate(equations, 0, new double[] { 1., 1., 1. }, str.length(), new double[3]);

      // 0.6 extract the new string
      //
      StringBuffer theNewString = new StringBuffer(str.length());
      for (int i = 0; i < Math.min(theMutatedCurve.size(), str.length()); i++) {
        double[] newP = theMutatedCurve.get(i);
        com.github.davidmoten.rtree.geometry.Point point = Geometries.point(newP[1], newP[2]);
        Entry<String, Geometry> pp = tree.nearest(point, 4, 1).toBlocking().single();
        theNewString.append(pp.value());
      }

      // for (double[] e : theMutatedCurve) {
      // e = null;
      // }
      theMutatedCurve.clear();
      System.gc();

      res.put(keyPrefix + "_" + String.valueOf(j), theNewString.toString());

    }

    equations = null;
    stepHandler = null;
    INTEGRATOR = null;
    tree = null;
    theCurve = null;
    System.gc();

    return res;

  }
}
