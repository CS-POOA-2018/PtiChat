package fr.centralesupelec.ptichatapp.NativeSocketClient;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class SendMessageTask extends AsyncTask<SendMessageObject, Void, Integer> {

    @Override
    protected Integer doInBackground(SendMessageObject... messages) {
        for (SendMessageObject smo : messages) {
            Log.d("SMs", "📧 Sending " + smo.getMessage());
            SocketSingleton.getInstance(smo.getContext()).getSocketClient().sendMessage(smo.getMessage());
        }
        return 0;
    }

    public static void sendMessageAsync(Context ctx, String message) {
        final AsyncTask<SendMessageObject, Void, Integer> smt = new SendMessageTask();
        smt.execute(new SendMessageObject(ctx, message));
    }
}
