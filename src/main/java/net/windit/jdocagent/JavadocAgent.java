package net.windit.jdocagent;

import java.lang.instrument.Instrumentation;

public class JavadocAgent {
    public static void premain(String args, Instrumentation inst) {
        System.out.println("working directory:" + System.getProperty("user.dir"));
        System.out.println("env:VERSION=" + System.getenv("VERSION"));
        System.out.println("env:L10N_DOCLETS_PATCH_FILE:" + System.getenv("L10N_DOCLETS_PATCH_FILE"));
        System.out.println("env:L10N_STANDARD_PATCH_FILE:" + System.getenv("L10N_STANDARD_PATCH_FILE"));
        System.out.println("env:BOTTOM_SCRIPT_FILE:" + System.getenv("BOTTOM_SCRIPT_FILE"));
        System.out.println("starting agent...");
        inst.addTransformer(new ResourcePathTransformer());
        inst.addTransformer(new L10NTransformer());
        inst.addTransformer(new AddBottomScriptTransformer());
        System.out.println("added transformers");
    }
}
