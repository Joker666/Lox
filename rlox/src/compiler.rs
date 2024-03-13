use crate::chunk::Chunk;
use crate::scanner::{Scanner, TokenType};
use crate::InterpretError;

pub struct Compiler;

impl Compiler {
    pub fn new() -> Self {
        Self {}
    }

    pub fn compile(&self, source: &str) -> Result<Chunk, InterpretError> {
        let mut scanner = Scanner::new(source);
        Ok(Chunk::new())
    }
}
