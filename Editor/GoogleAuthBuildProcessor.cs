using System.IO;
using System.Text;
using System.Xml;
using UnityEditor;
using UnityEditor.Build;
using UnityEditor.Build.Reporting;
using UnityEngine;

namespace GoogleAuth.Editor
{
    public class GoogleAuthBuildProcessor : IPreprocessBuildWithReport
    {
        public int callbackOrder => 0;
        private const string MetaDataName = "com.unity.extension.GoogleAuth.WEB_CLIENT_ID";

        public void OnPreprocessBuild(BuildReport report)
        {
            if (report.summary.platform != BuildTarget.Android) return;
            UpdateManifest();
        }

        [MenuItem("Tools/Google Auth/Force Update AndroidManifest")]
        public static bool UpdateManifest()
        {
            var settings = GoogleAuthSettingsProvider.GetSettingsInstance();
            if (settings == null)
            {
                Debug.LogError("Native Google Auth: Settings asset not found. Cannot update manifest.");
                return false;
            }

            string webClientId = settings.WebClientId;

            if (string.IsNullOrEmpty(webClientId) || !webClientId.EndsWith(".apps.googleusercontent.com"))
            {
                if (BuildPipeline.isBuildingPlayer)
                {
                    throw new BuildFailedException("Web Client ID is not set in Project Settings > Native Google Auth.");
                }

                Debug.LogError("Web Client ID is not set. Please set it in Project Settings > Native Google Auth and try again.");
                return false;
            }

            var manifest = new Manifest(Path.Combine(Application.dataPath, "Plugins/Android/AndroidManifest.xml"));
            manifest.SetMetaData(MetaDataName, webClientId);
            manifest.Save();
            Debug.Log("Native Google Auth: Successfully updated AndroidManifest.xml.");
            return true;
        }
    }

    internal class Manifest
    {
        private readonly XmlDocument _doc;
        private readonly string _path;
        private readonly XmlNamespaceManager _nsMgr;

        private const string AndroidXmlNamespace = "http://schemas.android.com/apk/res/android";

        public Manifest(string path)
        {
            _path = path;
            _doc = new XmlDocument();

            if (File.Exists(path))
            {
                _doc.Load(path);
            }
            else
            {
                _doc.LoadXml("<?xml version=\"1.0\" encoding=\"utf-8\"?><manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" package=\"com.unity3d.player\"><application></application></manifest>");
            }

            _nsMgr = new XmlNamespaceManager(_doc.NameTable);
            _nsMgr.AddNamespace("android", AndroidXmlNamespace);
        }

        public void SetMetaData(string name, string value)
        {
            var applicationNode = _doc.SelectSingleNode("/manifest/application") as XmlElement;
            if (applicationNode == null)
            {
                // This case should not happen with a valid manifest, but we handle it
                applicationNode = _doc.CreateElement("application");
                _doc.SelectSingleNode("/manifest")?.AppendChild(applicationNode);
            }

            string xpath = $"/manifest/application/meta-data[@android:name='{name}']";
            var metaDataNode = _doc.SelectSingleNode(xpath, _nsMgr) as XmlElement;

            if (metaDataNode == null)
            {
                metaDataNode = _doc.CreateElement("meta-data");
                metaDataNode.SetAttribute("name", AndroidXmlNamespace, name);
                applicationNode.AppendChild(metaDataNode);
            }

            metaDataNode.SetAttribute("value", AndroidXmlNamespace, value);
        }

        public void Save()
        {
            using (var writer = new XmlTextWriter(_path, new UTF8Encoding(false)))
            {
                writer.Formatting = Formatting.Indented;
                _doc.Save(writer);
            }
        }
    }
}