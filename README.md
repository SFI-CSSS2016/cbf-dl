# cbf-dl - classifying CBF data using DL & NN

###1.0 An example of Rössler attractor-based time series discretization mutation
#![Rössler tesselation-based mutator example](https://raw.githubusercontent.com/SFI-CSSS2016/cbf-dl/master/RCode/tessellation_test01.png)

For this example a single Cylinder time series was 
-  discretized with SAX via sliding window
-  the resulting SAX words were concatenated into a single string
-  which was mapped onto the attractor's curve (`RosslerEquations(0.439, 1.99, 3.75)` starting at `{ 1., 1., 1. }`)
-  these are plotted with `cornflowerblue` colors

Next,
-  a new Rössler curve was generated with slightly different parameters (`RosslerEquations(0.440, 2.01, 3.74)` starting at `{ 0.99, 1., 1. }`) -- plotted with `brown` color
- whose points were mapped into the letters (we use Voronoi tesselation to create polygons and RTree to perform mapping efficiently), resulting in a new (_potential_) representative of the same Cylinder class time series in the discretized space ...

###2.0 First sanity check before diving to DL
For the sanity check we use [SAX-VSM classifier](https://github.com/jMotif/sax-vsm_classic) built for CBF dataset. Once built, the classifier will be used for all the new data points generated with the above procedure based on Rössler attractor. Expected is the high accuracy... but might be not... Let's see... "_The data point_" here is the character sequence converted into a word bag.

Apparently the accuracy is 100% for classifying the mutated series

    $ java -Xmx14G -cp "target/cbf-dl-0.0.1-SNAPSHOT-jar-with-dependencies.jar" net.seninp.attractor.experiment.Step01MutatorRefactory
    11:34:14.388 [main] INFO  net.seninp.jmotif.sax.TSProcessor - read "src/resources/data/CBF/CBF_TRAIN" classes: 3, series length min: 128, max: 128, min value: -2.3168621, max value: 3.2445649;"src/resources/data/CBF/CBF_TRAIN"  class: 1 series: 10;"src/resources/data/CBF/CBF_TRAIN"  class: 2 series: 12;"src/resources/data/CBF/CBF_TRAIN"  class: 3 series: 8
    11:34:14.394 [main] INFO  net.seninp.jmotif.sax.TSProcessor - processing series of class 1, index 0
    11:34:14.684 [main] INFO  net.seninp.jmotif.sax.TSProcessor - processing series of class 1, index 1
    ...
    11:37:23.165 [main] INFO  net.seninp.jmotif.sax.TSProcessor - processing series of class 3, index 7
    11:37:50.259 [main] INFO  net.seninp.jmotif.sax.TSProcessor - training the classifier
    STANADARD CBF classification results: accuracy 0.9966666666666667, error 0.0033333333333332993
    mutants classification results: accuracy 1.0; error 0.0
    
###3.0 Second sanity check before diving to DL
Second sanity check would be to look on the shingles for CBF classes -- both the original data and the mutants...
