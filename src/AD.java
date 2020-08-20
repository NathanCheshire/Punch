
//Object used for storing admin details
public class AD
{
    private String name;
    private String value;

    public AD(String thisName, String thisValue)
    {
        name = thisName;
        value = thisValue;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String newValue) {
        this.value = newValue;
    }
}
