package gov.nysenate.inventory.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 *
 * @author Brian Heitner
 */
public class LoginStatus
{
 public final int VALID = 1; // Username/Password have been validated, password is not going to expire soon
 public final int PASSWORD_EXPIRED = 2; // Username/Password have been validated but password expired
 public final int PASSWORD_EXPIRES_SOON = 3; // Username/Password have been validated but password will expire soon
 public final int NO_ACCESS = 4;  // Username/Password have been validated but user does not have access to App.
 public final int PASSWORD_RULE_FAILURE = 5; // Password fails the rules setup for valid passwords
 public final int ACCOUNT_LOCKED = 6; // Username/Password are correct but account is locked 
 public final int INVALID_USERNAME_OR_PASSWORD = 99;  // Invalid Username and/or Password
 public final int INVALID = 100;  // Invalid Username/password and the default value, covers Oracle Generic Login Errors
  
 @SerializedName("nauser")
 @Expose private transient String nauser = null;
 @Expose private transient String destatus = null;
 @Expose private transient String cdseclevel = null;
 @Expose private transient int nustatus = INVALID;
 @Expose private transient int sqlErrorCode = -1;
 @Expose private transient Date dtpasswdexp = null; 
 @Expose private transient boolean usernamePasswordValid = false;
   
  public void setNauser(String nauser) {
    this.nauser = nauser;
  }
  
  public String getNauser() {
     return this.nauser;
  }

  public void setDestatus(String destatus) {
    this.destatus = destatus;
  }
  
  public String getDestatus() {
     return this.destatus;
  }
  
  public void setNustatus(int nustatus) {
    this.nustatus = nustatus;
    switch (this.nustatus) {
      case INVALID:
      case INVALID_USERNAME_OR_PASSWORD:
        this.usernamePasswordValid = false;
        break;
      default:
        this.usernamePasswordValid = true;
        break;
    }
  }
  
  public int getNustatus() {
     return this.nustatus;
  }

  public void setDtpasswdexp(Date dtpasswdexp) {
    this.dtpasswdexp = dtpasswdexp;
  }
  
  public Date getDtpasswdexp() {
     return this.dtpasswdexp;
  }
  
  public void setCdseclevel(String cdseclevel) {
    this.cdseclevel = cdseclevel;
  }
  
  public String getCdseclevel() {
     return this.cdseclevel;
  } 

  public void setSQLErrorCode(int sqlErrorCode) {
    this.sqlErrorCode = sqlErrorCode;
  }
  
  public int getSQLErrorCode() {
     return this.sqlErrorCode;
  }
  
  public boolean isUsernamePasswordValid () {
      return this.usernamePasswordValid;
  }
  
  public void parseJSON(String JSONString) {

      /*
       * Add the Ability to convert Android Object from JSON without any
       * external libraries.
       */

      try {
          System.out.println("Parsing JSON:"+JSONString);
          JSONObject jsonObject = new JSONObject(JSONString);
          try {
              this.setNauser(jsonObject.getString("nauser")) ;
          } catch (JSONException e2) {
              e2.printStackTrace();
          }

          try {
               this.setDestatus(jsonObject.getString("destatus")) ;
           } catch (JSONException e2) {
               e2.printStackTrace();
           }
          
          try {
              this.setNustatus(jsonObject.getInt("nustatus"));
          } catch (JSONException e2) {
              e2.printStackTrace();
          }
          
          try {
              @SuppressWarnings("deprecation")
              Date date = new Date(jsonObject.getString("dtpasswdexp"));
              this.setDtpasswdexp(date);
          } catch (JSONException e2) {
              e2.printStackTrace();
          }
          
          try {
              this.setCdseclevel(jsonObject.getString("cdseclevel"));
          } catch (JSONException e2) {
              e2.printStackTrace();
          }

          
          try {
              this.setSQLErrorCode(jsonObject.getInt("sqlErrorCode"));
          } catch (JSONException e2) {
              e2.printStackTrace();
          }
          
          try {
              String usernamePasswordValids  = jsonObject.getString("usernamePasswordValid").toLowerCase();
              this.usernamePasswordValid = usernamePasswordValids.startsWith("t");
          } catch (JSONException e2) {
              e2.printStackTrace();
          }

          
          
      } catch (JSONException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
      }

  }
  
  
}
