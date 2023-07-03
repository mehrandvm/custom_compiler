import java.util.*;

public class TwoAddressCode extends Code {
    private String op1;

    public TwoAddressCode(String opcode, String op1, String result, String type) {
        this.opcode = opcode;
        this.op1 = op1;
        this.result = result;
        this.type = type;
    }

    public String getOp1() {
        return op1;
    }
    public String getOp2() {
        return null;
    }
    public String toIR() {
        return opcode + " " + op1 + " " + result;
    }

    public List<String> getOpArray() {
        List<String> list = new ArrayList<>();
        list.add(this.op1);
        return list;
    }
}
