package root.integrat;
import org.mockito.ArgumentMatcher;

public class NegativeLongMatcher implements ArgumentMatcher<Long> {
    @Override
    public boolean matches(Long aLong) {
        return aLong < 0;
    }
}
