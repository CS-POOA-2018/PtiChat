package fr.centralesupelec.ptichatapp.NativeSocketClient;

import android.content.Context;

public class SendMessageObject {
    private Context mCtx;
    private String mMessage;

    SendMessageObject(Context ctx, String message) {
        mCtx = ctx;
        mMessage = message;
    }

    public Context getContext() {
        return mCtx;
    }

    public String getMessage() {
        return mMessage;
    }
}
