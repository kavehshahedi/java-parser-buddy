package jpb;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import com.github.javaparser.ast.comments.Comment;

public class JavaParserBuddy {

    public class Method {
        private String signature;
        private List<String> tokens;
        private String hash;

        public Method(String signature, List<String> tokens, String hash) {
            this.signature = signature;
            this.tokens = tokens;
            this.hash = hash;
        }

        public String getSignature() {
            return this.signature;
        }

        public List<String> getTokens() {
            return this.tokens;
        }

        public String getHash() {
            return this.hash;
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println(
                    "Please provide a valid option (-get-methods-hash, -convert-method-signature, or -get-method) and the required file path(s).");
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

            case "-get-method":
                if (args.length != 3) {
                    System.out.println("Please provide a valid file path and method signature for the -get-method option.");
                    return;
                }
                new JavaParserBuddy().handleGetMethod(args[1], args[2]);
                break;

            default:
                System.out.println("Invalid option. Use -get-methods-hash, -convert-method-signature, or -get-method.");
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

    private void handleGetMethod(String filePath, String methodSignature) {
        try {
            String javaCode = new String(Files.readAllBytes(Paths.get(filePath)));
            String result = getMethod(javaCode, methodSignature);
            System.out.println(result);
        } catch (Exception e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    private Map<String, String> getMethodsHash(CompilationUnit classUnit) {
        Map<String, String> methods = new HashMap<>();
        classUnit.findAll(MethodDeclaration.class).forEach(method -> {
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

    private String convertMethodSignature(String methodSignature) {
        // Remove 'public' or other modifiers if present
        methodSignature = methodSignature.replaceAll("^(public|private|protected)\\s+", "");
    
        // Split the signature into return type and the rest
        String[] parts = methodSignature.split("\\s+", 2);
        String returnType = parts[0];
        String methodNameAndParams = parts.length > 1 ? parts[1] : "";
    
        // Handle cases where the method name includes the full package path
        int openParen = methodNameAndParams.indexOf('(');
        String methodName = methodNameAndParams.substring(0, openParen);
        
        // Extract just the method name without package
        methodName = methodName.substring(methodName.lastIndexOf('.') + 1);
    
        // Extract parameters
        String params = methodNameAndParams.substring(openParen);
    
        // Simplify fully qualified type names in parameters and return type
        params = simplifyFullyQualifiedNames(params);
        returnType = simplifyFullyQualifiedNames(returnType);
    
        // Remove generic type parameters
        params = removeGenericParameters(params);
        returnType = removeGenericParameters(returnType);
    
        // Construct the converted signature
        return returnType + "-" + methodName + "-" + params;
    }
    
    private String simplifyFullyQualifiedNames(String input) {
        StringBuilder simplified = new StringBuilder();
        int depth = 0;
        StringBuilder current = new StringBuilder();
    
        for (char c : input.toCharArray()) {
            if (c == '<') {
                depth++;
            } else if (c == '>') {
                depth--;
            }
    
            if ((c == ',' || c == '(' || c == ')' || c == '>') && depth == 0) {
                if (current.length() > 0) {
                    simplified.append(simplifyName(current.toString()));
                    current = new StringBuilder();
                }
                simplified.append(c);
            } else {
                current.append(c);
            }
        }
    
        if (current.length() > 0) {
            simplified.append(simplifyName(current.toString()));
        }
    
        return simplified.toString();
    }
    
    private String simplifyName(String name) {
        String trimmed = name.trim();
        int lastDot = trimmed.lastIndexOf('.');
        return lastDot != -1 ? trimmed.substring(lastDot + 1) : trimmed;
    }
    
    private String removeGenericParameters(String input) {
        StringBuilder result = new StringBuilder();
        int depth = 0;
        for (char c : input.toCharArray()) {
            if (c == '<') {
                depth++;
            } else if (c == '>') {
                depth--;
            } else if (depth == 0) {
                result.append(c);
            }
        }
        return result.toString();
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

    public String getMethod(String javaCode, String methodSignature) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(javaCode);
            
            String convertedSignature = convertMethodSignature(methodSignature);
            
            for (MethodDeclaration method : cu.findAll(MethodDeclaration.class)) {
                String currentSignature = method.getDeclarationAsString(true, false, false);
                String convertedCurrentSignature = convertMethodSignature(currentSignature);
                
                if (convertedCurrentSignature.equals(convertedSignature)) {
                    // Remove comments
                    method.getAllContainedComments().forEach(Comment::remove);
                    
                    // Replace string literals
                    method.walk(StringLiteralExpr.class, s -> s.setString("X"));
                    
                    // Get the formatted code
                    String formattedCode = method.toString();
                    
                    return formattedCode;
                }
            }
            
            return "not-found";
        } catch (Exception e) {
            return "error";
        }
    }
}