package model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class LogStderr {
  private static final PrintStream console = System.err;
  private static File file;
  private static PrintStream ps;
  private static FileOutputStream fos;

  static {
    try {
      file = createErrFile();
      fos = new FileOutputStream(file);
      ps = new PrintStream(fos);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void setErrToFile() {
    System.err.println(">>> Start logging error to file err.txt");
    System.setErr(ps);
  }

  public static void EndErrorLogToFile() {
    System.setErr(console);
    System.err.println(">>> Start Print error to console");
  }

  public static void close() throws IOException {
    console.close();
    fos.close();
    ps.close();
  }

  private static File createErrFile() throws IOException {
    File file = new File("err.txt");
    if (file.exists()) file.delete();
    file.createNewFile();

    return file;
  }



}
