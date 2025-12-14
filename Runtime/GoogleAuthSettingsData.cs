using Sirenix.OdinInspector;
using UnityEngine;

namespace GoogleAuth
{
    [CreateAssetMenu(fileName = "GoogleAuthSettings", menuName = "Google Auth/Settings Asset", order = 1)]
    public class GoogleAuthSettingsData : ScriptableObject
    {
        [Tooltip("Your Web Client ID (client_type: 3) from your google-services.json file."), ReadOnly]
        public string WebClientId;
    }
}
