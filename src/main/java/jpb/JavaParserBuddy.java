package jpb;

import jpb.handlers.GetMethodHandler;
import jpb.handlers.GetMethodsHashHandler;
import jpb.handlers.SignatureConversionHandler;
import jpb.utils.ParserConfigurationUtil;

public class JavaParserBuddy {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please provide a valid option (-get-methods-hash, -convert-method-signature, or -get-method) and the required file path(s).");
            return;
        }

        ParserConfigurationUtil.configureParser();

        String option = args[0];
        try {
            switch (option) {
                case "-get-methods-hash":
                    new GetMethodsHashHandler().handle(args[1]);
                    break;
                case "-convert-method-signature":
                    new SignatureConversionHandler().handle(args[1]);
                    break;
                case "-get-method":
                    new GetMethodHandler().handle(args[1], args[2]);
                    break;
                default:
                    System.out.println("Invalid option. Use -get-methods-hash, -convert-method-signature, or -get-method.");
                    break;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Insufficient arguments provided for the selected option.");
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }
}