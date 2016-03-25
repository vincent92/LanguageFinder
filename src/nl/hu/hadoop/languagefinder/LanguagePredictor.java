package nl.hu.hadoop.languagefinder;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import nl.hu.hadoop.languagefinder.Dictionary.LetterRow;

public class LanguagePredictor {

	private static final int SECOND_CHAR = 1;
	private static final int FIRST_CHAR = 0;
	private Dictionary dict;

	public LanguagePredictor() {};
	
	public LanguagePredictor(Dictionary dictionary) {
		try {
			setDictionary(dictionary);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	};

	public void setDictionary(Dictionary dictionary) throws FileNotFoundException {
		dict = dictionary;
	}

	public double calculatePrediction(String word) {

		if (word.length() <= 1) return 0.0;
		ArrayList<Double> predictions = new ArrayList<Double>();
		
		for (int index = 0; index < word.length(); index++) {
			String[] consecutive = consecutiveSearch(word, index);
			if (consecutive != null) {
				predictions.add(calculatePredictionAfter(consecutive[FIRST_CHAR], consecutive[SECOND_CHAR]));
				predictions.add(calculatePredictionBefore(consecutive[FIRST_CHAR], consecutive[SECOND_CHAR]));
			}
		}

		double average = calculateAverage(predictions);
		return average;
	}

	private double calculateAverage(ArrayList<Double> predictions) {
		double sum = 0.0;
		for (double prediction : predictions) {
			sum += prediction;
		}
		return (sum / predictions.size());
	}

	private String[] consecutiveSearch(String word, int index) {
		String[] consecutive = null;
		String firstChar = String.valueOf(word.charAt(index));
		int secondIndex = (index + 1);
		if (secondIndex < word.length()) {
			String secondChar = String.valueOf(word.charAt(secondIndex));
			consecutive = new String[] { firstChar, secondChar };
		}
		return consecutive;
	}

	private double calculatePredictionAfter(String firstChar, String secondChar) {

		LetterRow row = dict.getAfterLetterRow(firstChar);
		int frequency = row.getColumn(secondChar).frequency;
		return getPercentage(row.total, frequency);
	}

	private double calculatePredictionBefore(String firstChar, String secondChar) {

		LetterRow row = dict.getBeforeLetterRow(firstChar);
		int frequency = row.getColumn(secondChar).frequency;
		return getPercentage(row.total, frequency);
	}

	private float getPercentage(int total, int obtained) {
		float proportionCorrect = ((float) obtained) / ((float) total);
		return proportionCorrect * 100;
	}
}