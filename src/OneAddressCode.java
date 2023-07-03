import java.util.*;

public class OneAddressCode extends Code {
    public OneAddressCode(String result, String type) {
        this(null, result, type);
    }

    public OneAddressCode(String opcode, String result, String type) {
        this.opcode = opcode;
        this.result = result;
        this.type = type;
    }

    public String getOp1() {
        return null;
    }
    public String getOp2() {
        return null;
    }
    public String toIR() {
        return opcode + " " + result;
    }

    public List<String> getOpArray() {
        return new ArrayList<>();
    }
}
