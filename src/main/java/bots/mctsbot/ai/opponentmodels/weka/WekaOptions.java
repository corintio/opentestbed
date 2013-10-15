package bots.mctsbot.ai.opponentmodels.weka;

public class WekaOptions {
	
	private boolean useOnlineLearning = true;
	/** Continuous learning will start after threshold */
	private boolean continuousLearning = true; 
	
	/** if continuousLearning is false, a new model will be learned
	 * after X actions by an opponent, where X is modelCreationTreshold	*/
	private long modelCreationTreshold = 500; 
	private long minimalLearnExamples = 1;
	
	/** continuousLearning must be true for using solveConceptDrift */
	private boolean solveConceptDrift = true;
	private double cdHighCoverage = 50;
	private double cdLowCoverage = 1;
	private double cdAccuracy = 0.8;
	/** if solveConceptDrift is false, a new model must be learned at intervals
	 * based on the number of reported actions	*/
	private long learningInterval = 1;
	
	// TODO: keep files of previous online learning
	private boolean arffOverwrite = true;
	/** only available when continuousLearning if false */
	private boolean continueAfterCreation = true; 

	private boolean modelPersistency = true;
	
	public boolean useOnlineLearning() {
		return useOnlineLearning;
	}

	public void setUseOnlineLearning(boolean useOnlineLearning) {
		this.useOnlineLearning = useOnlineLearning;
	}

	public boolean continuousLearning() {
		return continuousLearning;
	}

	public void setContinuousLearning(boolean continuousLearning) {
		this.continuousLearning = continuousLearning;
	}

	public long modelCreationTreshold() {
		return modelCreationTreshold;
	}

	public void setModelCreationTreshold(long modelCreationTreshold) {
		this.modelCreationTreshold = modelCreationTreshold;
	}

	public long getMinimalLearnExamples() {
		return minimalLearnExamples;
	}

	public void setMinimalLearnExamples(long minimalLearnExamples) {
		this.minimalLearnExamples = minimalLearnExamples;
	}

	public boolean solveConceptDrift() {
		return solveConceptDrift;
	}

	public void setSolveConceptDrift(boolean solveConceptDrift) {
		if (!continuousLearning && solveConceptDrift)
			throw new IllegalStateException("Cannot use concept drift solver without continuous learning!");
		this.solveConceptDrift = solveConceptDrift;
	}
	
	
	public long getLearningInterval() {
		return learningInterval;
	}

	public void setLearningInterval(long learningInterval) {
		this.learningInterval = learningInterval;
	}

	public boolean arffOverwrite() {
		return arffOverwrite;
	}

	public void setArffOverwrite(boolean arffOverwrite) {
		this.arffOverwrite = arffOverwrite;
	}

	public boolean continueAfterCreation() {
		return continueAfterCreation;
	}

	public void setContinueAfterCreation(boolean continueAfterCreation) {
		this.continueAfterCreation = continueAfterCreation;
	}

	public boolean modelPersistency() {
		return modelPersistency;
	}

	public void setModelPersistency(boolean modelPersistency) {
		this.modelPersistency = modelPersistency;
	}

	public double getCdHighCoverage() {
		return cdHighCoverage;
	}

	public void setCdHighCoverage(double cdHighCoverage) {
		this.cdHighCoverage = cdHighCoverage;
	}

	public double getCdLowCoverage() {
		return cdLowCoverage;
	}

	public void setCdLowCoverage(double cdLowCoverage) {
		this.cdLowCoverage = cdLowCoverage;
	}

	public double getCdAccuracy() {
		return cdAccuracy;
	}

	public void setCdAccuracy(double cdAccuracy) {
		this.cdAccuracy = cdAccuracy;
	}
}
