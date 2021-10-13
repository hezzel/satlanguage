package logic.sat;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeSet;

/**
 * A SatProblem is essentially just a ClauseCollection which can be asked to solve itself using an
 * external SAT solver.  A SatProblem is mutable (clauses can be added).
 */
public class SatProblem extends ClauseCollection {
  /** This yields the index of the largest variable in the SatProblem. */
  private int getNumberVariables() {
    int max = 0;
    for (int i = 0; i < _clauses.size(); i++) {
      int topVar = _clauses.get(i).getHighestAtomIdentifier();
      if (topVar > max) max = topVar;
    }
    return max;
  }

  /**
   * This creates a file for the SAT solver and returns true, or prints a message and returns
   * false if creating the file fails for some reason.
   */
  private boolean createSatFile() {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter("problem.sat"));
      writer.write("p cnf " + getNumberVariables() + " " + +_clauses.size());
      writer.newLine();
      for (int i = 0; i < _clauses.size(); i++) {
        writer.write(_clauses.get(i).getSatDescription());
        writer.newLine();
      }
      writer.close();
    } catch (IOException e) {
      System.out.println("Could not create SAT file.");
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * This function runs the SAT solver on problem.sat and returns true if it succeeded; it it did
   * not, a message is printed instead.
   */
  private boolean runSatSolver() {
    Runtime rt = Runtime.getRuntime();
    // clean up old result, it any
    try { Process p = rt.exec("rm result"); p.waitFor(); } catch (Exception e) {}
    // start new minisat process
    try {
      Process p = rt.exec("./minisat problem.sat result");
      p.waitFor();
    } catch (Exception e) {
      System.out.println("Could not execute minisat.");
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * This reads the solution from the expected SAT output file, and turns it into a Solution.
   * If the file cannot be read -- for example because the SAT solver failed -- then null is
   * returned instead (and a message printed).
   */
  private Solution readSatFile() {
    try {
      File file = new File("result");
      Scanner reader = new Scanner(file);
      if (!reader.hasNextLine()) {
        System.out.println("Could not read result file.");
        return null;
      }
      String answer = reader.nextLine();
      if (answer.equals("UNSAT")) return new Solution(null);
      if (!answer.equals("SAT")) {
        System.out.println("Unexpected answer: " + answer);
        return null;
      }
      int num = reader.nextInt();
      TreeSet<Integer> set = new TreeSet<Integer>();
      while (num != 0) {
        if (num > 0) set.add(num);
        num = reader.nextInt();
      }
      return new Solution(set);
    } catch (IOException e) {
      System.out.println("Error reading result file.");
      e.printStackTrace();
      return null;
    }
  }

  /**
   * This function seeks to solve the SatProblem as it currently is (using an external SAT solver)
   * and returns the Solution that is found, if any.  If no yes/no answer can be found, then null
   * is returned instead.
   */
  public Solution solve() {
    if (!createSatFile()) return null;
    if (!runSatSolver()) return null;
    return readSatFile();
  }
}
