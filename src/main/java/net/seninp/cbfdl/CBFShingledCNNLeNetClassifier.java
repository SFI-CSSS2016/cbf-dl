package net.seninp.cbfdl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A first try to see the cross-validation accuracy using shingles and NN.
 * 
 * @author psenin
 *
 */
public class CBFShingledCNNLeNetClassifier {

  // the data
  private static final String TRAIN_DATA = "shingled_mutant_CBF.txt";
  private static final String TEST_DATA = "src/resources/data/CBF/CBF_TEST_shingled.txt";

  private static Logger log = LoggerFactory.getLogger(CBFShingledCNNLeNetClassifier.class);

  public static void main(String[] args)
      throws FileNotFoundException, IOException, InterruptedException {

    // Initialize the user interface backend
    UIServer uiServer = UIServer.getInstance();

    // Configure where the network information (gradients, score vs. time etc) is to be stored.
    // Here: store in memory.
    StatsStorage statsStorage = new InMemoryStatsStorage(); // Alternative: new
                                                            // FileStatsStorage(File), for saving
                                                            // and loading later

    // Attach the StatsStorage instance to the UI: this allows the contents of the StatsStorage to
    // be visualized
    uiServer.attach(statsStorage);

    // [1.0] describe the dataset
    //
    int labelIndex = 625; // 128 values in each row of the CBF file: 128 input features followed by
    // an integer label (class) index. Labels are the 129th value (index 128) in each row

    int numClasses = 3; // 3 classes (types of CBF) in the modified CBF data set. Classes have
    // integer values 0, 1 or 2

    int batchSize = 3000; // CBF train data set: 29 examples total. We are loading all of them into
                          // one DataSet (not recommended for large data sets)

    RecordReader recordReader = new CBFRecordReader(1, ",");
    FileSplit files = new FileSplit(new File(TRAIN_DATA));
    recordReader.initialize(files);
    DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, batchSize, labelIndex,
        numClasses);
    DataSet allData = iterator.next();
    // allData.shuffle();
    // SplitTestAndTrain testAndTrain = allData.splitTestAndTrain(0.65); // Use 65% of data for
    DataSet trainingData = allData;
//    DataSet testData = testAndTrain.getTest();

    
    RecordReader recordReader2 = new CBFRecordReader(1, ",");
    FileSplit files2 = new FileSplit(new File(TEST_DATA));
    recordReader2.initialize(files2);
    DataSetIterator iterator2 = new RecordReaderDataSetIterator(recordReader2, batchSize, labelIndex,
        numClasses);
    DataSet testData = iterator2.next();
    
    // We need to normalize our data. We'll use NormalizeStandardize (which gives us mean 0, unit
    // variance):
    DataNormalization normalizer = new NormalizerStandardize();
    normalizer.fit(trainingData); // Collect the statistics (mean/stdev) from the training data.
                                  // This does not modify the input data
    normalizer.transform(trainingData); // Apply normalization to the training data
    normalizer.transform(testData); // Apply normalization to the test data. This is using
                                    // statistics calculated from the *training* set

    int nChannels = 1; // Number of input channels
    int outputNum = 3; // The number of possible outcomes
    int iterations = 30; // Number of training iterations
    int seed = 123; //

    log.info("Build model....");
    MultiLayerConfiguration.Builder builder = new NeuralNetConfiguration.Builder().seed(seed)
        .iterations(iterations) // Training iterations as above
        .regularization(true).l2(0.0005)
        /*
         * Uncomment the following for learning decay and bias
         */
        .learningRate(.01).biasLearningRate(0.02)
        // .learningRateDecayPolicy(LearningRatePolicy.Inverse).lrPolicyDecayRate(0.001).lrPolicyPower(0.75)
        .weightInit(WeightInit.XAVIER)
        .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
        .updater(Updater.NESTEROVS).momentum(0.9).list()
        .layer(0,
            new ConvolutionLayer.Builder(5, 5)
                // nIn and nOut specify depth. nIn here is the nChannels and nOut is the number of
                // filters to be applied
                .nIn(nChannels).stride(1, 1).nOut(20).activation("identity").build())
        .layer(1,
            new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX).kernelSize(2, 2)
                .stride(2, 2).build())
        .layer(2,
            new ConvolutionLayer.Builder(5, 5)
                // Note that nIn need not be specified in later layers
                .stride(1, 1).nOut(50).activation("identity").build())
        .layer(3,
            new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX).kernelSize(2, 2)
                .stride(2, 2).build())
        .layer(4, new DenseLayer.Builder().activation("relu").nOut(500).build())
        .layer(5,
            new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                .nOut(outputNum).activation("softmax").build())
        .setInputType(InputType.convolutionalFlat(25, 25, 1)) // See note below
        .backprop(true).pretrain(false);

    // run the model
    MultiLayerConfiguration conf = builder.build();
    MultiLayerNetwork model = new MultiLayerNetwork(conf);
    model.init();
    model.setListeners(new ScoreIterationListener(10));
    // Then add the StatsListener to collect this information from the network, as it trains
    model.setListeners(new StatsListener(statsStorage));

    model.fit(trainingData);

    // evaluate the model on the test set
    Evaluation eval = new Evaluation(3);
    //
    INDArray output = model.output(testData.getFeatureMatrix());
    eval.eval(testData.getLabels(), output);
    log.info(eval.stats());

  }

}
