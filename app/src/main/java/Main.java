import language.Program;
import logic.sat.SatProblem;
import java.nio.file.Paths;
import java.nio.file.FileSystems;

public class Main {
  public static void main(String[] args) {
    Program program = new Program();
    String filename = "../inputs/test.log";

    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if (arg.length() > 9 && arg.substring(0,9).equals("--solver=")) {
        SatProblem.solvername = arg.substring(9);
        System.out.println("Setting SAT solver to: " + SatProblem.solvername);
      }
      else filename = "../" + arg;
    }

    program.readFromFile(filename);
    program.execute(false);

    /*
    program.declare("queen[x,y] :: Bool for x ∈ {1..8}, y ∈ {1..8}");
    // there is a queen in every row
    program.require("∀ y ∈ {1..8}.∃ x ∈ {1..8}.queen[x,y]");
    // there is only one queen per row
    program.require("∀ y ∈ {1..8}.∀ x1 ∈ {1..8-1}.∀ x2 ∈ {x1+1..8}.queen[x1,y] → ¬queen[x2,y]");
    // there is only one queen per column
    program.require("∀ x ∈ {1..8}.∀ y1 ∈ {1..8-1}.∀ y2 ∈ {y1+1..8}.queen[x,y1] → ¬queen[x,y2]");
    // there is only one queen per diagonal
    program.require("∀ x1 ∈ {1..8-1}.∀ y1 ∈ {1..8}.∀ x2 ∈ {x1+1..8}.∀ y2 ∈ {1..8} with x1-y1 = x2-y2.¬queen[x1,y1] ∨ ¬queen[x2,y2]");
    program.require("∀ x1 ∈ {1..8-1}.∀ y1 ∈ {1..8}.∀ x2 ∈ {x1+1..8}.∀ y2 ∈ {1..8} with x1+y1 = x2+y2.¬queen[x1,y1] ∨ ¬queen[x2,y2]");
    program.setOutput(
      "for y := 1 to 8 do {" +
      "  for x := 1 to 8 do {" +
      "    if queen[x,y] then print('Q')" +
      "    else print('.')" +
      "  }" +
      "  println()" +
      "}");
    */
  }
}
