import java.util.*;

public class ForGraph extends Graph {
    private String topLabel = "";
    private String incrLabel = "";
    private String outLabel = "";
    private List<Code> incrCodeList;

    public ForGraph() {
        this.topLabel = nextLable();
        this.incrLabel = nextLable();
        this.outLabel = nextLable();
        this.incrCodeList = new ArrayList<>();
    }

    @Override
    public String getTopLabel() {
        return topLabel;
    }

    @Override
    public String getIncrLabel() {
        return incrLabel;
    }

    @Override
    public String getOutLabel() {
        return outLabel;
    }

    @Override
    public List<Code> getIncrCodeList() {
        return incrCodeList;
    }

    @Override
    public void setIncrCodeList(List<Code> incrCodeList) {
        this.incrCodeList = incrCodeList;
    }
}
