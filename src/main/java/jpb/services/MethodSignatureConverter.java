package jpb.services;

public class MethodSignatureConverter {

    public String convertMethodSignature(String methodSignature) {
        methodSignature = methodSignature.trim();
        methodSignature = methodSignature.replaceAll("^(public|private|protected)\\s+", "");
        methodSignature = methodSignature.replaceAll("^(final)\\s+", "");

        methodSignature = removeGenericParameters(methodSignature);

        String[] parts = methodSignature.split("\\s+", 2);
        String returnType = parts[0];
        String methodNameAndParams = parts.length > 1 ? parts[1] : "";

        int openParen = methodNameAndParams.indexOf('(');
        String methodName = methodNameAndParams.substring(0, openParen);
        methodName = methodName.substring(methodName.lastIndexOf('.') + 1);

        String params = methodNameAndParams.substring(openParen);

        params = simplifyFullyQualifiedNames(params);
        returnType = simplifyFullyQualifiedNames(returnType);

        return returnType + "-" + methodName + "-" + params;
    }

    private String simplifyFullyQualifiedNames(String input) {
        StringBuilder simplified = new StringBuilder();
        StringBuilder current = new StringBuilder();
        int depth = 0;

        for (char c : input.toCharArray()) {
            if (c == '<') depth++;
            else if (c == '>') depth--;

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

        if (trimmed.endsWith("...")) {
            return name;
        }

        int lastDot = trimmed.lastIndexOf('.');
        return lastDot != -1 ? trimmed.substring(lastDot + 1) : trimmed;
    }

    private String removeGenericParameters(String input) {
        StringBuilder result = new StringBuilder();
        int depth = 0;
        for (char c : input.toCharArray()) {
            if (c == '<') depth++;
            else if (c == '>') depth--;
            else if (depth == 0) result.append(c);
        }
        return result.toString();
    }
}