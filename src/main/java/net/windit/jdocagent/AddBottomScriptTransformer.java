package net.windit.jdocagent;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class AddBottomScriptTransformer implements ClassFileTransformer {

    private static final String className = "jdk.javadoc.internal.doclets.formats.html.HtmlDocletWriter";
    private static final String code = """
            {
            return jdk.javadoc.internal.doclets.formats.html.markup.HtmlTree.SCRIPT(replaceDocRootDir("{@docRoot}%s"));
            }
            """;

    private String scriptFile;

    public AddBottomScriptTransformer() {
        scriptFile = System.getenv("BOTTOM_SCRIPT_FILE");
        if (scriptFile == null) {
            scriptFile = "";
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String name, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (scriptFile.isEmpty()) {
            return null;
        }
        name = name.replace("/", ".");
        if (!name.equals(className)) {
            return null;
        }

        System.out.println("------------------------");
        System.out.println("transforming " + name);
        try {
            var clazz = ClassPool.getDefault().get(name);
            var method = clazz.getDeclaredMethod("getFooter");
            method.setBody(String.format(code, scriptFile));
            System.out.println("transform done");
            System.out.println("------------------------");
            return clazz.toBytecode();
        } catch (NotFoundException | CannotCompileException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
