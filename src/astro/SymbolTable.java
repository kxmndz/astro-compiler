package astro;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Symbol table for the Astro scanner.
 *
 * Stores one entry per unique identifier found in the source program.
 * The tester adds an entry whenever the scanner returns a T_IDENTIFIER token
 * that is not already in the table.
 *
 * Phase 2: stores lexeme + token type only.
 * Phase 3+: dataType and value will be filled in by the parser / interpreter.
 */
public class SymbolTable {

    // ── Inner class: one row in the table ──────────────────────────────────────
    public static class SymbolEntry {

        public final String    lexeme;     // the identifier name, e.g. "age"
        public final TokenType tokenType;  // always T_IDENTIFIER in phase 2
        public String          dataType;   // e.g. "Saturn" — filled in later phases
        public String          value;      // e.g. "25"     — filled in later phases

        public SymbolEntry(String lexeme, TokenType tokenType) {
            this.lexeme    = lexeme;
            this.tokenType = tokenType;
            this.dataType  = null;
            this.value     = null;
        }

        @Override
        public String toString() {
            return String.format("%-20s %-15s %-12s %s",
                    lexeme,
                    tokenType.name(),
                    dataType == null ? "unknown" : dataType,
                    value    == null ? "unknown" : value);
        }
    }

    // ── The table itself ───────────────────────────────────────────────────────
    // LinkedHashMap preserves insertion order so the printout is predictable.
    private final Map<String, SymbolEntry> table = new LinkedHashMap<>();

    // ── Public methods ─────────────────────────────────────────────────────────

    /**
     * Adds a new identifier to the table if it is not already present.
     * If it is already present, does nothing.
     * Either way, returns the entry for that lexeme.
     *
     * Call this from the tester every time the scanner returns T_IDENTIFIER.
     */
    public SymbolEntry put(String lexeme, TokenType type) {
        if (!table.containsKey(lexeme)) {
            table.put(lexeme, new SymbolEntry(lexeme, type));
        }
        return table.get(lexeme);
    }

    /**
     * Returns true if the identifier is already in the table.
     */
    public boolean contains(String lexeme) {
        return table.containsKey(lexeme);
    }

    /**
     * Retrieves the entry for a given lexeme, or null if not found.
     */
    public SymbolEntry get(String lexeme) {
        return table.get(lexeme);
    }

    /**
     * Returns how many unique identifiers are stored.
     */
    public int size() {
        return table.size();
    }

    /**
     * Prints the full symbol table to the console.
     * Called by the tester after all tokens have been processed.
     */
    public void print() {
        System.out.println();
        System.out.println("==========================================================");
        System.out.println("  SYMBOL TABLE  (" + table.size() + " identifier(s) found)");
        System.out.println("==========================================================");
        System.out.printf("%-20s %-15s %-12s %s%n",
                "LEXEME", "TOKEN TYPE", "DATA TYPE", "VALUE");
        System.out.println("----------------------------------------------------------");

        if (table.isEmpty()) {
            System.out.println("  (no identifiers found)");
        } else {
            for (SymbolEntry entry : table.values()) {
                System.out.println("  " + entry);
            }
        }

        System.out.println("==========================================================");
        System.out.println();
    }
}
