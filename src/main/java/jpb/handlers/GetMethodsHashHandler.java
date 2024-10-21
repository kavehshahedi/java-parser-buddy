package jpb.handlers;

import com.github.javaparser.ast.CompilationUnit;
import jpb.models.Method;
import jpb.services.MethodExtractor;
import jpb.services.MethodHashCalculator;
import jpb.utils.JsonUtil;
import jpb.utils.ParserUtil;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GetMethodsHashHandler {

    private final MethodExtractor methodExtractor;
    private final MethodHashCalculator methodHashCalculator;

    public GetMethodsHashHandler() {
        this.methodExtractor = new MethodExtractor();
        this.methodHashCalculator = new MethodHashCalculator();
    }

    public void handle(String filePath) throws IOException {
        CompilationUnit cu = ParserUtil.parseFile(Paths.get(filePath));
        Map<String, String> hashes = methodHashCalculator.calculateMethodHashes(cu);
        Map<String, List<String>> methodTokens = methodExtractor.extractMethodTokens(cu);

        List<Method> methods = hashes.entrySet().stream()
                .map(entry -> new Method(entry.getKey(), methodTokens.get(entry.getKey()), entry.getValue()))
                .collect(Collectors.toList());

        System.out.println(JsonUtil.toJson(methods));
    }
}