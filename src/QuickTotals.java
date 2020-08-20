
public class QuickTotals
{
    //Quick Total params for adding up an employee's total time
    private String Name;
    private double Hours;

    public QuickTotals(String thisName, double thisHours)
    {
        Name = thisName;
        Hours = thisHours;
    }

    public String getName() {
        return this.Name;
    }

    public void setName(String setName) {
        this.Name = setName;
    }

    public double getHours() {
        return this.Hours;
    }

    public void setHours(double setHours) {
        this.Hours = setHours;
    }
}
