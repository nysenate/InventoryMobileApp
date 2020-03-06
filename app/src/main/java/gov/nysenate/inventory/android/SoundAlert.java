package gov.nysenate.inventory.android;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.widget.Toast;

import gov.nysenate.inventory.activity.LoginActivity;

public class SoundAlert extends AsyncTask<Object, Integer, String> {
    MediaPlayer mp;

    @Override
    protected String doInBackground(Object... soundParams) {
        //LoginActivity.activeAsyncTask = this;
        Context context = (Context) soundParams[0];

        if (soundParams == null) {
            return null;
        }

        int soundCnt = soundParams.length;
        for (int x = 1; x < soundCnt; x++) {
            try {
                mp = MediaPlayer.create(context,
                        ((Integer) soundParams[x]).intValue());
                mp.setOnCompletionListener(new OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        // TODO Auto-generated method stub
                        mp.reset();
                        mp.release();
                    }

                });
                mp.start();
            }
            catch (Exception e) {
                Toast.makeText(InvApplication.getAppContext(), "ERROR on SoundAlert playing sound#:"+x+" : "+e.getMessage() + ". Please contact STSBAC.", Toast.LENGTH_LONG).show();
            }

        }
        return null;
    }

    @Override
    protected void onPostExecute(String response) {
        LoginActivity.activeAsyncTask = null;
    }

}
