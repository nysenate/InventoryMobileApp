package gov.nysenate.inventory.android;

import android.app.Application;

public class InvApplication extends Application 
{

        public static boolean isActivityVisible() {
          return activityVisible;
        }  
        
        public static boolean isActivityDestroyed() {
            return activityDestroyed;
          }  

        
        public static void activityResumed() {
          activityVisible = true;
          activityDestroyed = false;
        }

        public static void activityPaused() {
          activityVisible = false;
          activityDestroyed = false;
        }
        public static void activityDestroyed() {
          activityDestroyed = true;
        }

        private static boolean activityVisible;
        private static boolean activityDestroyed = false;
 }
