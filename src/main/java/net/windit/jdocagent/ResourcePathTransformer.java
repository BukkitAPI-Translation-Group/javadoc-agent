package net.windit.jdocagent;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class ResourcePathTransformer implements ClassFileTransformer {

    private final String className = "jdk.javadoc.internal.doclets.formats.html.markup.Head";
    private String version;

    public ResourcePathTransformer() {
        version = System.getenv("VERSION");
        if (version == null) {
            version = "";
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String name, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (version.isEmpty()) {
            return null;
        }
        name = name.replace("/", ".");
        if (className.equals(name)) {
            try {
                System.out.println("------------------------");
                System.out.println("transforming " + className);

                CtClass clazz = ClassPool.getDefault().get(name);
                CtMethod method = clazz.getDeclaredMethod("addScripts");
                method.instrument(new ExprEditor() {
                    String lastCall = "";
                    boolean continues = true;

                    @Override
                    public void edit(MethodCall m) throws CannotCompileException {
                        if (!continues) {
                            return;
                        }

                        if ("SCRIPT".equals(m.getMethodName()) && "getPath".equals(lastCall)) {

                            m.replace(String.format("$1=$1+\"?ver=%s\";$_=$proceed($$);", version));
                            System.out.println("replace done");
                            continues = false;
                        }
                        if (continues) {
                            lastCall = m.getMethodName();
                        }
                    }
                });
                CtMethod method2 = clazz.getDeclaredMethod("addStylesheet");
                method2.insertBefore(String.format("{if (!stylesheet.equals(jdk.javadoc.internal.doclets.toolkit.util.DocPaths.JQUERY_FILES.resolve(jdk.javadoc.internal.doclets.toolkit.util.DocPaths.JQUERY_UI_CSS))){" +
                        "jdk.javadoc.internal.doclets.formats.html.markup.HtmlTree link = jdk.javadoc.internal.doclets.formats.html.markup.HtmlTree.LINK(\"stylesheet\", \"text/css\", pathToRoot.resolve(stylesheet).getPath()+\"?ver=%s\", \"Style\");" +
                        "if (stylesheet.equals(jdk.javadoc.internal.doclets.toolkit.util.DocPaths.STYLESHEET)){link.put(jdk.javadoc.internal.doclets.formats.html.markup.HtmlAttr.ID, \"doc-style\");}" +
                        "tree.add(link);" +
                        "return;}}", version));

                System.out.println("transform done");
                System.out.println("------------------------");
                return clazz.toBytecode();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
}
