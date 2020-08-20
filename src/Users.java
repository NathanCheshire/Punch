
public class Users
{
    //User fields, username, their password (make it secure pls), binary is weather they are punched in or out
    private String ChangeName;
    private String ChangePass;
    private String ChangeBinary;

    public Users(String thisChangeName, String thisChangePass, String thisChangeBinary)
    {
        ChangeName = thisChangeName;
        ChangePass = thisChangePass;
        ChangeBinary = thisChangeBinary;
    }

    public String getChangeName() {
        return this.ChangeName;
    }

    public void setChangeName(String NewName) {
        this.ChangeName = NewName;
    }

    public String getChangePass() {
        return this.ChangePass;
    }

    public void setChangePass(String NewPass) {
        this.ChangePass = NewPass;
    }

    public String getChangeBinary() {
        return this.ChangeBinary;
    }

    public void setChangeBinary(String NewBinary) {
        this.ChangeBinary = NewBinary;
    }
}
