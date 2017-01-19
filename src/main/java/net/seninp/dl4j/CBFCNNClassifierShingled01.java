package net.seninp.dl4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CBFCNNClassifierShingled01 {

  // the data
  private static final String TRAIN_DATA = "shingled_mutant_CBF.txt";
  private static final String TEST_DATA = "shingled_CBF.txt";

  private static Logger log = LoggerFactory.getLogger(CBFCNNClassifierShingled01.class);

  public static void main(String[] args)
      throws FileNotFoundException, IOException, InterruptedException {

    // [1.0] describe the dataset
    //
    int labelIndex = 216; // 128 values in each row of the CBF file: 128 input features followed by
    // an integer label (class) index. Labels are the 129th value (index 128) in each row

    int numClasses = 3; // 3 classes (types of CBF) in the modified CBF data set. Classes have
    // integer values 0, 1 or 2

    int batchSize = 3000; // CBF train data set: 29 examples total. We are loading all of them into
                          // one DataSet (not recommended for large data sets)

    RecordReader recordReader = new CBFRecordReader(0, ",");
    FileSplit files = new FileSplit(new File(TRAIN_DATA));
    recordReader.initialize(files);
    DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, batchSize, labelIndex,
        numClasses);
    DataSet trainingData = iterator.next();
    // allData.shuffle();
    // SplitTestAndTrain testAndTrain = allData.splitTestAndTrain(0.65); // Use 65% of data for
    // training

    // DataSet trainingData = testAndTrain.getTrain();
    files = new FileSplit(new File(TEST_DATA));
    recordReader.initialize(files);
    iterator = new RecordReaderDataSetIterator(recordReader, 900, labelIndex, numClasses);
    DataSet testData = iterator.next();

    // We need to normalize our data. We'll use NormalizeStandardize (which gives us mean 0, unit
    // variance):
    DataNormalization normalizer = new NormalizerStandardize();
    normalizer.fit(trainingData); // Collect the statistics (mean/stdev) from the training data.
                                  // This does not modify the input data
    normalizer.transform(trainingData); // Apply normalization to the training data
    normalizer.transform(testData); // Apply normalization to the test data. This is using
                                    // statistics calculated from the *training* set

    final int numInputs = 216;
    int outputNum = 3;
    int iterations = 10000;
    long seed = 6;

    log.info("Build model....");
    MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder().seed(seed)
        .iterations(iterations).activation("relu").weightInit(WeightInit.XAVIER).learningRate(0.05)
        .regularization(true).l2(1e-4).list()
        .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(108).build())
        .layer(1, new DenseLayer.Builder().nIn(108).nOut(3).build())
        .layer(2,
            new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                .activation("softmax").nIn(3).nOut(outputNum).build())
        .backprop(true).pretrain(false).build();

    // run the model
    MultiLayerNetwork model = new MultiLayerNetwork(conf);
    model.init();
    model.setListeners(new ScoreIterationListener(100));

    model.fit(trainingData);

    // evaluate the model on the test set
    Evaluation eval = new Evaluation(3);
    //
    INDArray output = model.output(testData.getFeatureMatrix());
    eval.eval(testData.getLabels(), output);
    log.info(eval.stats());

  }

}
