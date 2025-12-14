using System;
using UnityEngine;

namespace GoogleAuth
{
    public static class GoogleAuthentication
    {
        public struct AuthResult
        {
            public bool result;
            public string token_id;
            public string error_message;
        }
        private const int RC_SIGN_IN = 9002;

        public static Action<AuthResult> OnResultHandler;
        public static Action OnStart;

#if !UNITY_EDITOR && UNITY_ANDROID

        private class SignInListener : AndroidJavaProxy
        {
            public SignInListener() : base("com.unity.extension.GoogleAuth.GoogleSignInPlugin$SignInListener")
            {
            }

            public void OnSignInSuccess(string idToken)
            {
                OnResultHandler?.Invoke(new AuthResult
                {
                    result = true,
                    token_id = idToken,
                    error_message = string.Empty
                });
            }

            public void OnSignInFailed(string errorMessage)
            {
                OnResultHandler?.Invoke(new AuthResult
                {
                    result = false,
                    token_id = null,
                    error_message = errorMessage
                });
            }
        }

        static GoogleAuthentication()
        {
            AndroidJavaClass pluginClass = new AndroidJavaClass("com.unity.extension.GoogleAuth.GoogleSignInPlugin");
            pluginClass.CallStatic<AndroidJavaObject>("getInstance").Call("setListener", new SignInListener());
        }

        public static void Authorize()
        {
            OnStart?.Invoke();

            AndroidJavaClass unityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
            AndroidJavaObject currentActivity = unityPlayer.GetStatic<AndroidJavaObject>("currentActivity");

            AndroidJavaObject intent = new AndroidJavaObject("android.content.Intent", currentActivity, new AndroidJavaClass("com.unity.extension.GoogleAuth.SignInHelperActivity"));

            //currentActivity.Call("startActivity", intent);
            currentActivity.Call("startActivityForResult", intent, RC_SIGN_IN);
        }

#else

        public static void Authorize()
        { }

#endif
    }
}