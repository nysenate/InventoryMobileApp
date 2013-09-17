package gov.nysenate.inventory.model;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class Toasty
{
    Context context;
    String message = "";
    int duration = Toast.LENGTH_SHORT;
    int gravity = Gravity.CENTER;
    
    public Toasty(Context context) {
        this.context = context;
    }
 
    public Toasty(Context context, String message) {
        this.context = context;
        this.message = message;
    }

    public Toasty(Context context, String message, int duration) {
        this.context = context;
        this.message = message;
        this.duration = duration;
    }
    
    public Toasty(Context context, String message, int duration, int gravity) {
        this.context = context;
        this.message = message;
        this.duration = duration;
        this.gravity = gravity;
    }
    
    public void showMessage() {
        showMessage(message, Toast.LENGTH_SHORT, Gravity.CENTER);
    }
    
    public void showMessage(String message) {
        showMessage(message, Toast.LENGTH_SHORT, Gravity.CENTER);
    }
    
    public void showMessage(String message, int duration) {
        showMessage(message, duration, Gravity.CENTER);
    }
    
   public void showMessage(String message, int duration, int gravity) {    

       Toast toast = Toast.makeText(context,
               message, duration);
        toast.setGravity(gravity, 0, 0);
        toast.show();
   }
   
   public String getMessage() {
       return message;
   }

   public void setMessage(String message) {
       this.message = message;
   }
   
   public int getDuration() {
       return duration;
   }

   public void setDuration(int duration) {
       this.duration = duration;
   }
   
   public int getGravity() {
       return gravity;
   }

   public void setGravity(int gravity) {
       this.gravity = gravity;
   }

}
