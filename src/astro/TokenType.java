package astro;

/**
 * Every possible token type in the Astro language.
 *
 * How to read this file:
 *   - Each constant is one "kind" of token.
 *   - The Scanner will label every piece of source code with one of these.
 *   - The Tester will print reserved words/operators using these names (e.g. [T_WHEN]).
 */
public enum TokenType {

    // ── Program Structure ──────────────────────────────────────────────────────
    // "chart" - signals the start of a program
    T_PROGRAM_START,

    // ── Data Type Keywords ─────────────────────────────────────────────────────
    // Saturn, Moon, Mercury, Uranus, Mars, Venus, Neptune, Jupiter, Pluto
    T_DATATYPE,

    // ── Declaration Keywords ───────────────────────────────────────────────────
    T_ALIGN,         // align       - explicit / static declaration
    T_MANIFESTS,     // manifests   - implicit / dynamic declaration
    T_ARRAY_MARKER,  // constellation - array declaration
    T_REFINE,        // refine      - constant declaration

    // ── Control Flow Keywords ──────────────────────────────────────────────────
    T_WHEN,          // when        - if
    T_ELSE,          // else        - else
    T_ORBIT,         // orbit       - while loop
    T_CYCLE,         // cycle       - for loop
    T_CAST,          // cast        - do  (start of do-while)
    T_RETROGRADE,    // retrograde  - while (end of do-while)
    T_TRACE,         // trace       - switch
    T_PATH,          // path        - case
    T_VOIDPATH,      // voidPath    - default
    T_COLLAPSE,      // collapse    - break
    T_SKIP,          // skip        - continue

    // ── I/O Keywords ──────────────────────────────────────────────────────────
    T_SUMMON,        // summon      - input statement
    T_RADIATE,       // radiate     - output statement

    // ── Type Conversion ────────────────────────────────────────────────────────
    T_TRANSMUTE,     // transmute   - explicit type cast

    // ── Literal Values ─────────────────────────────────────────────────────────
    T_NUMBER_LITERAL,    // 42, -3.14, 0xFF, 0b101, 0o77
    T_STRING_LITERAL,    // "hello"
    T_CHARACTER_LITERAL, // 'A'
    T_BOOLEAN_LITERAL,   // true | false
    T_NULL_LITERAL,      // null

    // ── Identifiers ────────────────────────────────────────────────────────────
    // Any name that is NOT a reserved word: age, myVar, config_path$
    T_IDENTIFIER,

    // ── Arithmetic Operators ───────────────────────────────────────────────────
    T_ADDITIVE_OPERATOR,        // + or -
    T_MULTIPLICATIVE_OPERATOR,  // * or / or %

    // ── Unary Operator ─────────────────────────────────────────────────────────
    // ! (logical NOT)
    // Note: unary minus (-) shares the T_ADDITIVE_OPERATOR token
    T_UNARY_OPERATOR,

    // ── Relational Operators ───────────────────────────────────────────────────
    T_RELATIONAL_OPERATOR,  // >  <  >=  <=

    // ── Equality Operators ─────────────────────────────────────────────────────
    T_EQUALITY_OPERATOR,    // ==  !=

    // ── Logical Operators ──────────────────────────────────────────────────────
    T_LOGICAL_AND,   // &&
    T_LOGICAL_OR,    // ||

    // ── Assignment ─────────────────────────────────────────────────────────────
    T_ASSIGNMENT_OPERATOR,  // =

    // ── Delimiters and Separators ──────────────────────────────────────────────
    T_LPAREN,               // (
    T_RPAREN,               // )
    T_BLOCK_START,          // {
    T_BLOCK_END,            // }
    T_LSQR,                 // [
    T_RSQR,                 // ]
    T_STATEMENT_DELIMITER,  // ;
    T_SEPARATOR,            // ,
    T_COLON,                // :

    // ── Special ────────────────────────────────────────────────────────────────
    T_EOF,   // end of file - signals the tester to stop requesting tokens
    T_ERROR  // a character sequence that does not form any valid token
}
