package part2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import model.Record;

public class RecordWriter implements Runnable {

  BlockingQueue<Record> queue;
  private String pathToCsv;
  public static final String COMMA = ",";

  public RecordWriter(BlockingQueue<Record> queue, String pathToCsv) {
    this.queue = queue;
    this.pathToCsv = pathToCsv;
  }

  @Override
  public void run() {

    try {
      File csvFile = new File(pathToCsv);
      if (csvFile.exists())
        csvFile.delete();
      csvFile.createNewFile();
      FileWriter fileWriter = new FileWriter(csvFile);
      // headers = {start time, request type (ie POST), latency, response code}.
      fileWriter.append("Start Time");
      fileWriter.append(COMMA);
      fileWriter.append("Request Type");
      fileWriter.append(COMMA);
      fileWriter.append("Latency");
      fileWriter.append(COMMA);
      fileWriter.append("Response Code");
      fileWriter.append("\n");
      fileWriter.flush();

      while (true) {
        Record record = queue.take();
        if (record.getStartTime() == -1) break;
        fileWriter.append(String.valueOf(record.getStartTime()));
        fileWriter.append(COMMA);
        fileWriter.append(String.valueOf(record.getType()));
        fileWriter.append(COMMA);
        fileWriter.append(String.valueOf(record.getLatency()));
        fileWriter.append(COMMA);
        fileWriter.append(String.valueOf(record.getResCode()));
        fileWriter.append("\n");
        fileWriter.flush();
      }
      fileWriter.close();
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
