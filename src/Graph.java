import org.antlr.v4.codegen.model.AddToLabelList;

import java.util.*;

public abstract class Graph {
    private static int labelId = 0;
    private int startSize = 0;


    public String nextLable() { return "label" + (++labelId);}

    public int getStartSize() {
        return startSize;
    }

    public void setStartSize(int startSize) {
        this.startSize = startSize;
    }

    abstract String getTopLabel();
    abstract String getOutLabel();
    abstract String getIncrLabel();
    abstract List<Code> getIncrCodeList();
    abstract void setIncrCodeList(List<Code> list);
}
