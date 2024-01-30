use crate::chunk::*;

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
            let op_code = self.read_byte(chunk);
            return match op_code {
                OpCode::OpReturn => InterpretResult::Ok,
                OpCode::OpConstant => todo!(),
            };
        }
    }

    fn read_byte(&mut self, chunk: &Chunk) -> OpCode {
        let op_code = chunk.read(self.ip).into();
        self.ip += 1;
        op_code
    }

    pub fn free(&mut self) {}
}
