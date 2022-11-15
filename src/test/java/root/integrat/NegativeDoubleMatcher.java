package root.integrat;
import org.mockito.ArgumentMatcher;

public class NegativeDoubleMatcher implements ArgumentMatcher<Double> {
    @Override
    public boolean matches(Double aLong) {
        return aLong < 0;
    }
}