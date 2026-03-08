package astro;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Astro Language Scanner — Phase 2
 *
 * Reads source code one character at a time.
 * Returns one Token per call to nextToken().
 *
 * The tester calls nextToken() in a loop until T_EOF is returned.
 * Whitespace and comments are consumed silently — they are never returned.
 * Errors are printed immediately and returned as T_ERROR tokens so the
 * tester can keep going and find more errors in the same file.
 */
public class Scanner {

    // =========================================================================
    // SECTION 1: Reserved word table
    // =========================================================================
    //
    // Maps the exact keyword string → its TokenType.
    // Checked at the end of scanIdentifierOrKeyword().
    // If a word is in this table it is a keyword; otherwise it is T_IDENTIFIER.
    //
    // Important: checked BEFORE returning T_IDENTIFIER, so "true" and "false"
    // become T_BOOLEAN_LITERAL and "null" becomes T_NULL_LITERAL automatically.

    private static final Map<String, TokenType> RESERVED = new LinkedHashMap<>();

    static {
        // Program structure
        RESERVED.put("chart", TokenType.T_PROGRAM_START);

        // Data type keywords
        RESERVED.put("Saturn", TokenType.T_DATATYPE);
        RESERVED.put("Moon", TokenType.T_DATATYPE);
        RESERVED.put("Mercury", TokenType.T_DATATYPE);
        RESERVED.put("Uranus", TokenType.T_DATATYPE);
        RESERVED.put("Mars", TokenType.T_DATATYPE);
        RESERVED.put("Venus", TokenType.T_DATATYPE);
        RESERVED.put("Neptune", TokenType.T_DATATYPE);
        RESERVED.put("Jupiter", TokenType.T_DATATYPE);
        RESERVED.put("Pluto", TokenType.T_DATATYPE);

        // Declaration keywords
        RESERVED.put("align", TokenType.T_ALIGN);
        RESERVED.put("manifests", TokenType.T_MANIFESTS);
        RESERVED.put("constellation", TokenType.T_ARRAY_MARKER);
        RESERVED.put("refine", TokenType.T_REFINE);

        // Control flow keywords
        RESERVED.put("when", TokenType.T_WHEN);
        RESERVED.put("else", TokenType.T_ELSE);
        RESERVED.put("orbit", TokenType.T_ORBIT);
        RESERVED.put("cycle", TokenType.T_CYCLE);
        RESERVED.put("cast", TokenType.T_CAST);
        RESERVED.put("retrograde", TokenType.T_RETROGRADE);
        RESERVED.put("trace", TokenType.T_TRACE);
        RESERVED.put("path", TokenType.T_PATH);
        RESERVED.put("voidPath", TokenType.T_VOIDPATH);
        RESERVED.put("collapse", TokenType.T_COLLAPSE);
        RESERVED.put("skip", TokenType.T_SKIP);

        // I/O keywords
        RESERVED.put("summon", TokenType.T_SUMMON);
        RESERVED.put("radiate", TokenType.T_RADIATE);

        // Type conversion
        RESERVED.put("transmute", TokenType.T_TRANSMUTE);

        // Special literals — these look like identifiers but are reserved
        RESERVED.put("true", TokenType.T_BOOLEAN_LITERAL);
        RESERVED.put("false", TokenType.T_BOOLEAN_LITERAL);
        RESERVED.put("null", TokenType.T_NULL_LITERAL);
    }

    // =========================================================================
    // SECTION 2: State variables
    // =========================================================================
    //
    // The scanner holds the entire source as a String.
    // pos — index of the next character to read
    // line — current line number (1-based, for error messages)
    // col — current column number (1-based, for error messages)
    //
    // tokLine / tokCol — saved at the START of each token so the Token object
    // records where the token began, not where it ended.
    //
    // errorMessages — store error messages for a summary at the end

    private final String source;
    private int pos = 0;
    private int line = 1;
    private int col = 1;

    private int tokLine; // line where current token started
    private int tokCol; // column where current token started

    private List<String> errorMessages = new ArrayList<>();

    // =========================================================================
    // Constructor
    // =========================================================================

    public Scanner(String source) {
        this.source = source;
    }

    // =========================================================================
    // SECTION 3: nextToken() — the main public entry point
    // =========================================================================
    //
    // The tester calls this once per token.
    // Steps:
    // 1. Skip whitespace and comments
    // 2. If nothing left, return T_EOF
    // 3. Save the start position for this token
    // 4. Look at the first character and delegate to the right scanner method

    public Token nextToken() {
        // Step 1: skip whitespace and comments
        skipWhitespaceAndComments();

        // Step 2: end of file
        if (pos >= source.length()) {
            tokLine = line;
            tokCol = col;
            return makeToken(TokenType.T_EOF, "EOF");
        }

        // Step 3: mark where this token starts
        markTokenStart();

        // Step 4: branch on the first character
        char c = peek();

        if (c == '"')
            return scanStringLiteral();
        if (c == '\'')
            return scanCharLiteral();
        if (Character.isDigit(c))
            return scanNumber();
        if (Character.isLetter(c) || c == '_' || c == '$')
            return scanIdentifierOrKeyword();

        // Everything else: operators, delimiters, and error characters
        return scanOperatorOrDelimiter();
    }

    // =========================================================================
    // SECTION 4: Whitespace and comment skipping
    // =========================================================================
    //
    // Called at the start of every nextToken() call.
    // Consumes spaces, tabs, newlines, and both comment styles.
    // Nothing here is ever returned as a token.

    private void skipWhitespaceAndComments() {
        while (pos < source.length()) {
            char c = peek();

            // ── Plain whitespace ──────────────────────────────────────────────
            if (c == ' ' || c == '\t' || c == '\r') {
                advance();
                continue;
            }

            // ── Newline: advance line counter ─────────────────────────────────
            if (c == '\n') {
                advance();
                line++;
                col = 1;
                continue;
            }

            // ── Comments ──────────────────────────────────────────────────────
            // IMPORTANT: check ~~~ before ~~ (longest match)
            if (c == '~') {
                if (matchAhead("~~~")) {
                    consumeMultiLineComment();
                    continue;
                }
                if (matchAhead("~~")) {
                    consumeSingleLineComment();
                    continue;
                }
                // A single '~' is a lexical error — stop skipping,
                // let the operator scanner handle and report it.
                break;
            }

            // ── Not whitespace or comment — stop ─────────────────────────────
            break;
        }
    }

    // Consumes everything from after ~~ to end of line (or EOF).
    private void consumeSingleLineComment() {
        // "~~" already consumed by matchAhead()
        while (pos < source.length() && peek() != '\n') {
            advance();
        }
        // The '\n' itself is left for the main loop to consume so the
        // line counter is incremented correctly.
    }

    // Consumes everything from after the opening ~~~ to the closing ~~~.
    // Reports a lexical error if EOF is reached before the closing ~~~.
    private void consumeMultiLineComment() {
        // Opening "~~~" already consumed by matchAhead()
        int startLine = line;
        int startCol = col;

        while (pos < source.length()) {
            if (matchAhead("~~~")) {
                return; // found closing ~~~, done
            }
            if (peek() == '\n') {
                line++;
                col = 1;
            }
            advance();
        }

        // Reached EOF without finding closing ~~~
        printError(startLine, startCol,
                "Unterminated multi-line comment — missing closing '~~~'");
    }

    // =========================================================================
    // SECTION 5A: String literal scanner
    // =========================================================================
    //
    // Called when the current character is "
    // Consumes everything up to the matching closing "
    // Handles escape sequences: \n \t \\ \" \'
    // Reports error if newline or EOF is reached before closing "

    private Token scanStringLiteral() {
        StringBuilder sb = new StringBuilder();
        sb.append(advance()); // consume the opening "

        while (pos < source.length()) {
            char c = peek();

            // Newline before closing quote — unterminated string
            if (c == '\n') {
                printError(tokLine, tokCol,
                        "Unterminated string literal — missing closing '\"' before end of line");
                return makeToken(TokenType.T_ERROR, sb.toString());
            }

            // Closing quote — string is complete
            if (c == '"') {
                sb.append(advance()); // consume the closing "
                return makeToken(TokenType.T_STRING_LITERAL, sb.toString());
            }

            // Escape sequence
            if (c == '\\') {
                sb.append(advance()); // consume '\'
                if (pos >= source.length() || peek() == '\n') {
                    printError(tokLine, tokCol,
                            "Unterminated escape sequence in string literal");
                    return makeToken(TokenType.T_ERROR, sb.toString());
                }
                char esc = advance(); // consume the escape character
                switch (esc) {
                    case 'n':
                    case 't':
                    case '\\':
                    case '"':
                    case '\'':
                        sb.append(esc);
                        break;
                    default:
                        printError(tokLine, tokCol,
                                "Unknown escape sequence '\\" + esc + "' in string literal");
                        sb.append(esc);
                        break;
                }
                continue;
            }

            // Normal character
            sb.append(advance());
        }

        // EOF before closing quote
        printError(tokLine, tokCol,
                "Unterminated string literal — reached end of file");
        return makeToken(TokenType.T_ERROR, sb.toString());
    }

    // =========================================================================
    // SECTION 5B: Character literal scanner
    // =========================================================================
    //
    // Called when the current character is '
    // Valid forms: 'A' '\n' '\\'
    // Errors: unterminated, more than one character, empty ''

    private Token scanCharLiteral() {
        StringBuilder sb = new StringBuilder();
        sb.append(advance()); // consume the opening '

        // EOF or newline immediately — unterminated
        if (pos >= source.length() || peek() == '\n') {
            printError(tokLine, tokCol,
                    "Unterminated character literal");
            return makeToken(TokenType.T_ERROR, sb.toString());
        }

        // Empty character literal ''
        if (peek() == '\'') {
            sb.append(advance()); // consume closing '
            printError(tokLine, tokCol,
                    "Empty character literal — must contain exactly one character");
            return makeToken(TokenType.T_ERROR, sb.toString());
        }

        // Escape sequence
        if (peek() == '\\') {
            sb.append(advance()); // consume '\'
            if (pos >= source.length() || peek() == '\n') {
                printError(tokLine, tokCol,
                        "Unterminated escape sequence in character literal");
                return makeToken(TokenType.T_ERROR, sb.toString());
            }
            char esc = advance();
            switch (esc) {
                case 'n':
                case 't':
                case '\\':
                case '"':
                case '\'':
                    sb.append(esc);
                    break;
                default:
                    printError(tokLine, tokCol,
                            "Unknown escape sequence '\\" + esc + "' in character literal");
                    sb.append(esc);
                    break;
            }
        } else {
            // Normal single character
            sb.append(advance());
        }

        // Now we must see the closing '
        if (pos < source.length() && peek() == '\'') {
            sb.append(advance()); // consume closing '
            return makeToken(TokenType.T_CHARACTER_LITERAL, sb.toString());
        }

        // Missing closing quote — consume until we find ' or newline
        while (pos < source.length() && peek() != '\'' && peek() != '\n') {
            sb.append(advance());
        }
        if (pos < source.length() && peek() == '\'') {
            sb.append(advance());
            printError(tokLine, tokCol,
                    "Invalid character literal — more than one character: " + sb);
        } else {
            printError(tokLine, tokCol,
                    "Unterminated character literal: " + sb);
        }
        return makeToken(TokenType.T_ERROR, sb.toString());
    }

    // =========================================================================
    // SECTION 5C: Number literal scanner
    // =========================================================================
    //
    // Handles:
    // Decimal integer 42
    // Negative decimal -42 (negated is passed from operator scanner)
    // Float / double 3.14
    // Binary 0b1010
    // Octal 0o77
    // Hexadecimal 0xFF
    private Token scanNumber() {
        StringBuilder sb = new StringBuilder();

        // ── Non-decimal prefix check: 0b, 0o, 0x ─────────────────────────────
        // Only applies when the first digit is '0' and a prefix letter follows.
        if (peek() == '0' && pos + 1 < source.length()) {
            char prefix = source.charAt(pos + 1);
            if (prefix == 'b' || prefix == 'B')
                return scanBinaryLiteral(sb);
            if (prefix == 'o' || prefix == 'O')
                return scanOctalLiteral(sb);
            if (prefix == 'x' || prefix == 'X')
                return scanHexLiteral(sb);
        }

        // ── Decimal digits ────────────────────────────────────────────────────
        while (pos < source.length() && Character.isDigit(peek())) {
            sb.append(advance());
        }

        // ── Possible float: digit(s) then '.' then digit(s) ──────────────────
        if (pos < source.length() && peek() == '.') {
            // A digit must follow the dot for it to be a float
            if (pos + 1 < source.length()
                    && Character.isDigit(source.charAt(pos + 1))) {
                sb.append(advance()); // consume '.'
                while (pos < source.length() && Character.isDigit(peek())) {
                    sb.append(advance());
                }

                // Check for multiple decimal points
                // e.g. 3.14.159
                if (peek() == '.'
                        && pos + 1 < source.length()
                        && Character.isDigit(source.charAt(pos + 1))) {
                    // consume the rest of the bad number for the error message
                    sb.append(advance()); // consume second '.'
                    while (pos < source.length() && (Character.isDigit(peek()) || peek() == '.')) {
                        sb.append(advance());
                    }
                    printError(
                            tokLine, tokCol,
                            "Invalid floating-point literal — multiple decimal points: " + sb);
                    return makeToken(TokenType.T_ERROR, sb.toString());
                }

                // Check for illegal suffix like 4000.5f
                if (pos < source.length() && Character.isLetter(peek())) {
                    sb.append(advance()); // consume the bad character
                    printError(tokLine, tokCol,
                            "Invalid numeric literal — unsupported suffix: " + sb);
                    return makeToken(TokenType.T_ERROR, sb.toString());
                }

                return makeToken(TokenType.T_NUMBER_LITERAL, sb.toString());
            }
        }

        // ── Check for illegal letter suffix on integer e.g. 42abc ────────────
        if (pos < source.length() && Character.isLetter(peek())) {
            while (pos < source.length()
                    && (Character.isLetterOrDigit(peek())
                            || peek() == '_'
                            || peek() == '$')) {
                sb.append(advance());
            }
            printError(tokLine, tokCol,
                    "Invalid token — identifiers cannot start with a digit: " + sb);
            return makeToken(TokenType.T_ERROR, sb.toString());
        }

        return makeToken(TokenType.T_NUMBER_LITERAL, sb.toString());
    }

    private Token scanBinaryLiteral(StringBuilder sb) {
        sb.append(advance()); // '0'
        sb.append(advance()); // 'b'

        if (pos >= source.length() || (peek() != '0' && peek() != '1')) {
            printError(tokLine, tokCol,
                    "Invalid binary literal — no binary digits after '0b'");
            return makeToken(TokenType.T_ERROR, sb.toString());
        }

        while (pos < source.length() && (peek() == '0' || peek() == '1')) {
            sb.append(advance());
        }

        // Check for decimal point(s)
        if (peek() == '.'
                && (pos + 1 < source.length())
                && (source.charAt(pos + 1) == '0' || source.charAt(pos + 1) == '1')) {
            // consume the rest of the bad number for the error message
            sb.append(advance()); // .
            while (pos < source.length() && (peek() == '0' || peek() == '1' || peek() == '.')) {
                sb.append(advance());
            }
            printError(
                    tokLine, tokCol,
                    "Invalid binary literal — decimal point not allowed: " + sb);
            return makeToken(TokenType.T_ERROR, sb.toString());
        }

        // A digit that is not 0 or 1 right after is an error
        if (pos < source.length() && Character.isDigit(peek())) {
            sb.append(advance());
            printError(tokLine, tokCol,
                    "Invalid binary literal — digit not allowed in base-2: " + sb);
            return makeToken(TokenType.T_ERROR, sb.toString());
        }

        return makeToken(TokenType.T_NUMBER_LITERAL, sb.toString());
    }

    private Token scanOctalLiteral(StringBuilder sb) {
        sb.append(advance()); // '0'
        sb.append(advance()); // 'o'

        if (pos >= source.length() || peek() < '0' || peek() > '7') {
            printError(tokLine, tokCol,
                    "Invalid octal literal — no octal digits after '0o'");
            return makeToken(TokenType.T_ERROR, sb.toString());
        }

        while (pos < source.length() && peek() >= '0' && peek() <= '7') {
            sb.append(advance());
        }

        // Check for decimal point(s)
        if (peek() == '.'
                && (pos + 1 < source.length())
                && (source.charAt(pos + 1) >= '0' && source.charAt(pos + 1) <= '7')) {
            // consume the rest of the bad number for the error message
            sb.append(advance()); // .
            while (pos < source.length() && (peek() >= '0' && peek() <= '7' || peek() == '.')) {
                sb.append(advance());
            }
            printError(
                    tokLine, tokCol,
                    "Invalid octal literal — decimal point not allowed: " + sb);
            return makeToken(TokenType.T_ERROR, sb.toString());
        }

        // check for digits that are not valid in octal (8 or 9)
        if (pos < source.length() && Character.isDigit(peek())) {
            sb.append(advance());
            printError(tokLine, tokCol,
                    "Invalid octal literal — digit not allowed in base-8: " + sb);
            return makeToken(TokenType.T_ERROR, sb.toString());
        }

        return makeToken(TokenType.T_NUMBER_LITERAL, sb.toString());
    }

    private Token scanHexLiteral(StringBuilder sb) {
        sb.append(advance()); // '0'
        sb.append(advance()); // 'x'

        if (pos >= source.length() || !isHexDigit(peek())) {
            printError(tokLine, tokCol,
                    "Invalid hex literal — no hex digits after '0x'");
            return makeToken(TokenType.T_ERROR, sb.toString());
        }

        // Check for decimal point(s)
        if (peek() == '.'
                && (pos + 1 < source.length())
                && isHexDigit(source.charAt(pos + 1))) {
            // consume the rest of the bad number for the error message
            sb.append(advance()); // .
            while (pos < source.length() && (isHexDigit(peek()) || peek() == '.')) {
                sb.append(advance());
            }
            printError(
                    tokLine, tokCol,
                    "Invalid hex literal — decimal point not allowed: " + sb);
            return makeToken(TokenType.T_ERROR, sb.toString());
        }

        // check for non-hex digits
        while (pos < source.length() && isHexDigit(peek())) {
            sb.append(advance());
        }

        return makeToken(TokenType.T_NUMBER_LITERAL, sb.toString());
    }

    // =========================================================================
    // SECTION 5D: Identifier and keyword scanner
    // =========================================================================
    //
    // Called when the first character is a letter.
    // Consumes letters, digits, underscores, and dollar signs.
    // After building the lexeme, checks the reserved word table.
    // If found → return the reserved word token type.
    // If not → return T_IDENTIFIER.

    private Token scanIdentifierOrKeyword() {
        StringBuilder sb = new StringBuilder();

        while (pos < source.length()
                && (Character.isLetterOrDigit(peek())
                        || peek() == '_'
                        || peek() == '$')) {
            sb.append(advance());
        }

        String lexeme = sb.toString();

        // Table lookup — reserved word or identifier?
        TokenType type = RESERVED.getOrDefault(lexeme, TokenType.T_IDENTIFIER);

        return makeToken(type, lexeme);
    }

    // =========================================================================
    // SECTION 5E: Operator and delimiter scanner
    // =========================================================================
    //
    // Called when the first character is not a letter, digit, " or '
    // Uses longest-match: checks for two-character operators before
    // falling back to single-character ones.

    private Token scanOperatorOrDelimiter() {
        char c = advance(); // consume the current character

        switch (c) {

            // ── Single-character delimiters ───────────────────────────────────
            case '(':
                return makeToken(TokenType.T_LPAREN, "(");
            case ')':
                return makeToken(TokenType.T_RPAREN, ")");
            case '{':
                return makeToken(TokenType.T_BLOCK_START, "{");
            case '}':
                return makeToken(TokenType.T_BLOCK_END, "}");
            case '[':
                return makeToken(TokenType.T_LSQR, "[");
            case ']':
                return makeToken(TokenType.T_RSQR, "]");
            case ';':
                return makeToken(TokenType.T_STATEMENT_DELIMITER, ";");
            case ',':
                return makeToken(TokenType.T_SEPARATOR, ",");
            case ':':
                return makeToken(TokenType.T_COLON, ":");

            // ── Arithmetic: unambiguous single-character ──────────────────────
            case '+':
                return makeToken(TokenType.T_ADDITIVE_OPERATOR, "+");
            case '*':
                return makeToken(TokenType.T_MULTIPLICATIVE_OPERATOR, "*");
            case '/':
                return makeToken(TokenType.T_MULTIPLICATIVE_OPERATOR, "/");
            case '%':
                return makeToken(TokenType.T_MULTIPLICATIVE_OPERATOR, "%");

            // ── Minus: always treated as an operator by the scanner ───────────
            // negative literals will be handled by the parser as a unary operator applied
            // to a positive literal.
            case '-': {
                return makeToken(TokenType.T_ADDITIVE_OPERATOR, "-");
            }

            // ── ! or != ───────────────────────────────────────────────────────
            case '!': {
                if (pos < source.length() && peek() == '=') {
                    advance();
                    return makeToken(TokenType.T_EQUALITY_OPERATOR, "!=");
                }
                return makeToken(TokenType.T_UNARY_OPERATOR, "!");
            }

            // ── = or == ───────────────────────────────────────────────────────
            case '=': {
                if (pos < source.length() && peek() == '=') {
                    advance();
                    return makeToken(TokenType.T_EQUALITY_OPERATOR, "==");
                }
                return makeToken(TokenType.T_ASSIGNMENT_OPERATOR, "=");
            }

            // ── < or <= ───────────────────────────────────────────────────────
            case '<': {
                if (pos < source.length() && peek() == '=') {
                    advance();
                    return makeToken(TokenType.T_RELATIONAL_OPERATOR, "<=");
                }
                return makeToken(TokenType.T_RELATIONAL_OPERATOR, "<");
            }

            // ── > or >= ───────────────────────────────────────────────────────
            case '>': {
                if (pos < source.length() && peek() == '=') {
                    advance();
                    return makeToken(TokenType.T_RELATIONAL_OPERATOR, ">=");
                }
                return makeToken(TokenType.T_RELATIONAL_OPERATOR, ">");
            }

            // ── && ────────────────────────────────────────────────────────────
            case '&': {
                if (pos < source.length() && peek() == '&') {
                    advance(); // consume second '&'
                    return makeToken(TokenType.T_LOGICAL_AND, "&&");
                }
                printError(tokLine, tokCol,
                        "Invalid token '&' — did you mean '&&'?");
                return makeToken(TokenType.T_ERROR, "&");
            }

            // ── || ────────────────────────────────────────────────────────────
            case '|': {
                if (pos < source.length() && peek() == '|') {
                    advance();
                    return makeToken(TokenType.T_LOGICAL_OR, "||");
                }
                printError(tokLine, tokCol,
                        "Invalid token '|' — did you mean '||'?");
                return makeToken(TokenType.T_ERROR, "|");
            }

            // ── Single '~' not consumed by comment skipper ────────────────────
            case '~': {
                printError(tokLine, tokCol,
                        "Invalid token '~' — did you mean '~~' or '~~~'?");
                return makeToken(TokenType.T_ERROR, "~");
            }

            // ── Dot appearing outside a numeric literal ───────────────────────
            case '.': {
                printError(tokLine, tokCol,
                        "Unexpected '.' — '.' is only valid inside numeric literals");
                return makeToken(TokenType.T_ERROR, ".");
            }

            // ── Anything else is an illegal character ─────────────────────────
            default: {
                printError(tokLine, tokCol,
                        "Illegal character '" + c + "'");
                return makeToken(TokenType.T_ERROR, String.valueOf(c));
            }
        }
    }

    // =========================================================================
    // SECTION 6: Low-level character helpers
    // =========================================================================

    /**
     * Returns the character at the current position WITHOUT advancing.
     * Always call pos < source.length() before calling peek().
     */
    private char peek() {
        return source.charAt(pos);
    }

    /**
     * Returns the current character AND moves pos forward by one.
     * Also increments the column counter.
     */
    private char advance() {
        char c = source.charAt(pos++);
        col++;
        return c;
    }

    /**
     * Checks if the source string starting at the current pos exactly matches
     * the given string. If yes, consumes all those characters and returns true.
     * If no, does nothing and returns false.
     *
     * Used to consume multi-character sequences like "~~" and "~~~".
     */
    private boolean matchAhead(String s) {
        if (pos + s.length() > source.length())
            return false;
        if (!source.startsWith(s, pos))
            return false;
        pos += s.length();
        return true;
    }

    /**
     * Returns true if the character is a valid hexadecimal digit:
     * 0-9, a-f, A-F
     */
    private boolean isHexDigit(char c) {
        return Character.isDigit(c)
                || (c >= 'a' && c <= 'f')
                || (c >= 'A' && c <= 'F');
    }

    /**
     * Saves the current line and column as the start of the next token.
     */
    private void markTokenStart() {
        tokLine = line;
        tokCol = col;
    }

    /**
     * Creates a Token using the saved token-start position.
     */
    private Token makeToken(TokenType type, String lexeme) {
        return new Token(type, lexeme, tokLine, tokCol);
    }

    /**
     * Prints a scanner error to the console with line and column information.
     * The error is also returned as a T_ERROR token so scanning continues.
     */
    private void printError(int line, int col, String message) {
        final String ANSI_RESET = "\u001B[0m";
        final String ANSI_RED = "\u001B[31m";
        String errorString = String.format(
                ANSI_RED
                        + "Line " + line + ", Col " + col + ": " + message
                        + ANSI_RESET
                        + "\n");
        errorMessages.add(errorString);
        System.out.println(ANSI_RED + "[SCANNER ERROR] " + ANSI_RESET + errorString);
    }

    /**
     * returns a summary of all errors found during scanning
     */
    public List<String> getErrorSummary() {
        return errorMessages;
    }
}
