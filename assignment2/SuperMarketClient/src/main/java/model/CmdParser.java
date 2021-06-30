package model;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CmdParser {

  public static CommandLine parseCmd(String[] args) {

    // setting up options for command line input
    Options options = new Options();
    Option ipOpt = new Option("ip", "ipAddress", true,
        "ip and port address of the server");
    ipOpt.setRequired(true);
    options.addOption(ipOpt);

    Option maxStoreOpt = new Option("s", "maxStore", true, "setting up max stores");
    maxStoreOpt.setRequired(true);
    options.addOption(maxStoreOpt);

    Option custPerStoreOpt = new Option("ct", "numOfCustomersPerStore", true,
        "setting up number of customers per store");
    custPerStoreOpt.setOptionalArg(true);
    options.addOption(custPerStoreOpt);

    Option maxItemOpt = new Option("it", "maxItem", true,
        "setting up the max item id");
    maxItemOpt.setOptionalArg(true);
    options.addOption(maxItemOpt);

    Option numPurchasesOpt = new Option("p", "numPurchasesPerHour", true,
        "setting up number of purchase per hour");
    numPurchasesOpt.setOptionalArg(true);
    options.addOption(numPurchasesOpt);

    Option itPerPurchaseOpt = new Option("n", "numItemsPerPurchase", true,
        "setting up number of items per purchase");
    itPerPurchaseOpt.setOptionalArg(true);
    options.addOption(itPerPurchaseOpt);

    Option dateOpt = new Option("d", "date", true,
        "setting up start date");
    dateOpt.setOptionalArg(true);
    options.addOption(dateOpt);

    Option csvOpt = new Option("o", "csv", true,
        "setting up the csv output path");
    csvOpt.setOptionalArg(true);
    options.addOption(csvOpt);

    // parse input from args
    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp("utility-name", options);
      System.exit(1);
    }
    return  cmd;
  }


}
