using UnityEditor;
using UnityEngine;
using GoogleAuth;

namespace GoogleAuth.Editor
{
    [CustomEditor(typeof(GoogleAuthSettingsData))]
    public class GoogleAuthSettingsDataEditor : UnityEditor.Editor
    {
        public override void OnInspectorGUI()
        {
            serializedObject.Update();

            EditorGUILayout.Space();
            EditorGUILayout.HelpBox(
                "This asset stores the configuration for the Native Google Auth package. " +
                "Click the button below to edit the value in Project Settings.", 
                MessageType.Info);
            EditorGUILayout.Space();

            // Display the Web Client ID field but make it read-only
            // to encourage editing in the correct place.
            GUI.enabled = false;
            EditorGUILayout.PropertyField(serializedObject.FindProperty(nameof(GoogleAuthSettingsData.WebClientId)));
            GUI.enabled = true;

            EditorGUILayout.Space();

            if (GUILayout.Button("Open Native Google Auth Settings"))
            {
                // This command opens the correct Project Settings window.
                SettingsService.OpenProjectSettings("Project/Native Google Auth");
            }
            
            serializedObject.ApplyModifiedProperties();
        }
    }
}
