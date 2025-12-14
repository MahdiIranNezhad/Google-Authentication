package com.unity.extension.GoogleAuth;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class SignInHelperActivity extends Activity
{
    private static final String TAG = "SignInHelperActivity";
    private static final String META_DATA_KEY = "com.unity.extension.GoogleAuth.WEB_CLIENT_ID";
    private static final int RC_SIGN_IN = 9002;
    private GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        String webClientId = null;
        try
        {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            webClientId = bundle.getString(META_DATA_KEY);
        } catch (PackageManager.NameNotFoundException | NullPointerException e)
        {
            Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
        }

        if (webClientId == null || webClientId.isEmpty() || webClientId.equals("YOUR_TYPE_3_WEB_CLIENT_ID_GOES_HERE"))
        {
            String errorMsg = "ERROR: Could not find or invalid meta-data '" + META_DATA_KEY + "' in AndroidManifest.xml";
            Log.e(TAG, errorMsg);
            GoogleSignInPlugin.getInstance().onSignInFailed(errorMsg);
            finish();
            return;
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN)
        {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try
            {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String idToken = account.getIdToken();
                GoogleSignInPlugin.getInstance().onSignInSuccess(idToken);
            } catch (ApiException e)
            {
                Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
                GoogleSignInPlugin.getInstance().onSignInFailed("Error: " + e.getStatusCode());
            }
        }
        finish();
    }
}
