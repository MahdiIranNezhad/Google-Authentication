package com.unity.extension.GoogleAuth;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.Task;

public class SignInHelperActivity extends Activity
{
    private static final String TAG = "SignInHelperActivity";
    private static final String META_DATA_KEY = "com.unity.extension.GoogleAuth.WEB_CLIENT_ID";

    private static final int RC_SIGN_IN = 9002;
    private static final int RC_ONE_TAP = 9003;
    private static final int RC_SILENT_SIGN_IN = 9004;

    private GoogleSignInClient googleSignInClient;
    private SignInClient oneTapClient;
    private BeginSignInRequest oneTapRequest;

    private static String loadWebClientIdStatic(Activity activity)
    {
        try
        {
            ApplicationInfo info = activity.getPackageManager().getApplicationInfo(activity.getPackageName(), PackageManager.GET_META_DATA);
            return info.metaData.getString("com.unity.extension.GoogleAuth.WEB_CLIENT_ID");
        } catch (Exception e)
        {
            return null;
        }
    }

    public void handleTryGetCachedIdToken()
    {
        try
        {
            String webClientId = loadWebClientIdStatic(this);
            if (webClientId == null)
                return;

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .build();

            GoogleSignInClient client = GoogleSignIn.getClient(this, gso);
            GoogleSignInAccount lastAccount = GoogleSignIn.getLastSignedInAccount(this);

            if (lastAccount == null)
                return;

            client.silentSignIn().addOnSuccessListener(account ->
            {
                if (account != null && account.getIdToken() != null)
                {
                    GoogleSignInPlugin.getInstance().onSilentSignInSuccess(account.getIdToken());
                }
            });

            // NOTE: no failure listener → do nothing

        } catch (Exception ignored)
        {
            // Explicitly ignore everything
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        String webClientId = loadWebClientIdStatic(this);
        if (webClientId == null)
        {
            GoogleSignInPlugin.getInstance().onSignInFailed("Invalid Web Client ID");
            finish();
            return;
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        oneTapClient = Identity.getSignInClient(this);
        oneTapRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(webClientId)
                        .setFilterByAuthorizedAccounts(true)
                        .build()
                )
                .setAutoSelectEnabled(true)
                .build();

        trySilentSignIn();
    }

    private void trySilentSignIn()
    {
        if (GoogleSignIn.getLastSignedInAccount(this) == null)
        {
            startOneTap();
            return;
        }

        googleSignInClient.silentSignIn().addOnCompleteListener(this, task ->
        {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().getIdToken() != null)
            {
                GoogleSignInPlugin.getInstance().onSignInSuccess(task.getResult().getIdToken());
                finish();
            } else
            {
                startOneTap();
            }
        });
    }

    private void startOneTap()
    {
        oneTapClient.beginSignIn(oneTapRequest)
                .addOnSuccessListener(this, result ->
                {
                    try
                    {
                        startIntentSenderForResult(result.getPendingIntent().getIntentSender(), RC_ONE_TAP, null, 0, 0, 0);
                    } catch (Exception e)
                    {
                        startClassicSignIn();
                    }
                })
                .addOnFailureListener(this, e ->
                {
                    // No eligible accounts or user disabled One Tap
                    startClassicSignIn();
                });
    }

    private void startClassicSignIn()
    {
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case RC_SIGN_IN:
                handleClassicResult(data);
                break;

            case RC_ONE_TAP:
                handleOneTapResult(data);
                break;

            case RC_SILENT_SIGN_IN:
                handleTryGetCachedIdToken();
                break;
        }
    }

    private void handleOneTapResult(Intent data)
    {
        try
        {
            String idToken = oneTapClient.getSignInCredentialFromIntent(data).getGoogleIdToken();

            if (idToken != null)
            {
                GoogleSignInPlugin.getInstance().onSignInSuccess(idToken);
            } else
            {
                startClassicSignIn();
            }
        } catch (ApiException e)
        {
            if (e.getStatusCode() != CommonStatusCodes.CANCELED)
            {
                startClassicSignIn();
            }
        } finally
        {
            finish();
        }
    }

    private void handleClassicResult(Intent data)
    {
        try
        {
            GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
            GoogleSignInPlugin.getInstance().onSignInSuccess(account.getIdToken());
        } catch (ApiException e)
        {
            GoogleSignInPlugin.getInstance().onSignInFailed("Error: " + e.getStatusCode());
        } finally
        {
            finish();
        }
    }
}
