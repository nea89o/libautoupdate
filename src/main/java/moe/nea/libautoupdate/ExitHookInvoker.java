package moe.nea.libautoupdate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExitHookInvoker {

    private static boolean isExitHookRegistered = false;
    private static List<UpdateAction> actions;
    private static File updaterJar;
    private static boolean cancelled = false;


    public static synchronized void setExitHook(File updaterJar, List<UpdateAction> actions) {
        if (!isExitHookRegistered) {
            Runtime.getRuntime().addShutdownHook(new Thread(ExitHookInvoker::runExitHook));

            isExitHookRegistered = true;
        }
        ExitHookInvoker.cancelled = false;
        ExitHookInvoker.actions = actions;
        ExitHookInvoker.updaterJar = updaterJar;
    }

    public static synchronized void cancelUpdate() {
        cancelled = true;
    }

    private static synchronized String[] buildInvocation() {
        boolean isWindows = System.getProperty("os.name", "").startsWith("Windows");
        File javaBinary = new File(System.getProperty("java.home"), "bin/java" + (isWindows ? ".exe" : ""));


        List<String> arguments = new ArrayList<>();
        arguments.add(javaBinary.getAbsolutePath());
        arguments.add("-jar");
        arguments.add(updaterJar.getAbsolutePath());

        for (UpdateAction action : actions) {
            action.encode(arguments);
        }

        return arguments.toArray(new String[0]);
    }

    private static void runExitHook() {
        try {
            if (cancelled) {
                System.out.println("Skipping cancelled update");
                return;
            }
            String[] invocation = buildInvocation();
            System.out.println("Running post updater using: " + String.join(" ", invocation));
            Runtime.getRuntime().exec(invocation);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


}
