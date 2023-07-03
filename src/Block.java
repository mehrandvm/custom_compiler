
public class Block extends Program implements SymbolTable {
    private SymbolTable parent = null;

    public Block(SymbolTable parent) {
        this.parent = parent;
    }

    @Override
    public SymbolTable getParent() {
        return this.parent;
    }
}
