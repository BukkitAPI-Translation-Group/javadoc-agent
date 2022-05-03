package net.windit.jdocagent;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.charset.StandardCharsets;
import java.security.ProtectionDomain;
import java.util.Properties;

public class L10NTransformer implements ClassFileTransformer {

    private static final String[] docletsPatch = {"jdk.javadoc.internal.doclets.toolkit.resources.doclets_zh_CN", System.getenv("L10N_DOCLETS_PATCH_FILE")};
    private static final String[] standardPatch = {"jdk.javadoc.internal.doclets.formats.html.resources.standard_zh_CN", System.getenv("L10N_STANDARD_PATCH_FILE")};

    private static final String insertCode = """
            {
                $_ = net.windit.jdocagent.L10NTransformer.addPatchEntries($_, "%s");
            }
            """;

    @Override
    public byte[] transform(ClassLoader loader, String name, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        name = name.replace("/", ".");
        if (name.equals(docletsPatch[0]) || name.equals(standardPatch[0])) {
            String filename;
            if (name.equals(docletsPatch[0])) {
                filename = docletsPatch[1];
            } else {
                filename = standardPatch[1];
            }
            if (filename == null) {
                return null;
            }
            System.out.println("------------------------");
            System.out.println("transforming " + name);
            try {
                var clazz = ClassPool.getDefault().get(name);
                var method = clazz.getDeclaredMethod("getContents");
                method.insertAfter(String.format(insertCode, filename));
                System.out.println("transform done");
                System.out.println("------------------------");
                return clazz.toBytecode();
            } catch (NotFoundException | CannotCompileException | IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public static Object[][] addPatchEntries(Object[][] orig, String fileName) {
        if (fileName == null) {
            return orig;
        }
        var file = new File(fileName);
        if (!file.exists()) {
            return orig;
        }
        try (var reader = new FileReader(file, StandardCharsets.UTF_8)) {
            var properties = new Properties();
            properties.load(reader);
            int len = orig.length;
            int patchEntries = properties.size();
            var mod = new Object[len + patchEntries][2];
            System.arraycopy(orig, 0, mod, 0, len);
            var index = len;
            for (var entry : properties.entrySet()) {
                var k = entry.getKey();
                var v = entry.getValue();
                mod[index][0] = k;
                mod[index][1] = v;
                index++;
            }
            return mod;
        } catch (IOException e) {
            e.printStackTrace();
            return orig;
        }
    }
}
