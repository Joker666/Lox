use crate::chunk::Chunk;
use crate::scanner::Scanner;
use crate::token::{Token, TokenType};
use crate::InterpretError;
use std::cell::RefCell;

pub struct Compiler {
    parser: Parser,
    scanner: Scanner,
}

#[derive(Default)]
pub struct Parser {
    current: Token,
    previous: Token,
    had_error: RefCell<bool>,
}

impl Compiler {
    pub fn new() -> Self {
        Self {
            parser: Parser::default(),
            scanner: Scanner::new(""),
        }
    }

    pub fn compile(&mut self, source: &str) -> Result<Chunk, InterpretError> {
        self.scanner = Scanner::new(source);

        self.advance();

        // self.expression();
        // self.consume(TokenType::Eof, "Expect end of expression.");

        if *self.parser.had_error.borrow() {
            Err(InterpretError::CompileError)
        } else {
            Ok(Chunk::new())
        }
    }

    pub fn advance(&mut self) {
        self.parser.previous = self.parser.current.clone();
        loop {
            self.parser.current = self.scanner.scan_token();
            if self.parser.current.token_type != TokenType::Error {
                break;
            }

            self.error_at_current(self.parser.current.lexeme.as_str());
        }
    }

    pub fn error_at_current(&self, message: &str) {
        self.error_at(&self.parser.current, message);
    }

    pub fn error(&self, message: &str) {
        self.error_at(&self.parser.previous, message);
    }

    pub fn error_at(&self, token: &Token, message: &str) {
        eprint!("[line {}] Error", token.line);

        if token.token_type == TokenType::Eof {
            eprint!(" at end");
        } else if token.token_type == TokenType::Error {
            // ignore
        } else {
            eprint!(" at '{}'", token.lexeme);
        }

        eprintln!(": {message}");
    }
}
