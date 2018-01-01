package com.example.wordCount
import org.apache.log4j.BasicConfigurator
import org.apache.log4j.Logger
import org.apache.commons.cli.Options
import org.apache.commons.cli.PosixParser
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.ParseException
import org.apache.commons.cli.HelpFormatter
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

object RunWordCountJob {
  val logger: Logger = Logger.getLogger(RunWordCountJob.getClass)
  def main(args: Array[String]) {

    BasicConfigurator.configure()
    val options = new Options()
    options.addOption("s", "single-node", false, "Speicify if program is in single-node environment")
    options.addOption("i", "input-file", true, "Path to the input file")
    options.addOption("o", "output-dir", true, "Path to the output diriectory")

    val cmdParser = new PosixParser
    var cmdLine: CommandLine = null;

    try {
      cmdLine = cmdParser.parse(options, args)
    } catch {
      case ex: ParseException => {
        logger.fatal("Fail to parse command line", ex)
        printHelp(options)
        System.exit(1)
      }
    }

    if (!cmdLine.hasOption('i')) {
      logger.fatal("Input file not provided")
      printHelp(options)
      System.exit(1)
    }
    if (!cmdLine.hasOption('o')) {
      logger.fatal("output directory not provided")
      printHelp(options)
      System.exit(1)
    }

    val inputTextFile = cmdLine.getOptionValue('i')
    val outputDir = cmdLine.getOptionValue('o')

    logger.info("Input File: " + inputTextFile)
    logger.info("Output Dir: " + outputDir)

    val sparkConf = new SparkConf().setAppName("distinct-job")

    if (cmdLine.hasOption('s')) {
      sparkConf.setMaster("local[4]")
    }

    val sc = new SparkContext(sparkConf)
    sc.textFile(inputTextFile)
      .flatMap(line => line.split(" "))
      .map(word => (word.replaceAll("[^A-Za-z0-9]+", "") , 1))
      .reduceByKey(_+_)
      .sortBy(_._2,false)
      .saveAsTextFile(outputDir)
  }
  def printHelp(options: Options) = {
    val helpFormatter = new HelpFormatter
    helpFormatter.printHelp("distinct-job [options]", options)
  }
}