package edu.nyu.cs.cs2580;

import java.io.IOException;

/**
 * Main task manager.
 * @author Yubing & Qian
 */
public class MainDriver {

	/**
	 * @param args the specified input and output path
	 */
	public static void main(String[] args) {
		try {
			LinkGraphDriver.linkGraphmain(args[0]);
			PageRankDriver.pageRankmain();
			PageRankCleanupMapper.cleanupMain(args[1]);
		} catch (IOException e) {}
	}

}
