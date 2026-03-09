package astro;

import java.util.List;

/**
 * Astro Scanner Tester - Phase 2
 *
 * Usage:
 * java astro.Main <path_to_source_file>
 *
 * What this does, in order:
 * 1. Reads the entire source file into a String.
 * 2. Creates a Scanner and a SymbolTable.
 * 3. Calls scanner.nextToken() once per token in a loop.
 * 4. For each token returned:
 * - If it is T_IDENTIFIER, adds it to the symbol table (if not there yet).
 * - If it is T_ERROR, the scanner already printed the error message.
 * The tester prints the error token so it appears in the token list too.
 * - If it is T_EOF, prints it and stops the loop.
 * - Otherwise, prints the token using Token.toDetailString().
 * 5. After the loop, prints the final symbol table.
 */
public class Main {

    // Define ANSI escape codes as constants
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_BLUE = "\u001B[34m";

    public static void main(String[] args) {

        // ── Step 1: Check that a filename was provided ────────────────────────
        if (args.length < 1) {
            System.out.println("[ERROR] No source file provided.");
            System.out.println("Usage: java astro.Main <source_file>");
            System.out.println("Example: java astro.Main test/valid_program.astro");
            return;
        }

        // ── Step 2: Read the source file ──────────────────────────────────────
        String filePath = args[0];

        // ── Step 3: Create the scanner ───────────────────────
        Scanner scanner = new Scanner(filePath);

        // ── Step 4: Print the header ──────────────────────────────────────────
        printHeader(filePath);

        // ── Step 5: Token loop ────────────────────────────────────────────────
        //
        // The tester makes ONE request per token to the scanner.
        // It keeps requesting until T_EOF is returned.

        int goodTokenCount = 0;
        int errorTokenCount = 0;

        while (true) {
            Token token = scanner.nextToken();

            // ── T_EOF: print it and stop ──────────────────────────────────────
            if (token.type == TokenType.T_EOF) {
                System.out.printf("%-28s %-20s line %-5d col %d%n",
                        token.type.name(),
                        token.lexeme,
                        token.line,
                        token.column);
                break;
            }

            // ── T_ERROR: scanner already printed the error message ────────────
            // The tester still prints the error token in the token list
            // so the output shows exactly where the bad token appeared.
            if (token.type == TokenType.T_ERROR) {
                System.out.printf("%s%-28s %-20s line %-5d col %d%s%n",
                        ANSI_RED,
                        "*** ERROR ***",
                        token.lexeme,
                        token.line,
                        token.column,
                        ANSI_RESET);
                errorTokenCount++;
                continue;
            }

            // ── T_IDENTIFIER: add to symbol table if new ─────────────────────
            if (token.type == TokenType.T_IDENTIFIER) {
                // Print the token - mark new identifiers with (new) for clarity
                System.out.printf("%s%-28s%s %s%-20s%s line %-5d col %-5d %n",
                        ANSI_BLUE,
                        token.type.name(),
                        ANSI_RESET,
                        ANSI_GREEN,
                        token.lexeme,
                        ANSI_RESET,
                        token.line,
                        token.column);
                goodTokenCount++;
                continue;
            }

            // ── All other good tokens ─────────────────────────────────────────
            // Print using the format from the requirements doc:
            // Reserved words / operators → [TOKEN_TYPE_NAME]
            // Literals → actual value
            System.out.printf("%s%-28s%s %-20s line %-5d col %d%n",
                    ANSI_BLUE,
                    token.type.name(),
                    ANSI_RESET,
                    token.lexeme,
                    token.line,
                    token.column);
            goodTokenCount++;
        }

        // ── Step 6: Print summary ─────────────────────────────────────────────
        printSummary(goodTokenCount, errorTokenCount);
        if (errorTokenCount > 0) {
            printErrorSummary(scanner.getErrorSummary());
        }

        // ── Step 7: Print the symbol table ───────────────────────────────────
        scanner.getSymbolTable().print();
    }

    // ── Helper: print the output header ──────────────────────────────────────

    private static void printHeader(String filePath) {
        System.out.println();
        System.out.println("==========================================================");
        System.out.println("  ASTRO LANGUAGE SCANNER - Phase 2");
        System.out.println("  Source file: " + filePath);
        System.out.println("==========================================================");
        System.out.printf("%-28s %-20s %s%n", "TOKEN TYPE", "LEXEME / VALUE", "POSITION");
        System.out.println("----------------------------------------------------------");
    }

    // ── Helper: print the token count summary ────────────────────────────────

    private static void printSummary(int good, int errors) {
        System.out.println("----------------------------------------------------------");
        System.out.println("  Tokens recognized : " + good);
        System.out.println("  Errors found      : " + errors);
        System.out.println("==========================================================");
    }

    // ── Helper: print the error summary ─────────────────────────────────────
    private static void printErrorSummary(List<String> errorSummary) {
        System.out.println();
        System.out.println("==========================================================");
        System.out.println("  ERROR SUMMARY");
        System.out.println("==========================================================");
        for (String error : errorSummary) {
            if (error != null) {
                System.out.print(error);
            }
        }
        System.out.println("==========================================================");
    }
}
