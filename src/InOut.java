
//Object used in the refreshTime function to add up the corresponding lines of an employees individual shifts
public class InOut
{
    private String In;
    private String Out;

    public InOut(String thisIn, String thisOut)
    {
        In = thisIn;
        Out = thisOut;
    }

    public String getIn() {
        return this.In;
    }

    public void setIn(String newIn) {
        this.In = newIn;
    }

    public String getOut() {
        return this.Out;
    }

    public void setOut(String newOut) {
        this.Out = newOut;
    }
}
