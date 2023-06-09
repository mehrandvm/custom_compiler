import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.IntervalSet;

public class CustomErrorStrategy extends DefaultErrorStrategy {

    @Override
    public void reportError(Parser recognizer, RecognitionException e) {
        if(!this.inErrorRecoveryMode(recognizer)) {
            CheckValid cv = new CheckValid();
            cv.setErrorToken();
        }
    }

    protected void reportUnwantedToken(Parser recognizer) {
        if(!this.inErrorRecoveryMode(recognizer)) {
           ;
        }
    }
}
