package org.telekit.controls.i18n;

import org.telekit.base.i18n.BundleLoader;

// Should contain basic words or phrases which can appear in controls or widgets.
public interface ControlsMessages {

    String AUTHENTICATION="Authentication";
    String CONFIRMATION="Confirmation";
    String COUNT="Count";
    String DELIMITER="Delimiter";
    String DESCRIPTION="Description";
    String DETAILS="Details";
    String DOCUMENTATION="Documentation";
    String ERROR="Error";
    String EXAMPLE="Example";
    String FILE="File";
    String FILE_FORMAT="FileFormat";
    String HEADER="Header";
    String HELP="Help";
    String INFO="Info";
    String KEY="Key";
    String LANGUAGE="Language";
    String LENGTH="Length";
    String LOG="Log";
    String MODE="Mode";
    String NAME="Name";
    String NEW="New";
    String PARAMETERS="Parameters";
    String PASSWORD="Password";
    String PASSWORDS="Passwords";
    String PATTERN="Pattern";
    String PLUGINS="Plugins";
    String PREFERENCES="Preferences";
    String PREFIX="Prefix";
    String PREVIEW="Preview";
    String RANDOM="Random";
    String RESULT="Result";
    String SEARCH="Search";
    String SEPARATOR="Separator";
    String SETTINGS="Settings";
    String STATUS="Status";
    String STEP="Step";
    String TEXT="Text";
    String TOOLS="Tools";
    String TYPE="Type";
    String USERNAME="Username";
    String VALUE="Value";
    String VERSION="Version";
    String WARNING="Warning";
    String WINDOW="Window";

    String ACTION_ADD ="action.Add";
    String ACTION_APPLY ="action.Apply";
    String ACTION_BROWSE ="action.Browse";
    String ACTION_DELETE ="action.Delete";
    String ACTION_CANCEL ="action.Cancel";
    String ACTION_CLOSE ="action.Close";
    String ACTION_COPY = "action.Copy";
    String ACTION_CUT = "action.Cut";
    String ACTION_DISABLE ="action.Disable";
    String ACTION_DUPLICATE ="action.Duplicate";
    String ACTION_EDIT ="action.Edit";
    String ACTION_ENABLE ="action.Enable";
    String ACTION_EXPORT ="action.Export";
    String ACTION_GENERATE ="action.Generate";
    String ACTION_IMPORT ="action.Import";
    String ACTION_INSTALL ="action.Install";
    String ACTION_OK ="action.OK";
    String ACTION_PASTE = "action.Paste";
    String ACTION_PREVIEW = "action.Preview";
    String ACTION_QUIT ="action.Quit";
    String ACTION_REDO = "action.Redo";
    String ACTION_REMOVE ="action.Remove";
    String ACTION_RESTART ="action.Restart";
    String ACTION_SAVE ="action.Save";
    String ACTION_SELECT_ALL = "action.SelectAll";
    String ACTION_START ="action.Start";
    String ACTION_STOP ="action.Stop";
    String ACTION_SUBMIT ="action.Submit";
    String ACTION_UNDO = "action.Undo";
    String ACTION_UNINSTALL ="action.Uninstall";
    String ACTION_UPDATE ="action.Update";

    String FILE_DIALOG_TEXT = "file-dialog.text";
    String FILE_DIALOG_XML = "file-dialog.xml";
    String FILE_DIALOG_YAML = "file-dialog.yaml";
    String FILE_DIALOG_ZIP = "file-dialog.zip";

    static BundleLoader getLoader() { return BundleLoader.of(ControlsMessages.class); }
}
