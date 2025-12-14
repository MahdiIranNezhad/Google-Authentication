package com.unity.extension.GoogleAuth;

import com.unity3d.player.UnityPlayer;

public class GoogleSignInPlugin {
    private static GoogleSignInPlugin instance;
    private SignInListener mListener;

    public static GoogleSignInPlugin getInstance() {
        if (instance == null) {
            instance = new GoogleSignInPlugin();
        }
        return instance;
    }

    public void setListener(SignInListener listener) {
        mListener = listener;
    }

    public void onSignInSuccess(String idToken) {
        if (mListener != null) {
            UnityPlayer.currentActivity.runOnUiThread(() -> mListener.OnSignInSuccess(idToken));
        }
    }

    public void onSignInFailed(String errorMessage) {
        if (mListener != null) {
            UnityPlayer.currentActivity.runOnUiThread(() -> mListener.OnSignInFailed(errorMessage));
        }
    }

    public interface SignInListener {
        void OnSignInSuccess(String idToken);

        void OnSignInFailed(String errorMessage);
    }
}
