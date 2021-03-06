package gov.nysenate.inventory.android;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Adapter;
import android.widget.Spinner;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import gov.nysenate.inventory.activity.LoginActivity;
import gov.nysenate.inventory.activity.SenateActivity;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.AppProperties;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.Serializer;
import gov.nysenate.inventory.util.Toasty;

public class OrigRemoteTask extends AsyncTask<Void, Void, Integer> {

    private Transaction original;
    private final Transaction trans;
    private final Spinner verMethod;
    private final ClearableAutoCompleteTextView remoteSigner;
    private final ClearableEditText remoteComment;
    private final ClearableEditText remoteHelpReferenceNum;
    private final Context context;

    public OrigRemoteTask(Context context, Transaction trans, Spinner spin, ClearableAutoCompleteTextView signer, ClearableEditText comment,
                          ClearableEditText helpNum) {
        this.trans = trans;
        this.context = context;
        this.verMethod = spin;
        this.remoteSigner = signer;
        this.remoteComment = comment;
        this.remoteHelpReferenceNum = helpNum;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected Integer doInBackground(Void... params) {
        //LoginActivity.activeAsyncTask = this;
        HttpUtils httpUtils = new HttpUtils();
        if (!httpUtils.isOnline()) {
            Log.i("RequestTask", "App is offline for:" + LoginActivity.WEBAPP_BASE_URL);

            try {
                new Toasty(SenateActivity.stContext).showMessage("!!ERROR: App is Offline.");
            } catch (Exception e) {
                Log.e(this.getClass().getName(), "!!ERROR: Could not show toasty message that App is Offline!!");
            }

            httpUtils.playSound(R.raw.noconnect);

            return HttpUtils.SC_SESSION_TIMEOUT;
        }

        String url = AppProperties.getBaseUrl(context);
        url += "PreviousRemoteInfo?nuxrpd=" + trans.getNuxrpd();

        HttpClient client = LoginActivity.getHttpClient();
        HttpResponse response = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            response = client.execute(new HttpGet(url));
            response.getEntity().writeTo(out);
            if (out.size() > 0) {
                original = Serializer.deserialize(out.toString(), Transaction.class).get(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response.getStatusLine().getStatusCode();
    }

    @Override
    protected void onPostExecute(Integer integer) {
        if (original != null) {
            setOriginalVerMethod();
            setOriginalSigner();
            setOriginalComments();
            setOriginalHelpNum();
        }
        LoginActivity.activeAsyncTask = null;
    }

    private void setOriginalVerMethod() {
        Adapter adapter = verMethod.getAdapter();
        for (int i = 1; i < adapter.getCount(); i++) {
            // Start at 1 since 0 is always null in this adapter.
            if (transVerMethodEqualsAdapterItem(adapter, i)) {
                verMethod.setSelection(i);
                return;
            }
        }
    }

    private boolean transVerMethodEqualsAdapterItem(Adapter adapter, int i) {
        return original.getVerificationMethod().equalsIgnoreCase(adapter.getItem(i).toString());
    }

    private void setOriginalSigner() {
        if (trans.isRemotePickup()) {
            remoteSigner.setText(original.getNareleaseby());
        }
        if (trans.isRemoteDelivery()) {
            remoteSigner.setText(original.getNaacceptby());
        }
    }

    private void setOriginalComments() {
        remoteComment.setText(original.getVerificationComments());
    }

    private void setOriginalHelpNum() {
        remoteHelpReferenceNum.setText(original.getHelpReferenceNum());
    }

}
