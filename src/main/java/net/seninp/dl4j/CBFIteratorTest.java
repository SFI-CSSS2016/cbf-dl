package net.seninp.dl4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

public class CBFIteratorTest {

  // the data
  private static final String TRAIN_DATA = "src/resources/data/CBF/cbf_train_original.csv";

  public static void main(String[] args)
      throws FileNotFoundException, IOException, InterruptedException {

    RecordReader recordReader = new CBFRecordReader(0, ",");

    // ClassPathResource res = new ClassPathResource(TRAIN_DATA);

//    RecordReader recordReader = new CSVRecordReader(0,",");
    
    FileSplit files = new FileSplit(new File(TRAIN_DATA));

    recordReader.initialize(files);

    // reader, label index, number of possible labels
    DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, 2);

    // get the dataset using the record reader. The datasetiterator handles vectorization
    DataSet next = iterator.next();

    // Customizing params
    Nd4j.MAX_SLICES_TO_PRINT = 10;
    Nd4j.MAX_ELEMENTS_PER_SLICE = 10;

    System.out.println(next);

  }

}
