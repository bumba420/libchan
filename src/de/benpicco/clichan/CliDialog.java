package de.benpicco.clichan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CliDialog {
	private BufferedReader	in;

	public CliDialog() {
		in = new BufferedReader(new InputStreamReader(System.in));
	}

	public String ask(String question) {
		System.out.print(question + ": ");
		String answer = null;
		try {
			answer = in.readLine();
			if (answer.length() == 0)
				answer = null;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return answer;
	}

	public String ask(String question, String defaultAnswer) {
		String answer = ask(question + " [" + defaultAnswer + "]");
		if (answer == null)
			return defaultAnswer;
		return answer;
	}
}
