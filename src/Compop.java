import java.util.Arrays;

public class Compop {
    private static final String[] originOp = {"<", ">", "=", "!=", "<=", ">="};
    public static final String[] IRop = {"GE", "LE", "NE", "EQ", "GT", "LT"};

    public static String toIRop(String opcode) {
        int ind = Arrays.asList(originOp).indexOf(opcode);
        if (ind != -1) {
            return IRop[ind];
        } else {
            return null;
        }
    }
}
