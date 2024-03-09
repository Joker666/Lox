use std::fmt;

pub struct Scanner {
    source: Vec<char>,
    start: usize,
    current: usize,
    line: usize,
}

pub struct Token {
    pub token_type: TokenType,
    pub lexeme: String,
    pub line: usize,
}

impl Scanner {
    pub fn new(source: &str) -> Self {
        Self {
            source: source.chars().collect(),
            start: 0,
            current: 0,
            line: 1,
        }
    }

    /// Returns the current character in the source code.
    ///
    /// This is used to scan through each character
    /// in the source code during lexical analysis.
    fn current(&self) -> char {
        self.source[self.current]
    }

    /// Returns the next character in the source code without consuming it.
    /// Returns '\0' if at the end of the source code. Used to look ahead
    /// when scanning through the source code.
    fn peek(&self) -> char {
        if self.is_at_end() {
            '\0'
        } else {
            self.source[self.current + 1]
        }
    }

    pub fn scan_token(&mut self) -> Token {
        self.skip_ignored();
        self.start = self.current;

        if self.is_at_end() {
            return self.make_token(TokenType::Eof);
        }

        let c = self.advance();
        if c.is_alphabetic() {
            return self.identifier();
        }

        if c.is_ascii_digit() {
            return self.number();
        }

        match c {
            '(' => self.make_token(TokenType::LeftParen),
            ')' => self.make_token(TokenType::RightParen),
            '{' => self.make_token(TokenType::LeftBrace),
            '}' => self.make_token(TokenType::RightBrace),
            ';' => self.make_token(TokenType::Semicolon),
            ',' => self.make_token(TokenType::Comma),
            '.' => self.make_token(TokenType::Dot),
            '-' => self.make_token(TokenType::Minus),
            '+' => self.make_token(TokenType::Plus),
            '/' => self.make_token(TokenType::Slash),
            '*' => self.make_token(TokenType::Star),
            '!' => {
                if self.match_and_advance('=') {
                    self.make_token(TokenType::BangEqual)
                } else {
                    self.make_token(TokenType::Bang)
                }
            }
            '=' => {
                if self.match_and_advance('=') {
                    self.make_token(TokenType::EqualEqual)
                } else {
                    self.make_token(TokenType::Equal)
                }
            }
            '<' => {
                if self.match_and_advance('=') {
                    self.make_token(TokenType::LessEqual)
                } else {
                    self.make_token(TokenType::Less)
                }
            }
            '>' => {
                if self.match_and_advance('=') {
                    self.make_token(TokenType::GreaterEqual)
                } else {
                    self.make_token(TokenType::Greater)
                }
            }
            _ => self.error_token("Unexpected character."),
        }
    }

    fn make_token(&self, token_type: TokenType) -> Token {
        Token {
            token_type,
            lexeme: self.get_text(),
            line: self.line,
        }
    }

    fn is_at_end(&self) -> bool {
        self.current >= self.source.len()
    }

    fn match_and_advance(&mut self, expected: char) -> bool {
        if self.is_at_end() {
            return false;
        }
        if self.source[self.current] != expected {
            return false;
        }

        self.current += 1;
        true
    }

    /// advances the current position and returns the character at the new position.
    fn advance(&mut self) -> char {
        self.current += 1;
        self.source[self.current - 1]
    }

    /// Parses a string token from the source code.
    ///
    /// Advances the current position through the string, handling escaped
    /// characters and tracking the current line. If the end of the string is
    /// reached without finding a closing quote, returns an error token.
    /// Otherwise, makes a string token from the lexeme.
    fn string(&mut self) -> Token {
        while self.current() != '"' && !self.is_at_end() {
            if self.current() == '\n' {
                self.line += 1;
            }
            self.advance();
        }

        if self.is_at_end() {
            return self.error_token("Unterminated string.");
        }

        // The closing ".
        self.advance();

        self.make_token(TokenType::String)
    }

    fn number(&mut self) -> Token {
        while self.current().is_ascii_digit() {
            self.advance();
        }

        // Look for a fractional part.
        if self.current() == '.' && self.peek().is_ascii_digit() {
            // Consume the "."
            self.advance();

            while self.current().is_ascii_digit() {
                self.advance();
            }
        }

        self.make_token(TokenType::Number)
    }

    fn identifier(&mut self) -> Token {
        while self.current().is_alphanumeric() {
            self.advance();
        }

        self.make_token(self.identifier_type())
    }

    fn identifier_type(&self) -> TokenType {
        match self.get_text().as_str() {
            "and" => TokenType::And,
            "class" => TokenType::Class,
            "else" => TokenType::Else,
            "false" => TokenType::False,
            "for" => TokenType::For,
            "fun" => TokenType::Fun,
            "if" => TokenType::If,
            "nil" => TokenType::Nil,
            "or" => TokenType::Or,
            "print" => TokenType::Print,
            "return" => TokenType::Return,
            "super" => TokenType::Super,
            "this" => TokenType::This,
            "true" => TokenType::True,
            "var" => TokenType::Var,
            "while" => TokenType::While,
            _ => TokenType::Identifier,
        }
    }

    /// Skips over any whitespace and comments until non-ignored
    /// characters are reached. Increments the line counter when
    /// newlines are encountered.
    fn skip_ignored(&mut self) {
        loop {
            let c = self.current();
            match c {
                ' ' | '\r' | '\t' => {
                    self.advance();
                }
                '\n' => {
                    self.line += 1;
                    self.advance();
                }
                '/' => {
                    if self.peek() == '/' {
                        // A comment goes until the end of the line.
                        while self.current() != '\n' && !self.is_at_end() {
                            self.advance();
                        }
                    } else {
                        return;
                    }
                }
                _ => return,
            }
        }
    }

    fn error_token(&self, message: &str) -> Token {
        Token {
            token_type: TokenType::Error,
            lexeme: message.to_string(),
            line: self.line,
        }
    }

    fn get_text(&self) -> String {
        self.source[self.start..self.current].iter().collect()
    }
}

#[derive(Debug, PartialEq, Copy, Clone)]
pub enum TokenType {
    LeftParen,
    RightParen,
    LeftBrace,
    RightBrace,
    Comma,
    Dot,
    Minus,
    Plus,
    Semicolon,
    Slash,
    Star,
    Bang,
    BangEqual,
    Equal,
    EqualEqual,
    Greater,
    GreaterEqual,
    Less,
    LessEqual,
    Identifier,
    String,
    Number,
    And,
    Class,
    Else,
    False,
    Fun,
    For,
    If,
    Nil,
    Or,
    Print,
    Return,
    Super,
    This,
    True,
    Var,
    While,
    Error,
    Eof,
}

impl fmt::Display for TokenType {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self {
            TokenType::LeftParen => write!(f, "LEFT_PAREN"),
            TokenType::RightParen => write!(f, "RIGHT_PAREN"),
            TokenType::LeftBrace => write!(f, "LEFT_BRACE"),
            TokenType::RightBrace => write!(f, "RIGHT_BRACE"),
            TokenType::Comma => write!(f, "COMMA"),
            TokenType::Dot => write!(f, "DOT"),
            TokenType::Minus => write!(f, "MINUS"),
            TokenType::Plus => write!(f, "PLUS"),
            TokenType::Semicolon => write!(f, "SEMICOLON"),
            TokenType::Slash => write!(f, "SLASH"),
            TokenType::Star => write!(f, "STAR"),
            TokenType::Bang => write!(f, "BANG"),
            TokenType::BangEqual => write!(f, "BANG_EQUAL"),
            TokenType::Equal => write!(f, "EQUAL"),
            TokenType::EqualEqual => write!(f, "EQUAL_EQUAL"),
            TokenType::Greater => write!(f, "GREATER"),
            TokenType::GreaterEqual => write!(f, "GREATER_EQUAL"),
            TokenType::Less => write!(f, "LESS"),
            TokenType::LessEqual => write!(f, "LESS_EQUAL"),
            TokenType::Identifier => write!(f, "IDENTIFIER"),
            TokenType::String => write!(f, "STRING"),
            TokenType::Number => write!(f, "NUMBER"),
            TokenType::And => write!(f, "AND"),
            TokenType::Class => write!(f, "CLASS"),
            TokenType::Else => write!(f, "ELSE"),
            TokenType::False => write!(f, "FALSE"),
            TokenType::Fun => write!(f, "FUN"),
            TokenType::For => write!(f, "FOR"),
            TokenType::If => write!(f, "IF"),
            TokenType::Nil => write!(f, "NIL"),
            TokenType::Or => write!(f, "OR"),
            TokenType::Print => write!(f, "PRINT"),
            TokenType::Return => write!(f, "RETURN"),
            TokenType::Super => write!(f, "SUPER"),
            TokenType::This => write!(f, "THIS"),
            TokenType::True => write!(f, "TRUE"),
            TokenType::Var => write!(f, "VAR"),
            TokenType::While => write!(f, "WHILE"),
            TokenType::Error => write!(f, "ERROR"),
            TokenType::Eof => write!(f, "EOF"),
        }
    }
}
