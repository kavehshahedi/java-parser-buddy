package jpb;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.security.MessageDigest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.javaparser.JavaToken;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.StringLiteralExpr;

public class JavaParserBuddy {

    public class Method {
        private String signature;
        private List<String> tokens;
        private String hash;

        public Method(String signature, List<String> tokens, String hash){
            this.signature = signature;
            this.tokens = tokens;
            this.hash = hash;
        }

        public String getSignature(){
            return this.signature;
        }

        public List<String> getTokens(){
            return this.tokens;
        }

        public String getHash(){
            return this.hash;
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println(
                    "Please provide a valid option (-get-methods-hash or -convert-method-signature) and the required file path(s).");
            return;
        }

        // Set the language level to Java 21 for higher compatibility
        StaticJavaParser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);

        String option = args[0];

        switch (option) {
            case "-get-methods-hash":
                if (args.length != 2) {
                    System.out.println("Please provide a valid file path for the -get-methods-hash option.");
                    return;
                }
                new JavaParserBuddy().handleGetMethodsHash(args[1]);
                break;

            case "-convert-method-signature":
                if (args.length != 2) {
                    System.out.println(
                            "Please provide exactly one method signature for the -convert-method-signature option.");
                    return;
                }
                new JavaParserBuddy().handleConvertMethodSignature(args[1]);
                break;

            default:
                System.out.println("Invalid option. Use -get-methods-hash or -convert-method-signature.");
        }
    }

    private void handleGetMethodsHash(String filePath) {
        try {
            File file = new File(filePath);

            CompilationUnit cu = StaticJavaParser.parse(file);

            Map<String, String> hashes = getMethodsHash(cu);
            Map<String, List<String>> methodTokens = extractMethodTokens(cu);

            List<Method> methods = new ArrayList<>();
            for (Map.Entry<String, String> entry : hashes.entrySet()) {
                String signature = entry.getKey();
                String hash = entry.getValue();
                List<String> tokens = methodTokens.get(signature);
                methods.add(new Method(signature, tokens, hash));
            }

            ObjectMapper om = new ObjectMapper();
            om.enable(SerializationFeature.INDENT_OUTPUT);
            String json = om.writeValueAsString(methods);
            System.out.println(json);
        } catch (Exception e) {
            // Return an empty list
            System.out.println("{}");
        }
    }

    private void handleConvertMethodSignature(String originalMethodSignature) {
        try {
            String convertedMethodSignature = convertMethodSignature(originalMethodSignature);
            System.out.println(convertedMethodSignature);
        } catch (Exception e) {
            System.out.println("");
        }
    }

    private Map<String, String> getMethodsHash(CompilationUnit classUnit) {
        Map<String, String> methods = new HashMap<>();
        classUnit.findAll(MethodDeclaration.class).forEach(method -> {
            // methodName =
            // f'{node.type_parameters}-{node.return_type}-{node.name}-{node.parameters}'
            String methodSignature = method.getTypeAsString() + "-" + method.getNameAsString() + "-"
                    + method.getParameters();

            // Replace all the literals in the method's body with a X
            method.findAll(StringLiteralExpr.class).forEach(literal -> {
                literal.setString("X");
            });

            String methodBody = method.getBody().toString();
            methodBody = methodBody.replaceAll("\\s", "");
            methodBody = methodBody.replaceAll("\\n", "");
            methodBody = methodBody.toLowerCase();
            methodBody = methodBody.trim();
            methodBody = calculateStringHash(methodBody);
            methods.put(methodSignature, methodBody);
        });
        return methods;
    }

    private String convertedMethodSignature;

    private String convertMethodSignature(String methodSignature) {
        convertedMethodSignature = "";
        // Extract the full method name
        String fullMethodName = methodSignature.split("\\(")[0]
                .split(" ")[methodSignature.split("\\(")[0].split(" ").length - 1];
        // Just keep the short method name
        String shortMethodName = fullMethodName.substring(fullMethodName.lastIndexOf('.') + 1);
        // Replace the full method name with the short method name
        methodSignature = methodSignature.replace(fullMethodName, shortMethodName);
        // Check if method signature ends with {}
        if (!methodSignature.replace("\\s", "").trim().endsWith("{}"))
            methodSignature += "{}";
        // Wrap the code in a class
        String wrappedCode = "public class A { " + methodSignature + " }";

        CompilationUnit cu = StaticJavaParser.parse(wrappedCode);
        cu.findFirst(MethodDeclaration.class).ifPresent(method -> {
            convertedMethodSignature = method.getTypeAsString() + "-" + method.getNameAsString() + "-"
                    + method.getParameters();
        });

        return convertedMethodSignature;
    }

    private String calculateStringHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private HashMap<String, List<String>> extractMethodTokens(CompilationUnit classUnit) {
        HashMap<String, List<String>> methodTokens = new HashMap<>();

        classUnit.findAll(MethodDeclaration.class).forEach(method -> {
            if (method.getTokenRange().isPresent()) {
                String methodSignature = method.getTypeAsString() + "-" + method.getNameAsString() + "-"
                    + method.getParameters();

                TokenRange tokenRange = method.getTokenRange().get();
                List<String> tokens = new ArrayList<>();
    
                for (JavaToken token : tokenRange) {
                    if (token.getCategory().isWhitespace()) {
                        continue;
                    }
                    
                    tokens.add(token.getText());
                }
    
                methodTokens.put(methodSignature, tokens);
            }
        });

        return methodTokens;
    }

}