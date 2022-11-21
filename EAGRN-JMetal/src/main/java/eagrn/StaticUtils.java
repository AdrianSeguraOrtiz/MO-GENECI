package eagrn;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.io.FileWriter;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.uma.jmetal.solution.doublesolution.DoubleSolution;

import eagrn.cutoffcriteria.CutOffCriteria;
import eagrn.cutoffcriteria.impl.NumLinksWithBestConfCriteria;
import eagrn.cutoffcriteria.impl.PercLinksWithBestConfCriteria;
import eagrn.fitnessfunction.FitnessFunction;
import eagrn.fitnessfunction.impl.loyalty.impl.LoyaltyFinal;
import eagrn.fitnessfunction.impl.loyalty.impl.LoyaltyProgressiveCurrentImpact;
import eagrn.fitnessfunction.impl.loyalty.impl.LoyaltyProgressiveNextImpact;
import eagrn.fitnessfunction.impl.loyalty.impl.LoyaltyProgressiveNextNextImpact;
import eagrn.fitnessfunction.impl.quality.impl.QualityMean;
import eagrn.fitnessfunction.impl.quality.impl.QualityMeanAboveAverage;
import eagrn.fitnessfunction.impl.quality.impl.QualityMeanAboveAverageWithContrast;
import eagrn.fitnessfunction.impl.quality.impl.QualityMeanAboveCutOff;
import eagrn.fitnessfunction.impl.quality.impl.QualityMedian;
import eagrn.fitnessfunction.impl.quality.impl.QualityMedianAboveAverage;
import eagrn.fitnessfunction.impl.quality.impl.QualityMedianAboveAverageWithContrast;
import eagrn.fitnessfunction.impl.quality.impl.QualityMedianAboveCutOff;
import eagrn.fitnessfunction.impl.topology.impl.AverageLocalClusteringMeasure;
import eagrn.fitnessfunction.impl.topology.impl.BetweennessDistribution;
import eagrn.fitnessfunction.impl.topology.impl.BinarizedDegreeDistribution;
import eagrn.fitnessfunction.impl.topology.impl.ClosenessDistribution;
import eagrn.fitnessfunction.impl.topology.impl.EdgeBetweennessDistribution;
import eagrn.fitnessfunction.impl.topology.impl.EigenvectorDistribution;
import eagrn.fitnessfunction.impl.topology.impl.GlobalClusteringMeasure;
import eagrn.fitnessfunction.impl.topology.impl.KatzDistribution;
import eagrn.fitnessfunction.impl.topology.impl.PageRankDistribution;
import eagrn.fitnessfunction.impl.topology.impl.WeightedDegreeDistribution;
import eagrn.cutoffcriteria.impl.MinConfCriteria;

public final class StaticUtils {
    public static Map<String, Double> getMapWithLinks(File listFile) {
        /**
         * This function takes as input the file with the list of links and 
         * their respective confidence levels, and returns a map containing 
         * all information
         */
        Map<String, Double> map = new HashMap<String, Double>();

        try {
            Scanner sc = new Scanner(listFile);
            while(sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] splitLine = line.split(",");

                String key = splitLine[0] + ";" + splitLine[1];
                Double value = Double.parseDouble(splitLine[2]);
                map.put(key, value);
            }
            sc.close();
        } catch (FileNotFoundException fnfe) {
            throw new RuntimeException(fnfe.getMessage());
        }

        return map;
    }

    public static File[] getCSVFilesFromDirectory(String directory) {
        /**
         * This function takes as input a directory and returns the 
         * list of CSV files contained in it
         */
        File dir = new File(directory);
        FileFilter fileFilter = new WildcardFileFilter("*.csv");
        return dir.listFiles(fileFilter);
    }

    public static ArrayList<String> getGeneNames(String strFile) {
        /**
         * This function receives the path to the file containing the 
         * list of gene names and returns a vector with all of them.
         */
        ArrayList<String> geneNames;

        File geneNamesFile = new File(strFile);
        try {
            Scanner sc = new Scanner(geneNamesFile);
            String line = sc.nextLine();
            String[] lineSplit = line.split(",");
            geneNames = new ArrayList<>(List.of(lineSplit));
            sc.close();
        } catch (FileNotFoundException fnfe) {
            throw new RuntimeException(fnfe.getMessage());
        }

        return geneNames;
    }

    public static CutOffCriteria getCutOffCriteriaFromString(String strCutOffCriteria, double cutOffValue, ArrayList<String> geneNames) {
        /**
         * This function takes as input a character string representing a 
         * cut-off criteria and returns the object corresponding to it
         */
        CutOffCriteria cutOffCriteria;

        switch (strCutOffCriteria) {
            case "MinConf":
                cutOffCriteria = new MinConfCriteria(cutOffValue, geneNames);
                break;
            case "NumLinksWithBestConf":
                cutOffCriteria = new NumLinksWithBestConfCriteria((int) cutOffValue, geneNames);
                break;
            case "PercLinksWithBestConf":
                cutOffCriteria = new PercLinksWithBestConfCriteria(cutOffValue, geneNames);
                break;
            default:
                throw new RuntimeException("The cut-off criteria entered is not available");
        }

        return cutOffCriteria;
    }

    public static void writeFitnessEvolution(String strFile, Map<String, Double[]> fitnessEvolution) {
        /**
         * This function is responsible for writing the evolution of the 
         * fitness values in an output txt file specified as parameter
         */
        try {
            File outputFile = new File(strFile);
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));

            for (Map.Entry<String, Double[]> entry : fitnessEvolution.entrySet()) {
                String strVector = Arrays.toString(entry.getValue());
                bw.write(strVector.substring(1, strVector.length() - 1) + "\n");
            }

            bw.flush();
            bw.close();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static void writeConsensus(String strFile, Map<String, Double> consensus) {
        /**
         * This function is responsible for writing the consensus list of 
         * a solution to an output csv file specified as a parameter.
         */
        try {
            File outputFile = new File(strFile);
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));

            for (Map.Entry<String, Double> pair : consensus.entrySet()) {
                String [] vKeySplit = pair.getKey().split(";");
                bw.write(vKeySplit[0] + "," + vKeySplit[1] + "," + pair.getValue());
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe.getMessage());
        }
    }

    public static void writeBinaryNetwork(String strFile, int[][] binaryNetwork, ArrayList<String> geneNames) {
        /**
         * This function is responsible for writing the binary network 
         * of a solution to an output csv file specified as a parameter.
         */
        try {
            File outputFile = new File(strFile);
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));

            bw.write("," + String.join(",", geneNames));
            bw.newLine();
            for (int i = 0; i < binaryNetwork.length; i++) {
                bw.write(geneNames.get(i) + ",");
                for (int j = 0; j < binaryNetwork[i].length; j++) {
                    bw.write(binaryNetwork[i][j] + ((j == binaryNetwork[i].length - 1) ? "" : ","));
                }
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe.getMessage());
        }
    }

    public static Map<String, Double> makeConsensus(Double[] weights, Map<String, Double[]> inferredNetworks) {
        /**
         * This function calculates the weighted sum of 
         * confidence levels based on the weights.
         */

        Map<String, Double> consensus = new HashMap<>();

        for (Map.Entry<String, Double[]> pair : inferredNetworks.entrySet()) {
            double confidence = 0.0;

            for (int i = 0; i < weights.length; i++) {
                confidence += weights[i] * pair.getValue()[i];
            }

            consensus.put(pair.getKey(), confidence);
        }

        return consensus;
    }

    public static Map<String, Double[]> readAllInferredNetworkFiles(File[] inferredNetworkFiles) {
        /**
         * It scans the lists of links offered by the different techniques and stores them in a map 
         * with vector values for later query during the construction of the consensus network.
         */

        Map<String, Double[]> res = new HashMap<String, Double[]>();
        Double[] initialValue = new Double[inferredNetworkFiles.length];
        Arrays.fill(initialValue, 0.0);

        for (int i = 0; i < inferredNetworkFiles.length; i++) {
            Map<String, Double> map = StaticUtils.getMapWithLinks(inferredNetworkFiles[i]);

            for (Map.Entry<String, Double> entry : map.entrySet()) {
                Double[] value = res.getOrDefault(entry.getKey(), initialValue.clone());
                value[i] = entry.getValue();
                res.put(entry.getKey(), value);
            }
        }

        return res;
    }

    public static Map<String, Double[]> readTimeSeries(String strTimeSeriesFile) {
        /**
         * It reads the file with the input time series
         */

        Map<String, Double[]> res = new HashMap<String, Double[]>();

        try {
            File timesSeriesFile = new File(strTimeSeriesFile);
            Scanner sc = new Scanner(timesSeriesFile);
            sc.nextLine();
            while(sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] splitLine = line.split(",");

                String gene = splitLine[0].replace("\"", "");
                Double[] array = new Double[splitLine.length - 1];
                for (int i = 1; i < splitLine.length; i++) {
                    array[i-1] = Double.parseDouble(splitLine[i]);
                }
                res.put(gene, array);
            }
            sc.close();
        } catch (FileNotFoundException fnfe) {
            throw new RuntimeException(fnfe.getMessage());
        }

        return res;
    }

    public static FitnessFunction getBasicFitnessFunction(String str, ArrayList<String> geneNames, Map<String, Double[]> inferredNetworks, CutOffCriteria cutOffCriteria, Map<String, Double[]> timeSeriesMap) {
        /** 
         * Function to return a basic FitnessFunction object based on a identifier string 
         */
        
        FitnessFunction res;
        switch (str.toLowerCase()) {
            case "loyaltyprogressivecurrentimpact":
                res = new LoyaltyProgressiveCurrentImpact(timeSeriesMap);
                break;
            case "loyaltyprogressivenextimpact":
                res = new LoyaltyProgressiveNextImpact(timeSeriesMap);
                break;
            case "loyaltyprogressivenextnextimpact":
                res = new LoyaltyProgressiveNextNextImpact(timeSeriesMap);
                break;
            case "loyaltyfinal":
                res = new LoyaltyFinal(timeSeriesMap);
                break;
            case "qualitymean":
                res = new QualityMean(inferredNetworks);
                break;
            case "qualitymedian":
                res = new QualityMedian(inferredNetworks);
                break;
            case "qualitymeanaboveaverage":
                res = new QualityMeanAboveAverage(inferredNetworks);
                break;
            case "qualitymedianaboveaverage":
                res = new QualityMedianAboveAverage(inferredNetworks);
                break;
            case "qualitymeanabovecutoff":
                res = new QualityMeanAboveCutOff(inferredNetworks, cutOffCriteria);
                break;
            case "qualitymedianabovecutoff":
                res = new QualityMedianAboveCutOff(inferredNetworks, cutOffCriteria);
                break;
            case "qualitymeanaboveaveragewithcontrast":
                res = new QualityMeanAboveAverageWithContrast(geneNames.size(), inferredNetworks);
                break;
            case "qualitymedianaboveaveragewithcontrast":
                res = new QualityMedianAboveAverageWithContrast(geneNames.size(), inferredNetworks);
                break;
            case "averagelocalclusteringmeasure":
                res = new AverageLocalClusteringMeasure(geneNames, cutOffCriteria);
                break;
            case "globalclusteringmeasure":
                res = new GlobalClusteringMeasure(geneNames, cutOffCriteria);
                break;
            case "binarizeddegreedistribution":
                res = new BinarizedDegreeDistribution(geneNames.size(), cutOffCriteria);
                break;
            case "weighteddegreedistribution":
                res = new WeightedDegreeDistribution(geneNames);
                break;
            case "betweennessdistribution":
                res = new BetweennessDistribution(geneNames);
                break;
            case "closenessdistribution":
                res = new ClosenessDistribution(geneNames);
                break;
            case "edgebetweennessdistribution":
                res = new EdgeBetweennessDistribution(geneNames);
                break;
            case "eigenvectordistribution":
                res = new EigenvectorDistribution(geneNames);
                break;
            case "katzdistribution":
                res = new KatzDistribution(geneNames);
                break;
            case "pagerankdistribution":
                res = new PageRankDistribution(geneNames);
                break;
            default:
                throw new RuntimeException("The evaluation term " + str + " is not implemented.");
        }
        return res;
    }

    public static FitnessFunction getCompositeFitnessFunction(String formula, ArrayList<String> geneNames, Map<String, Double[]> inferredNetworks, CutOffCriteria cutOffCriteria, Map<String, Double[]> timeSeriesMap) {
        /**
         * Function to return a composite FitnessFunction object based on a formula string
         */
        
        FitnessFunction function;

        String[] subformulas = formula.split("\\+");
        if (subformulas.length == 1 && subformulas[0].split("\\*").length == 1) {
            function = getBasicFitnessFunction(formula, geneNames, inferredNetworks, cutOffCriteria, timeSeriesMap);
        } else {
            FitnessFunction[] functions = new FitnessFunction[subformulas.length];
            Double[] weights = new Double[subformulas.length];
            double totalWeight = 0;

            for (int j = 0; j < subformulas.length; j++) {
                String[] tuple = subformulas[j].split("\\*");
                if (tuple.length != 2) {
                    throw new RuntimeException("Function specified with improper formatting. Remember to separate the name of the terms by the symbol +, and assign their weight by preceding them with a decimal followed by the symbol *.");
                }

                functions[j] = getBasicFitnessFunction(tuple[1], geneNames, inferredNetworks, cutOffCriteria, timeSeriesMap);
                try {
                    weights[j] = Double.parseDouble(tuple[0]);
                    totalWeight += weights[j];
                } catch (Exception e) {
                    throw new RuntimeException("The weight " + tuple[0] + " assigned to term " + tuple[1] + " is invalid.");
                }
            }

            if (Math.abs(totalWeight - 1.0) > 0.01) {
                throw new RuntimeException("The weights of all the terms in the formula must add up to 1. Currently total " + totalWeight);
            }

            function = (Map<String, Double> consensus, Double[] x) -> {
                double res = 0;
                for (int j = 0; j < functions.length; j++) {
                    res += weights[j] * functions[j].run(consensus, x);
                }
                return res;
            };
        }

        return function;
    }

    public static double[] standardize(double[] x) {
        /**
         * this function takes care of standardising 
         * vectors between 0 and 1.
         */

        double[] res = new double[x.length];

        double sum = 0;
        for (int i = 0; i < x.length; i++) {
            sum += x[i];
        }

        for (int i = 0; i < x.length; i++) {
            res[i] = x[i]/sum;
        }

        return res;
    }

    public static void standardizeInitialSolution(DoubleSolution solution) {
        /**
         * this function takes care of standardising 
         * initial solutions between 0 and 1.
         */

        double v, sum = 0;

        for (int i = 0; i < solution.variables().size(); i++) {
            v = solution.variables().get(i);
            sum += v;
        }

        for (int i = 0; i < solution.variables().size(); i++) {
            v = solution.variables().get(i);
            v = Math.round(v/sum * 10000.0) / 10000.0;
            solution.variables().set(i, v);
        }
    }
}
