package jpb.handlers;

import jpb.services.MethodSignatureConverter;

public class SignatureConversionHandler {

    private final MethodSignatureConverter converter;

    public SignatureConversionHandler() {
        this.converter = new MethodSignatureConverter();
    }

    public void handle(String originalMethodSignature) {
        String convertedMethodSignature = converter.convertMethodSignature(originalMethodSignature);
        System.out.println(convertedMethodSignature);
    }
}