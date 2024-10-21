package jpb.handlers;

import jpb.services.MethodExtractor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GetMethodHandler {

    private final MethodExtractor methodExtractor;

    public GetMethodHandler() {
        this.methodExtractor = new MethodExtractor();
    }

    public void handle(String filePath, String methodSignature) throws IOException {
        String javaCode = new String(Files.readAllBytes(Paths.get(filePath)));
        String result = methodExtractor.getMethod(javaCode, methodSignature);
        System.out.println(result);
    }
}