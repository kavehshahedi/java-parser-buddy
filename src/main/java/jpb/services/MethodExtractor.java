package jpb.services;

import com.github.javaparser.JavaToken;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import jpb.utils.ParserUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodExtractor {

    private final MethodSignatureConverter signatureConverter;

    public MethodExtractor() {
        this.signatureConverter = new MethodSignatureConverter();
    }

    public Map<String, List<String>> extractMethodTokens(CompilationUnit classUnit) {
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
            CompilationUnit cu = ParserUtil.parseCode(javaCode);
            String convertedSignature = signatureConverter.convertMethodSignature(methodSignature);

            return cu.findAll(MethodDeclaration.class).stream()
                    .filter(method -> signatureConverter.convertMethodSignature(method.getDeclarationAsString(true, false, false))
                            .equals(convertedSignature))
                    .findFirst()
                    .map(method -> {
                        method.getAllContainedComments().forEach(com.github.javaparser.ast.comments.Comment::remove);
                        method.walk(StringLiteralExpr.class, s -> s.setString("X"));
                        return method.toString();
                    })
                    .orElse("not-found");
        } catch (Exception e) {
            return "error";
        }
    }
}