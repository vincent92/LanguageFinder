package nl.hu.hadoop.languagefinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Dictionary {

	public static final String[] ALPHABET = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };
	public static final int ALPHABETSIZE = 26;

	public ArrayList<LetterRow> afterRows = new ArrayList<LetterRow>();
	public ArrayList<LetterRow> beforeRows = new ArrayList<LetterRow>();

	public Dictionary(String fileName) {
		try {
			createDictionary(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void createDictionary(String fileName) throws FileNotFoundException {

		File file = new File(fileName);
		boolean firstRow = true;

		Scanner rowScanner = new Scanner(file);
		Scanner columnScanner;

		while (rowScanner.hasNextLine()) {

			String row = rowScanner.nextLine();
			columnScanner = new Scanner(row);

			// initialize rows based on the alphabet
			if (firstRow) {
				firstRow = false;
				initializeRows(columnScanner);
			}
			// check if not final row
			else if (!row.contains("_")) {

				// set frequency
				String key = columnScanner.next();
				initializeColumnFrequency(columnScanner, key);

				// set total after
				String total = columnScanner.next();
				initializeTotalAfterRow(key, total);

			} else {

				// set total before
				columnScanner.close();
				columnScanner = new Scanner(rowScanner.nextLine());
				initializeTotalBeforeRows(columnScanner);
			}
			columnScanner.close();
		}
		rowScanner.close();
	}

	private void initializeRows(Scanner columnScanner) {
		while (columnScanner.hasNext()) {
			String letter = columnScanner.next();
			createAfterRow(letter);
			createBeforeRows(letter);
		}
	}

	private void createAfterRow(String letter) {
		afterRows.add(new LetterRow(letter));
	}

	private void createBeforeRows(String letter) {
		beforeRows.add(new LetterRow(letter));
	}

	private void initializeColumnFrequency(Scanner columnScanner, String key) {

		for (int i = 0; i < ALPHABETSIZE; i++) {

			String value = columnScanner.next();
			int frequency = Integer.parseInt(value);

			// set frequency
			getAfterLetterRow(key).columns.get(i).frequency = frequency;
			beforeRows.get(i).getColumn(key).frequency = frequency;
		}
	}

	private void initializeTotalAfterRow(String key, String total) {
		getAfterLetterRow(key).total = Integer.parseInt(total.substring(1));
	}

	private void initializeTotalBeforeRows(Scanner columnScanner) {
		boolean skipFirst = true;

		for (int i = 0; i < ALPHABETSIZE; i++) {

			if (skipFirst) {
				skipFirst = false;
				columnScanner.next();
			}
			String value = columnScanner.next();
			beforeRows.get(i).total = Integer.parseInt(value);
		}
	}

	public LetterRow getAfterLetterRow(String key) {
		LetterRow row = null;
		for (LetterRow r : afterRows) {
			if (r.key.equals(key)) {
				row = r;
				break;
			}
		}
		return row;
	}

	public LetterRow getBeforeLetterRow(String key) {
		LetterRow row = null;
		for (LetterRow r : beforeRows) {
			if (r.key.equals(key)) {
				row = r;
				break;
			}
		}
		return row;
	}

	class LetterRow {

		public ArrayList<LetterColumn> columns = new ArrayList<LetterColumn>();

		public String key;
		public int total;

		public LetterRow(String letter) {
			setKey(letter);
			createColumns();
		}

		private void createColumns() {
			for (String letter : ALPHABET) {
				columns.add(new LetterColumn(letter));
			}
		}

		public LetterColumn getColumn(String key) {
			LetterColumn column = null;
			for (LetterColumn c : columns) {
				if (c.key.equals(key)) {
					column = c;
					break;
				}
			}
			return column;
		}

		private void setKey(String letter) {
			key = letter;
		}

	}

	class LetterColumn {

		public String key;
		public int frequency;

		public LetterColumn(String letter) {
			key = letter;
		}
	}
}
