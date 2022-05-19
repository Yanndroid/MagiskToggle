package de.dlyt.yanndroid.magisktoggle;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.EditText;

import com.topjohnwu.superuser.Shell;

public class QSTile extends TileService {
    private String magiskPackage;

    @Override
    public void onCreate() {
        super.onCreate();
        magiskPackage = getSharedPreferences("sp", Context.MODE_PRIVATE).getString("magiskPackage", null);
    }

    private void packageNameDialog() {
        EditText edittext = new EditText(this);
        edittext.setHint(R.string.package_name);
        edittext.setText(magiskPackage);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.magisk).setView(edittext)
                .setPositiveButton(android.R.string.ok, (dialog1, whichButton) -> {
                    getSharedPreferences("sp", Context.MODE_PRIVATE).edit().putString("magiskPackage", magiskPackage = edittext.getText().toString()).apply();
                    dialog1.dismiss();
                    if (magiskPackage != null) updateTile();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        showDialog(dialog);
    }

    private boolean isMagiskEnabled() {
        return (magiskPackage != null) && !Shell.cmd("pm list packages -d").exec().getOut().contains("package:" + magiskPackage);
    }

    private boolean hasPackage() {
        return (magiskPackage != null) && Shell.cmd("pm list packages").exec().getOut().contains("package:" + magiskPackage);
    }

    private void updateTile() {
        Tile tile = getQsTile();
        boolean magiskEnabled = isMagiskEnabled();
        tile.setState(magiskEnabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.setSubtitle(magiskEnabled ? getString(R.string.visible) : getString(R.string.hidden));
        }
        tile.updateTile();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        if (magiskPackage != null) updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();
        if (!hasPackage()) {
            packageNameDialog();
            return;
        }
        Shell.cmd("pm " + (!isMagiskEnabled() ? "enable " : "disable ") + magiskPackage).exec();
        updateTile();
    }
}