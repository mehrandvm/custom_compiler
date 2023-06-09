
import java.util.*;
import java.util.HashMap;
import java.util.Map;
public class TinyCode {
    private List<Code> codeList;
    private int currReg = 0;
    private HashMap<String, String> map;
    private List<String> dirtyList;
    private int paramId;
    private int localTemp;
    private int declId;
    private boolean isDebug = true;


    public TinyCode(List<Code> codeList,int paramId,int localTemp, int declId) {
        this.codeList = codeList;
        this.paramId = paramId;
        this.localTemp = localTemp;
        this.declId = declId;
        this.map = new HashMap<String, String>();
        this.dirtyList = new ArrayList<String>();
        initMap();
    }

    public List<Code> getCodeList() {
        return codeList;
    }

    public int getCurrReg() {
        return currReg;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void spillAllReg(){
        for(String key : map.keySet()) {
            String value = map.get(key);
            if (!value.equals("null")) {
                if (checkDirty(value)) {
                    spillReg(key, value);
                }
                map.put(key, "null");
            }
        }
    }

    public void spillReg(String key, String value) {
        dbgPrint(";Spilling variable: " + value);
        System.out.println("move " + key + " " +getTinyTransform(value));
        dirtyList.remove(value);
    }

    public void initMap() {
        for (int i=0; i<4; i++){
            map.put("r"+i, "null");
        }
    }

    public String getNextReg() {
        if (currReg <= 3) {
            return "r" + (currReg++);
        } else {
            return GlobalTinyCode.getNextReg();
        }
    }

    public void toTinyCode() {
        for (Code c : codeList) {
            if (c.getClass() == OneAddressCode.class) {
                handleOneAddress(c);  // write and read...
            } else if (c.getClass() == TwoAddressCode.class) {
                //System.out.println(this.map);
                handleTwoAddress(c);  // store
            } else {
                handleThreeAddress(c);
            }
        }
        dbgPrint("; Final map" + map);
    }

    public void handleOneAddress(Code c) {
        String op = c.getOpcode();
        String result = c.getResult();
        Set<String> liveness = c.getOut();
        String reg = "";
        switch (op) {
            case "LABEL":
                System.out.println(getTinyOpcode(op) + " " + result);
                break;
            case "JUMP":
                if (c.isTail()) {
                    spillAllReg();
                }
                System.out.println(getTinyOpcode(op).replace("u", "") + " " + result);
                break;
            case "JSR":
                spillAllReg();
                for(int i=0; i<4; i++) {
                    System.out.println("push " + "r" + i);
                }
                System.out.println(op.toLowerCase() + " " + result);
                for(int i=3; i>-1; i--) {
                    System.out.println("pop " + "r" + i);
                }
                break;
            case "RET":
                // at the end of each jsr function call, free all reg map
                spillAllReg();

                System.out.println("unlnk");
                System.out.println(getTinyOpcode(c.getOpcode()));
                break;
            case "LINK":
                //System.out.println(op.toLowerCase() + " " + (declId));
                int resultValue = localTemp + declId - 2;
                System.out.println(op.toLowerCase() + " " + resultValue);
                break;
            default:  // READ or WRITE
                dbgPrint(";" + c + " " +liveness);
                if (op.contains("READ") || op.contains("WRITEI") || op.contains("WRITEF") || op.contains("POP") || op.contains("PUSH")) {

                    if (op.contains("READ") || op.contains("POP")) {
                        reg = regAllocate(c, result, liveness);
                        addDirty(result);
                    } else {
                        reg = regEnsure(c, result, liveness);
                    }

                    if (op.contains("POP") || op.contains("PUSH")) {
                        System.out.println(getTinyOpcode(op) + " " + reg);
                    } else {
                        dbgPrint(";sys " + map);
                        System.out.println("sys " + getTinyOpcode(op) + " " + reg);
                    }
                } else {  // hgp: what's the else situation?
                    System.out.println("sys " + getTinyOpcode(c.getOpcode()) + " " + getTinyTransform(result));
                }
                if (c.isTail()) {
                    spillAllReg();
                }
                break;
        }
    }

    public void handleTwoAddress(Code c) {
        String op1 = c.getOp1();
        String result = c.getResult();
        String reg1 = "";
        String reg2 = "";
        Set<String> liveness = c.getOut();

        if (result.equals("$R")) {
            reg1 = regEnsure(c, op1, liveness);
            System.out.println("move " + reg1 + " " + getTinyTransform(result));
        } else {  // STORE
            dbgPrint(";" + c + " "+liveness);
            reg1 = regEnsure(c, op1, liveness);
            reg2 = regAllocate(c, result, liveness);
            addDirty(result);
            if (reg2.equals(reg1)) {
                dbgPrint(";Switching owner of register " + reg2 + " to " + result);
            } else {
                System.out.println("move " + reg1 + " " + reg2);
            }
            checkRegLive(liveness);
            if (c.isTail()) {
                spillAllReg();
            }
        }
    }

    public void handleThreeAddress(Code c) {
        String op1 = c.getOp1();
        String op2 = c.getOp2();
        String op = c.getOpcode();
        String type = c.getType();
        String result = c.getResult();
        Set<String> liveness = c.getOut();
        String reg1, reg2, reg3;
        String[] operationList = {"ADDI","ADDF","SUBI","SUBF","MULTI","MULTF","DIVI","DIVF"};
        if (Arrays.asList(operationList).contains(op)){
            dbgPrint(";"+c +" " + liveness);
            reg1 = regEnsure(c, op1, liveness);
            reg2 = regEnsure(c, op2, liveness);
            reg3 = switchReg(op1, result, liveness);
            System.out.println(getTinyOpcode(op) + " " + reg2 + " " + reg3);
            addDirty(result);
            checkRegLive(liveness);
            if (c.isTail()) {
                spillAllReg();
            }
        } else if (Arrays.asList(Compop.IRop).contains(op)) {  // compare op
            dbgPrint(";" + c + " " + liveness);
            reg1 = regEnsure(c, op1, liveness);
            reg2 = regEnsure(c, op2, liveness);
            if (type.equals("INT")) {
                System.out.println("cmpi " + reg1 + " " + reg2);
            } else if (type.equals("FLOAT")) {
                System.out.println("cmpr " + reg1 + " " + reg2);
            }
            checkRegLive(liveness);

            if (c.isTail()) {
                spillAllReg();
            }

            System.out.println("j" + op.toLowerCase() + " " + getTinyTransform(result));
        }
    }

    public String regEnsure(Code c, String op, Set<String> liveness) {
        if (op.isEmpty() || !op.startsWith("$") && !Character.isLetter(op.charAt(0))) return op;

        for (String key : map.keySet()) {
            String value = map.get(key);
            if (value.equals(op)) {
                dbgPrint(";ensure(): " + op + " has register " + key + " " + map);
                return key;
            }
        }

        String reg = regAllocate(c, op, liveness);;
        dbgPrint(";loading " + op + " to register " + reg);
        System.out.println("move " + getTinyTransform(op) + " " + reg);
        map.put(reg, op);

        return reg;
    }

    public String regAllocate(Code c, String op, Set<String> liveness) {
        if (op.isEmpty()) return op;

        for (int i = map.size() - 1; i >= 0; i--) {
            String key = "r" + i;
            String value = map.get(key);
            if (value.equals("null")) {
                map.put(key, op);
                dbgPrint(";ensure(): " + op + " gets register " + key + " " + map);
                return key;
            }
        }

        String reg = "";
        List<String> opList = c.getOpArray();
        for (String key : map.keySet()) {
            String value = map.get(key);
            if (!opList.contains(value)) {
                reg = key;
                if (!liveness.contains(value)) {
                    break;
                }
            }
        }

        dbgPrint(";allocate force to spill " + reg + "->" + map.get(reg));
        regFree(map.get(reg), liveness);
        map.put(reg, op);
        return reg;
    }

    public void regFree(String op, Set<String> liveness) {
        if (!liveness.contains(op)) {
            dbgPrint(";Freeing unused variable " + op + " " + map);
        }
        String key = regLookup(op);
        if (checkDirty(op)) {
            spillReg(key, op);
        }
        map.put(key, "null");
    }

    public String regLookup(String op){
        for( String key : this.map.keySet()){
            String value = this.map.get(key);
            if (op.equals(value)){return key;}
        }
        return null;
    }

    public String switchReg(String op1, String result, Set<String> liveness) {
        String reg1 = regLookup(op1);
        map.put(reg1, result);
        dbgPrint(";Switching owner of register " + reg1 + " to " + result);
        if (checkDirty(op1)) {
            spillReg(reg1, op1);
        }
        return reg1;
    }

    public void checkRegLive(Set<String> liveness){
        for(String key : map.keySet()) {
            String value = map.get(key);
            if (!value.equals("null") && !liveness.contains(value)) {
                regFree(value, liveness);
            }
        }
    }

    public void addDirty(String result) {
        if (!checkDirty(result)) {
            dirtyList.add(result);
        }
    }

    public boolean checkDirty(String value){
        return this.dirtyList.contains(value);
    }

    public String getTinyTransform(String s){
        String result = s;
        if (s.startsWith("$L")) {
            result = "$-" + s.replace("$L", "");
        } else if (s.startsWith("$T")) {
            int resultValue = Integer.parseInt(s.replace("$T","")) + declId - 1;
            result = "$-" + resultValue;
        } else if (s.startsWith("$P")) {
            int temp = Integer.parseInt(s.replace("$P", ""));
            temp = paramId + 5 - temp;
            result = "$" + temp;
        } else if (s.startsWith("$R")) {
            result = "$" + (5 + paramId);
        }

        return result;
    }

    public String getTinyOpcode(String opcode) {
        String op = opcode.substring(0, 3).toLowerCase();
        if (op.equals("mul") || op.equals("div") || op.equals("add") || op.equals("sub")) {
            op += opcode.endsWith("F") ? "r" : "i";
        } else if (opcode.startsWith("READ")) {
            if (opcode.endsWith("F")){
                op = "readr";
            } else if (opcode.endsWith("I")){
                op = "readi";
            } else {
                op = "reads";
            }
        } else if (opcode.startsWith("WRITE")) {
            if (opcode.endsWith("F")){
                op = "writer";
            } else if (opcode.endsWith("I")){
                op = "writei";
            } else {
                op = "writes";
            }
        } else if (opcode.equals("LABEL") || opcode.equals("JUMP")){
            op = opcode.toLowerCase();
        } else if (opcode.equals("POP") || opcode.equals("PUSH")){
            op = opcode.toLowerCase();
        }

        return op;
    }

    public void dbgPrint(String msg) {
        if (isDebug) {
            System.out.println(msg);
        }
    }
}
