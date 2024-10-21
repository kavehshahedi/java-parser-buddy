package jpb.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.IOException;
import java.nio.file.Path;

public class ParserUtil {

    public static CompilationUnit parseFile(Path filePath) throws IOException {
        return StaticJavaParser.parse(filePath);
    }

    public static CompilationUnit parseCode(String code) {
        return StaticJavaParser.parse(code);
    }
}