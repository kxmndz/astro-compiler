package astro;

/**
 * A single token produced by the Astro scanner.
 *
 * Holds:
 *   type   - what kind of token this is (from the TokenType enum)
 *   lexeme - the exact text from the source file
 *   line   - the line number where this token starts (1-based)
 *   column - the column number where this token starts (1-based)
 *
 * The toString() method prints the token in the format required by the spec:
 *   - Reserved words and operators  → [TOKEN_TYPE_NAME]   e.g. [T_WHEN]
 *   - Identifiers                   → lexeme only         e.g. age
 *   - Literal constants             → actual value        e.g. 25  or  "Hello"
 *   - EOF                           → [EOF]
 *   - Error                         → [ERROR: <lexeme>]
 */
public class Token {

    // ── Fields ─────────────────────────────────────────────────────────────────
    public final TokenType type;
    public final String    lexeme;
    public final int       line;
    public final int       column;

    // ── Constructor ────────────────────────────────────────────────────────────
    public Token(TokenType type, String lexeme, int line, int column) {
        this.type   = type;
        this.lexeme = lexeme;
        this.line   = line;
        this.column = column;
    }

    // ── Print format (required by spec) ───────────────────────────────────────
    /**
     * Returns the token in the format the tester must print.
     *
     * Rule summary from the requirements doc:
     *   Whitespace        → not printed at all (never reaches here)
     *   Reserved words    → [TOKEN_TYPE_NAME]
     *   Operators         → [TOKEN_TYPE_NAME]
     *   Identifiers       → lexeme  (e.g.  age  or  numStudents)
     *   Constant values   → actual value  (e.g.  42  or  "Hello"  or  true)
     *   EOF               → [EOF]
     *   Error             → [ERROR: <bad text>]
     */
    @Override
    public String toString() {
        switch (type) {

            // ── Identifiers: print the name as-is ─────────────────────────────
            case T_IDENTIFIER:
                return lexeme;

            // ── Literals: print the actual value ──────────────────────────────
            case T_NUMBER_LITERAL:
            case T_STRING_LITERAL:
            case T_CHARACTER_LITERAL:
            case T_BOOLEAN_LITERAL:
            case T_NULL_LITERAL:
                return lexeme;

            // ── EOF ────────────────────────────────────────────────────────────
            case T_EOF:
                return "[EOF]";

            // ── Errors ─────────────────────────────────────────────────────────
            case T_ERROR:
                return "[ERROR: " + lexeme + "]";

            // ── Everything else (reserved words, operators, delimiters) ────────
            // Print the token type name in square brackets, e.g. [T_WHEN]
            default:
                return "[" + type.name() + "]";
        }
    }

    /**
     * A detailed format used for the token list printout.
     * Shows type, lexeme, line, and column aligned in columns.
     *
     * Example:
     *   T_ALIGN              align                line 3   col 5
     *   T_DATATYPE           Saturn               line 3   col 11
     *   T_IDENTIFIER         age                  line 3   col 18
     */
    public String toDetailString() {
        return String.format("%-28s %-20s line %-5d col %d",
                type.name(),
                lexeme,
                line,
                column);
    }
}
