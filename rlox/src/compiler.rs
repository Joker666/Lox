use crate::scanner::{Scanner, TokenType};

pub struct Compiler;

impl Compiler {
    pub fn new() -> Self {
        Self {}
    }

    pub fn compile(&self, source: &str) {
        let mut scanner = Scanner::new(source);
        let mut line = 0;

        loop {
            let token = scanner.scan_token();
            if token.line != line {
                print!("{:4} ", token.line);
                line = token.line;
            } else {
                print!("   | ");
            }
            println!("{:2} '{}'", token.token_type, token.lexeme);

            if token.token_type == TokenType::Eof {
                break;
            }
        }
    }
}
