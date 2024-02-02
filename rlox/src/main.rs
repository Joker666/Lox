mod chunk;
mod value;
mod vm;

use chunk::*;
use vm::*;

fn main() {
    let mut vm = VM::new();
    let mut chunk = Chunk::new();

    let mut constant = chunk.add_constants(1.2);
    chunk.write_opcode(OpCode::Constant, 123);
    chunk.write(constant, 123);

    constant = chunk.add_constants(3.4);
    chunk.write_opcode(OpCode::Constant, 123);
    chunk.write(constant, 123);

    chunk.write_opcode(OpCode::Add, 123);

    constant = chunk.add_constants(5.6);
    chunk.write_opcode(OpCode::Constant, 123);
    chunk.write(constant, 123);

    chunk.write_opcode(OpCode::Divide, 123);
    chunk.write_opcode(OpCode::Negate, 123);

    chunk.write_opcode(OpCode::Return, 123);

    vm.interpret(&chunk);

    chunk.free();
    vm.free();
}
