package jpb.models;

import java.util.List;

public class Method {
    private final String signature;
    private final List<String> tokens;
    private final String hash;

    public Method(String signature, List<String> tokens, String hash) {
        this.signature = signature;
        this.tokens = tokens;
        this.hash = hash;
    }

    public String getSignature() {
        return signature;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public String getHash() {
        return hash;
    }
}