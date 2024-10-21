package jpb.services;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import jpb.utils.HashUtil;

import java.util.Map;
import java.util.stream.Collectors;

public class MethodHashCalculator {

    public Map<String, String> calculateMethodHashes(CompilationUnit classUnit) {
        return classUnit.findAll(MethodDeclaration.class).stream()
                .collect(Collectors.toMap(
                        this::getMethodSignature,
                        method -> {
                            method.findAll(StringLiteralExpr.class).forEach(literal -> literal.setString("X"));
                            String methodBody = method.getBody().map(body -> body.toString()
                                    .replaceAll("\\s", "")
                                    .toLowerCase()
                                    .trim()).orElse("");
                            return HashUtil.calculateMD5Hash(methodBody);
                        }
                ));
    }

    private String getMethodSignature(MethodDeclaration method) {
        return String.format("%s-%s-%s", method.getTypeAsString(), method.getNameAsString(), method.getParameters());
    }
}