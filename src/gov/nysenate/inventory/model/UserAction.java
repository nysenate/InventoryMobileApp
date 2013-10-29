package gov.nysenate.inventory.model;

import java.util.Date;

/*
 * UserAction will be used in as a log of User Actions. To force only new entries, 
 * no setters were created. The developer is forced to call a 
 * new UserAction([screen], [action], [description]) which will then set a date/time. If 
 * setters were used, there would be a potential for the developer to "update" an
 * existing UserAction, which we don't want.
 */
public class UserAction
{
    private String dedescription = "";
    private String deaction = "";
    private String cdscreen = "";
    private Date dtdate;

    public UserAction(String cdscreen, String deaction, String dedescription) {
        this.cdscreen = cdscreen;
        this.deaction = deaction;
        this.dedescription = dedescription;
        dtdate = new Date();
    }

    public String getScreen() {
        return this.cdscreen;
    }

    public String getDescription() {
        return this.dedescription;
    }

    public String getAction() {
        return this.deaction;
    }

    public Date getDate() {
        return this.dtdate;
    }

}
