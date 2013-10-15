package bots.mctsbot.ai.opponentmodels.weka;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import weka.classifiers.Classifier;
import weka.classifiers.trees.M5P;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import bots.mctsbot.ai.opponentmodels.weka.instances.InstancesBuilder;

public class ARFFFile {

	private final String nl = InstancesBuilder.nl;
	private final File path;

	private final String name;

	private File arffFile;

	private Writer file;
	private long count = 0;
	private WekaOptions config;

	private Instances instances;
	private ArrayList<Prediction> predictions;
	private M5P cl = null;

	private boolean echo = false;

	public ARFFFile(String path, Object player, String name, String attributes, WekaOptions config) throws Exception {
		//		if (name.equals("PreFoldCallRaise.arff")) echo = true;
		String playerPath = player.toString().replace("\\", "").replace("/", "");
		this.path = new File(path, playerPath);
		this.name = name;

		this.config = config;

		// TODO: false => !config.arffOverwrite()
		arffFile = new File(this.path, "/arff/" + name);
		if (!arffFile.getParentFile().exists()) {
			arffFile.getParentFile().mkdirs();
		}

		file = new BufferedWriter(new FileWriter(arffFile, false));
		file.write(attributes);
		file.flush();

		DataSource source = new DataSource(new FileInputStream(arffFile));
		instances = source.getDataSet();
		// make it clean
		instances.delete();

		predictions = new ArrayList<Prediction>();

		// initiate accuracies
		for (int i = 0; i < MAX_DECREASE; i++) {
			accuracies[i] = -1;
		}
	}

	//	private double countDataLines() {
	//		InputStream is;
	//		try {
	//			is = new BufferedInputStream(new FileInputStream(path + player + name));
	//			byte[] c = new byte[1024];
	//			int count = 0;
	//			int readChars = 0;
	//			boolean startReading = false;
	//			while ((readChars = is.read(c)) != -1) {
	//				for (int i = 0; i < readChars; ++i) {
	//					if (c[i] == '\n' && startReading)
	//						++count;
	//					else if (!startReading && i >= 4 && c[i - 4] == '@'
	//							&& c[i - 3] == 'd' && c[i - 2] == 'a'
	//							&& c[i - 1] == 't' && c[i] == 'a')
	//						startReading = true;
	//				}
	//			}
	//			is.close();
	//			return count + (count > 0 ? -1 : 0);
	//		} catch (FileNotFoundException e) {
	//			e.printStackTrace();
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//		return 0;
	//	}
	//
	//	private boolean fileExists() throws FileNotFoundException {
	//		return new File(path + player + name).exists();
	//	}

	public void close() throws IOException {
		file.close();
	}

	public void write(Instance instance) {
		//		System.out.println("Writing instance " + (count +1) + " in file " + name);
		try {
			count++;
			file.write(instance.toString() + nl);
			file.flush();
			instances.add(instance);
			adjustWindow();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public void addPrediction(Prediction p) {
		//		if (echo) System.out.println("Adding " + p);
		for (int i = 0; i < instances.numInstances() - predictions.size() - 1; i++)
			predictions.add(null);
		predictions.add(p);
	}

	public double getWindowSize() {
		return instances.numInstances();
	}

	public double getAccuracy() {
		if (predictions.isEmpty())
			return 0.0;
		double truePositive = 0.0;
		double trueNegative = 0.0;
		double falsePositive = 0.0;
		double falseNegative = 0.0;
		for (int i = 0; i < predictions.size(); i++) {
			Prediction p = predictions.get(i);
			if (p != null) {
				truePositive += p.getTruePositive();
				trueNegative += p.getTrueNegative();
				falsePositive += p.getFalsePositive();
				falseNegative += p.getFalseNegative();
			}
		}
		return (trueNegative + truePositive) / (trueNegative + truePositive + falseNegative + falsePositive);
	}

	private final int MAX_DECREASE = 20;
	private double[] accuracies = new double[MAX_DECREASE];
	private int currentDecrease = 0;

	private boolean decreasingAcc(double accuracy) {
		currentDecrease++;
		if (currentDecrease > MAX_DECREASE) {
			for (int i = 0; i < MAX_DECREASE - 1; i++) {
				accuracies[i] = accuracies[i + 1];
			}
			accuracies[MAX_DECREASE - 1] = accuracy;
		} else
			accuracies[currentDecrease - 1] = accuracy;

		double slope = calculateLeastSquaresSlope(accuracies);

		return (slope < 0);
	}

	private double calculateLeastSquaresSlope(double[] accuracies) {
		double n = accuracies.length;
		double sumY = 0.0;
		double sumX = 0.0;
		double sumXY = 0.0;
		double sumX2 = 0.0;
		for (int i = 0; i < accuracies.length; i++) {
			if (echo)
				System.out.print(accuracies[i] + ", ");
			if (accuracies[i] != -1) {
				sumY += accuracies[i];
				sumX += i;
				sumXY += i * accuracies[i];
				sumX2 += i * i;
			}
		}

		double slope = ((n * sumXY) - (sumX * sumY)) / ((n * sumX2) - (sumX * sumX));
		double intercept = (sumY - (sumX * slope)) / n;
		if (echo)
			System.out.print("slope: " + slope + ", intercept: " + intercept);
		if (echo)
			System.out.println("");

		return slope;
	}

	private boolean printed = false;

	private void adjustWindow() {
		if (cl == null)
			return;
		double windowSize = instances.numInstances();
		double coverage = windowSize / cl.measureNumRules();
		double accuracy = getAccuracy();
		boolean decreasing = decreasingAcc(accuracy);
		double l;
		if ((coverage < config.getCdLowCoverage()) || (accuracy < config.getCdAccuracy() && decreasing))
			l = Math.round(0.2 * windowSize);
		else if (coverage > 2 * config.getCdHighCoverage() && accuracy > config.getCdAccuracy())
			l = 2;
		else if (coverage > config.getCdHighCoverage() && accuracy > config.getCdAccuracy())
			l = 1;
		else
			l = 0;

		if (echo && !printed) {
			System.out.println("L \t Accuracy \t Coverage \t Instances \t Decreasing");
			printed = true;
		}

		if (echo)
			System.out.println(l + "\t" + accuracy + "\t" + coverage + "\t" + windowSize + "\t" + decreasing);

		for (int i = 0; i < l; i++) {
			instances.delete(0);
			if (!predictions.isEmpty())
				predictions.remove(0);
		}

		//		windowSize = windowSize - l;
		//		System.out.println(name + ", " + windowSize + ", l: " + l + ", acc: " + accuracy + ", coverage: " + coverage);
	}

	public boolean isModelReady() {
		return count > config.getMinimalLearnExamples();
	}

	public long getNrExamples() {
		return count;
	}

	public String getName() {
		return name;
	}

	public Classifier createModel(String fileName, String attribute, String[] rmAttributes) throws Exception {
		//		System.out.println("Creating model for " + player + name);
		Instances data;
		if (config.solveConceptDrift())
			data = instances;
		else {
			DataSource source = new DataSource(new FileInputStream(arffFile));
			data = source.getDataSet();
		}
		if (rmAttributes.length > 0) {
			String[] optionsDel = new String[2];
			optionsDel[0] = "-R";
			optionsDel[1] = "";
			for (int i = 0; i < rmAttributes.length; i++)
				optionsDel[1] += (1 + data.attribute(rmAttributes[i]).index()) + ",";
			Remove remove = new Remove();
			remove.setOptions(optionsDel);
			remove.setInputFormat(data);
			data = Filter.useFilter(data, remove);
		}
		// setting class attribute if the data format does not provide this information
		// E.g., the XRFF format saves the class attribute information as well
		if (data.classIndex() == -1)
			data.setClass(data.attribute(attribute));

		// train M5P
		cl = new M5P();
		cl.setBuildRegressionTree(true);
		cl.setUnpruned(false);
		cl.setUseUnsmoothed(false);
		// further options...
		cl.buildClassifier(data);

		//	    System.out.println("Number of instances: " + data.numInstances());
		//	    System.out.println("Number of measures: " + cl.measureNumRules());
		//	    System.out.println(cl);

		// save model + header
		// save model + header
		if (config.modelPersistency()) {
			File modelFile = new File(this.path, "/model/" + fileName + ".model");
			if (!modelFile.getParentFile().exists()) {
				modelFile.getParentFile().mkdirs();
			}

			SerializationHelper.write(new FileOutputStream(modelFile), cl);
		}

		return cl;
	}
}
