package simulator.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONObject;
import org.json.JSONTokener;

import simulator.control.Controller;
import simulator.factories.BuilderBasedFactory;
import simulator.factories.Builder;
import simulator.factories.DefaultRegionBuilder;
import simulator.factories.DynamicSupplyRegionBuilder;
import simulator.factories.Factory;
import simulator.factories.SelectClosestBuilder;
import simulator.factories.SelectFirstBuilder;
import simulator.factories.SelectYoungestBuilder;
import simulator.factories.SheepBuilder;
import simulator.factories.WolfBuilder;
import simulator.misc.Utils;
import simulator.model.Animal;
import simulator.model.Region;
import simulator.model.SelectionStrategy;
import simulator.model.Simulator;

public class Main {

  private enum ExecMode {
    BATCH("batch", "Batch mode"), GUI("gui", "Graphical User Interface mode");

    private String tag;
    private String desc;

    private ExecMode(String modeTag, String modeDesc) {
      tag = modeTag;
      desc = modeDesc;
    }

    public String getTag() {
      return tag;
    }

    public String getDesc() {
      return desc;
    }
  }

  // default values for some parameters
  //
  private final static Double DEFAULT_TIME = 10.0; // in seconds
  private final static Double DEFAULT_DELTA_TIME = 0.03;

  // some attributes to stores values corresponding to command-line parameters
  //
  private static Double time = null;
  private static Double dt = null;
  private static String inFile = null;
  private static String outFile = null;
  private static boolean sv = false;
  private static ExecMode mode = ExecMode.BATCH;

  // factories
  private static Factory<SelectionStrategy> selectionStrategyFactory;
  private static Factory<Animal> animalsFactory;
  private static Factory<Region> regionsFactory;

  private static void parseArgs(String[] args) {

    // define the valid command line options
    //
    Options cmdLineOptions = buildOptions();

    // parse the command line as provided in args
    //
    CommandLineParser parser = new DefaultParser();
    try {
      CommandLine line = parser.parse(cmdLineOptions, args);
      parseHelpOption(line, cmdLineOptions);
      parseInFileOption(line);
      // NEW
      parseOutFileOption(line);
      parseDeltaTimeOption(line);
      parseSVOption(line);
      // END NEW
      parseTimeOption(line);

      // if there are some remaining arguments, then something wrong is
      // provided in the command line!
      //
      String[] remaining = line.getArgs();
      if (remaining.length > 0) {
        String error = "Illegal arguments:";
        for (String o : remaining)
          error += (" " + o);
        throw new ParseException(error);
      }

    } catch (ParseException e) {
      System.err.println(e.getLocalizedMessage());
      System.exit(1);
    }

  }

  private static Options buildOptions() {
    Options cmdLineOptions = new Options();

    // help
    cmdLineOptions.addOption(Option.builder("h").longOpt("help").desc("Print this message.").build());

    // input file
    cmdLineOptions.addOption(Option.builder("i").longOpt("input").hasArg().desc("Initial configuration file.").build());

    // output file -- NEW
    cmdLineOptions.addOption(Option.builder("o").longOpt("output").hasArg().desc("Output file, where output is written.").build());

    // steps
    cmdLineOptions.addOption(Option.builder("t").longOpt("time").hasArg()
      .desc("An real number representing the total simulation time in seconds. Default value: "
        + DEFAULT_TIME + ".")
      .build());

    // delta time -- NEW
    cmdLineOptions.addOption(Option.builder("dt").longOpt("delta-time").hasArg()
      .desc("A double representing actual time, in seconds, per simulation step. Default value: "
        + DEFAULT_DELTA_TIME + ".")
      .build());

    // simple viewer -- NEW
    cmdLineOptions.addOption(Option.builder("sv").longOpt("simple-viewer").desc("Show the viewer window in console mode.").build());

    return cmdLineOptions;
  }

  private static void parseHelpOption(CommandLine line, Options cmdLineOptions) {
    if (line.hasOption("h")) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(Main.class.getCanonicalName(), cmdLineOptions, true);
      System.exit(0);
    }
  }

  private static void parseInFileOption(CommandLine line) throws ParseException {
    inFile = line.getOptionValue("i");
    if (mode == ExecMode.BATCH && inFile == null) {
      throw new ParseException("In batch mode an input configuration file is required");
    }
  }

  // NEW
  private static void parseOutFileOption(CommandLine line) {
    outFile = line.getOptionValue("o");
  }

  private static void parseDeltaTimeOption(CommandLine line) throws ParseException {// parsea el delta-time, si no se especifica se utiliza el valor por defecto, si se especifica pero no es un número válido o es negativo se lanza una excepción
    String d = line.getOptionValue("dt", DEFAULT_DELTA_TIME.toString()); 
    try {
      dt = Double.parseDouble(d);
      assert (dt > 0);
    } catch (Exception e) {
      throw new ParseException("Invalid value for delta-time: " + d);
    }
  }

  private static void parseSVOption(CommandLine line) {
    sv = line.hasOption("sv");
  }
  

  private static void parseTimeOption(CommandLine line) throws ParseException {
    String t = line.getOptionValue("t", DEFAULT_TIME.toString());
    try {
      time = Double.parseDouble(t);
      assert (time >= 0);
    } catch (Exception e) {
      throw new ParseException("Invalid value for time: " + t);
    }
  }

  private static void initFactories() {
    // selection strategies factory
    List<Builder<SelectionStrategy>> ssBuilders = new ArrayList<>();
    ssBuilders.add(new SelectFirstBuilder());
    ssBuilders.add(new SelectClosestBuilder());
    ssBuilders.add(new SelectYoungestBuilder());
    selectionStrategyFactory = new BuilderBasedFactory<>(ssBuilders);

    // animals factory
    List<Builder<Animal>> animalBuilders = new ArrayList<>();
    animalBuilders.add(new SheepBuilder(selectionStrategyFactory));
    animalBuilders.add(new WolfBuilder(selectionStrategyFactory));
    animalsFactory = new BuilderBasedFactory<>(animalBuilders);

    // regions factory
    List<Builder<Region>> regionBuilders = new ArrayList<>();
    regionBuilders.add(new DefaultRegionBuilder());
    regionBuilders.add(new DynamicSupplyRegionBuilder());
    regionsFactory = new BuilderBasedFactory<>(regionBuilders);
  }

  private static JSONObject loadJSONFile(InputStream in) {
    return new JSONObject(new JSONTokener(in));
  }

  private static void start_batch_mode() throws Exception {
    // 1. Carga input JSON
    InputStream is = new FileInputStream(new File(inFile));
    JSONObject inputData = loadJSONFile(is);
    is.close();

    // 2. Crea output 
    OutputStream os = outFile != null ? new FileOutputStream(new File(outFile)) : System.out;

    // 3. Crea Simulator
    Simulator sim = new Simulator(
      inputData.getInt("cols"),
      inputData.getInt("rows"),
      inputData.getInt("width"),
      inputData.getInt("height"),
      animalsFactory,
      regionsFactory
    );

    // 4. Crea Controller
    Controller ctrl = new Controller(sim);

    // 5. Carga datos
    ctrl.loadData(inputData);

    // 6. Run
    ctrl.run(time, dt, sv, os);

    // 7. Cierra output
    if (outFile != null) os.close();
  }

  private static void start_GUI_mode() throws Exception {
    throw new UnsupportedOperationException("GUI mode is not ready yet ...");
  }

  private static void start(String[] args) throws Exception {
    initFactories();
    parseArgs(args);
    switch (mode) {
      case BATCH:
        start_batch_mode();
        break;
      case GUI:
        start_GUI_mode();
        break;
    }
  }

  public static void main(String[] args) {
    Utils.RAND.setSeed(2147483647l);
    try {
      start(args);
    } catch (Exception e) {
      System.err.println("Something went wrong ...");
      System.err.println();
      e.printStackTrace();
    }
  }
}