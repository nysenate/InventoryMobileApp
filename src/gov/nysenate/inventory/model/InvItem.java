package gov.nysenate.inventory.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class InvItem implements Parcelable
{
    String decommodityf = "";
    String type = ""; // set to NEW if entered/saved from inside app.
    String nusenate = "";
    String cdcategory = "";
    String cdlocat = "";
    String cdintransit = "N";
    String cdcommodity = "";
    String decomments = "";

    boolean selected = false;

    final transient int DECOMMODITYF = -101;
    final transient int TYPE = -102;
    final transient int NUSENATE = -103;
    final transient int CDCATEGORY = -104;
    final transient int SELECTED = -105;
    final transient int INTRANSIT = -106;
    final transient int CDCOMMODITY = -107;
    final transient int DECOMMENTS = -108;

    public InvItem(String nusenate, String cdcategory, String type,
            String decommodityf, String cdlocat) {
        this.nusenate = nusenate;
        this.cdcategory = cdcategory;
        this.type = type;
        this.decommodityf = decommodityf;
        this.cdlocat = cdlocat;
    }

    public InvItem() {

    }

    public String getDecommodityf() {
        return decommodityf;
    }

    public void setDecommodityf(String decommodityf) {
        this.decommodityf = decommodityf;
    }

    public String getNusenate() {
        return nusenate;
    }

    public void setNusenate(String nusenate) {
        this.nusenate = nusenate;
    }

    public String getCdcategory() {
        return cdcategory;
    }

    public void setCdcategory(String cdcategory) {
        this.cdcategory = cdcategory;
    }

    public String getCdlocat() {
        return cdlocat;
    }

    public void setCdlocat(String cdlocat) {
        this.cdlocat = cdlocat;
    }

    public String getCdintransit() {
        return cdintransit;
    }

    public void setCdintransit(String cdlocat) {
        this.cdintransit = cdintransit;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean getSelected() {
        return this.selected;
    }

    @Override
    public String toString() {
        return decommodityf;
    }

    public void setCdcommodity(String cdcommodity) {
        this.cdcommodity = cdcommodity;
    }

    public String getCdcommodity() {
        return cdcommodity;
    }

    public void setDecomments(String decomments) {
        this.decomments = decomments;
    }

    public String getDecomments() {
        return decomments;
    }

    public String toJSON() {

        /*
         * Add the Ability to convert Android Object to JSON without any
         * external libraries.
         */

        try {

            return getJSONObject().toString();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }

    }

    public JSONObject getJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("nusenate", getNusenate());
            jsonObject.put("type", getType());
            jsonObject.put("cdcategory", getCdcategory());
            jsonObject.put("decommodityf", getDecommodityf());
            jsonObject.put("cdlocat", getCdlocat());
            jsonObject.put("cdintransit", getCdintransit());
            jsonObject.put("cdcommodity", getCdcommodity());
            jsonObject.put("decomments", getDecomments());
            jsonObject.put("selected", getSelected());

            // Log.i("InvItem ToJSON", jsonObject.toString());

            return jsonObject;
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public void parseJSON(String JSONString) {

        /*
         * Add the Ability to convert Android Object from JSON without any
         * external libraries.
         */

        try {
            JSONObject jsonObject = new JSONObject(JSONString);
            try {
                this.setNusenate(jsonObject.getString("nusenate"));
            } catch (JSONException e2) {
                e2.printStackTrace();
            }
            try {
                this.setType(jsonObject.getString("type"));
            } catch (JSONException e2) {
                e2.printStackTrace();
            }
            try {
                this.setCdcategory(jsonObject.getString("cdcategory"));
            } catch (JSONException e2) {
                e2.printStackTrace();
            }
            try {
                this.setDecommodityf(jsonObject.getString("decommodityf"));
            } catch (JSONException e2) {
                e2.printStackTrace();
            }

            try {
                this.setCdlocat(jsonObject.getString("cdlocat"));
            } catch (JSONException e2) {
                e2.printStackTrace();
            }

            try {
                this.setCdintransit(jsonObject.getString("cdintransit"));
            } catch (JSONException e2) {
                e2.printStackTrace();
            }

            try {
                this.setCdcommodity(jsonObject.getString("cdcommodity"));
            } catch (JSONException e2) {
                this.setCdcommodity(""); // No Error msgs since cdcommodity may
                                         // or may not be included
            }

            try {
                this.setDecomments(jsonObject.getString("decomments"));
            } catch (JSONException e2) {
                this.setDecomments(""); // No Error msgs since decomments may or
                                        // may not be included
            }

            try {
                if (jsonObject.getString("selected").trim().toUpperCase()
                        .startsWith("T")) {
                    this.setSelected(true);
                } else {
                    this.setSelected(false);
                }
            } catch (JSONException e2) {
                this.setSelected(false);
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    // ---------- Code for Parcelable interface ----------

    public InvItem(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        decommodityf = in.readString();
        type = in.readString();
        nusenate = in.readString();
        cdcategory = in.readString();
        cdlocat = in.readString();
        cdintransit = in.readString();
        cdcommodity = in.readString();
        decomments = in.readString();
        selected = in.readByte() == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(decommodityf);
        dest.writeString(type);
        dest.writeString(nusenate);
        dest.writeString(cdcategory);
        dest.writeString(cdlocat);
        dest.writeString(cdintransit);
        dest.writeString(cdcommodity);
        dest.writeString(decomments);
        dest.writeByte((byte) (selected ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<InvItem> CREATOR = new Parcelable.Creator<InvItem>()
    {

        @Override
        public InvItem createFromParcel(Parcel in) {
            return new InvItem(in);
        }

        @Override
        public InvItem[] newArray(int size) {
            return new InvItem[size];
        }
    };

}
