use crate::chunk::*;
use crate::value::*;

pub enum InterpretResult {
    Ok,
    CompileError,
    RuntimeError,
}

pub struct VM {
    ip: usize, // instruction pointer
}

impl VM {
    pub fn new() -> Self {
        Self { ip: 0 }
    }

    pub fn interpret(&mut self, chunk: &Chunk) -> InterpretResult {
        self.run(chunk)
    }

    pub fn run(&mut self, chunk: &Chunk) -> InterpretResult {
        loop {
            #[cfg(feature = "debug_trace_exec")]
            chunk.disassemble_instruction(self.ip);

            let op_code = self.read_byte(chunk);

            match op_code {
                OpCode::OpReturn => {
                    return InterpretResult::Ok;
                }
                OpCode::OpConstant => {
                    let constant = self.read_constant(chunk);
                    println!("{}", constant);
                }
            };
        }
    }

    fn read_byte(&mut self, chunk: &Chunk) -> OpCode {
        let op_code = chunk.read(self.ip).into();
        self.ip += 1;
        op_code
    }

    fn read_constant(&mut self, chunk: &Chunk) -> Value {
        let index = chunk.read(self.ip) as usize;
        self.ip += 1;
        chunk.get_constant(index)
    }

    pub fn free(&mut self) {}
}
