package gov.nysenate.inventory.android;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;

public class SoundAlert extends AsyncTask<Object, Integer, String>
{    
MediaPlayer mp;

@Override
protected String doInBackground(Object... soundParams) {
        Context context = (Context)soundParams[0];
        int soundCnt = soundParams.length;
        for (int x=1;x<soundCnt;x++) {
            mp = MediaPlayer.create(context, ((Integer)soundParams[x]).intValue());
            mp.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                    mp.release();
                }

            });   
            mp.start();
        }
        return null;            
    }        

}    
