use crate::chunk::*;
use crate::compiler::*;
use crate::value::*;

pub enum InterpretResult {
    Ok,
    CompileError,
    RuntimeError,
}

pub struct VM {
    ip: usize, // instruction pointer
    stack: Vec<Value>,
}

impl VM {
    pub fn new() -> Self {
        Self {
            ip: 0,
            stack: Vec::new(),
        }
    }

    pub fn interpret(&mut self, source: &String) -> InterpretResult {
        let compiler = Compiler::new();
        compiler.compile(source);
        InterpretResult::Ok
    }

    pub fn run(&mut self, chunk: &Chunk) -> InterpretResult {
        loop {
            #[cfg(feature = "debug_trace_exec")]
            {
                print!("          ");
                for slot in &self.stack {
                    print!("[ {:?} ]", slot);
                }
                println!();
                chunk.disassemble_instruction(self.ip);
            }

            let op_code = self.read_byte(chunk);

            match op_code {
                OpCode::Return => {
                    println!("{:?}", self.stack.pop().unwrap());
                    return InterpretResult::Ok;
                }
                OpCode::Constant => {
                    let constant = self.read_constant(chunk);
                    self.stack.push(constant);
                }
                OpCode::Negate => {
                    let value = self.stack.pop().unwrap();
                    self.stack.push(-value);
                }
                OpCode::Add => self.binary_op(|a, b| a + b),
                OpCode::Subtract => self.binary_op(|a, b| a - b),
                OpCode::Multiply => self.binary_op(|a, b| a * b),
                OpCode::Divide => self.binary_op(|a, b| a / b),
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

    // When the operands themselves are calculated, the left is evaluated first, then the right.
    // That means the left operand gets pushed before the right operand.
    // So the right operand will be on top of the stack.
    // That's why we assign the first popped operand to b.
    pub fn binary_op(&mut self, op: fn(a: Value, b: Value) -> Value) {
        let b = self.stack.pop().unwrap();
        let a = self.stack.pop().unwrap();
        self.stack.push(op(a, b));
    }

    pub fn free(&mut self) {}
}
