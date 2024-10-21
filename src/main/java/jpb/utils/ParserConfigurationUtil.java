package jpb.utils;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;

public class ParserConfigurationUtil {

    public static void configureParser() {
        StaticJavaParser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
    }
}