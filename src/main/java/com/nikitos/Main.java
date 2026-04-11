package com.nikitos;


import com.nikitos.main.debugger.BSODScreen;
import com.nikitos.platform.DesktopLauncher;
import com.nikitos.platformBridge.LauncherParams;


public class Main {
    public static void main(String[] args) {
        LauncherParams launcherParams = new LauncherParams()
                .setFullScreen(false)
                .setDebug(true)
                             .setStartPage(unused -> new BSODScreen("test error at 0x0001"));
                //.setStartPage(unused -> new MainRenderer());
        DesktopLauncher desktopLauncher = new DesktopLauncher(launcherParams);
        desktopLauncher.run();
    }
}