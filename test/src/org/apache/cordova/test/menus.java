package org.apache.cordova.test;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;

import org.apache.cordova.*;
import org.apache.cordova.api.LOG;

public class menus extends DroidGap {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init();
        super.registerForContextMenu(super.appView);
        super.loadUrl("file:///android_asset/www/menus/index.html");
    }

    // Demonstrate how to add your own menus to app

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        int base = Menu.FIRST;
        // Group, item id, order, title
        menu.add(base, base, base, "Item1");
        menu.add(base, base + 1, base + 1, "Item2");
        menu.add(base, base + 2, base + 2, "Item3");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LOG.d("menus", "Item " + item.getItemId() + " pressed.");
        this.appView.loadUrl("javascript:alert('Menu " + item.getItemId() + " pressed.')");
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        LOG.d("menus", "onPrepareOptionsMenu()");
        // this.appView.loadUrl("javascript:alert('onPrepareOptionsMenu()')");
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo info) {
        LOG.d("menus", "onCreateContextMenu()");
        menu.setHeaderTitle("Test Context Menu");
        menu.add(200, 200, 200, "Context Item1");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        this.appView.loadUrl("javascript:alert('Context Menu " + item.getItemId() + " pressed.')");
        return true;
    }

}
