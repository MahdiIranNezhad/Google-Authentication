using System.IO;
using UnityEditor;
using UnityEngine;

namespace GoogleAuth.Editor
{
    internal class GoogleAuthSettingsProvider : SettingsProvider
    {
        private static SerializedObject _settings;
        private const string SettingsAssetPath = "Assets/Resources/GoogleAuthSettings.asset";

        internal static GoogleAuthSettingsData GetSettingsInstance()
        {
            var instance = Resources.Load<GoogleAuthSettingsData>("GoogleAuthSettings");
            if (instance == null)
            {
                var dir = Path.GetDirectoryName(SettingsAssetPath);
                if (!Directory.Exists(dir)) Directory.CreateDirectory(dir);
                instance = ScriptableObject.CreateInstance<GoogleAuthSettingsData>();
                AssetDatabase.CreateAsset(instance, SettingsAssetPath);
                AssetDatabase.SaveAssets();
            }

            return instance;
        }

        internal static SerializedObject GetSerializedSettings()
        {
            return _settings ?? (_settings = new SerializedObject(GetSettingsInstance()));
        }

        public GoogleAuthSettingsProvider(string path, SettingsScope scope) : base(path, scope)
        {
        }

        public override void OnGUI(string searchContext)
        {
            var settings = GetSerializedSettings();
            EditorGUILayout.LabelField("Native Google Auth Settings", EditorStyles.boldLabel);
            EditorGUILayout.PropertyField(settings.FindProperty(nameof(GoogleAuthSettingsData.WebClientId)), new GUIContent("Web Client ID"));
            if (settings.hasModifiedProperties)
                settings.ApplyModifiedProperties();

            EditorGUILayout.Space();
            EditorGUILayout.HelpBox("This setting is automatically applied to your AndroidManifest.xml at build time.", MessageType.Info);

            if (GUILayout.Button("Force Update AndroidManifest Now"))
            {
                if (GoogleAuthBuildProcessor.UpdateManifest())
                    EditorUtility.DisplayDialog("Success", "AndroidManifest.xml has been updated with the current Web Client ID.", "OK");
                else
                    EditorUtility.DisplayDialog("Error", "Web Client ID is not Valid.", "OK");
            }
        }

        [SettingsProvider]
        public static SettingsProvider CreateSettingsProvider()
        {
            GetSettingsInstance();
            return new GoogleAuthSettingsProvider("Project/Native Google Auth", SettingsScope.Project);
        }
    }
}