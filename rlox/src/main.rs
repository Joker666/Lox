mod chunk;
mod value;
mod vm;

use chunk::*;
use vm::*;

fn main() {
    let mut vm = VM::new();
    let mut chunk = Chunk::new();

    let constant = chunk.add_constants(1.2);
    chunk.write_opcode(OpCode::OpConstant, 123);
    chunk.write(constant, 123);

    chunk.write_opcode(OpCode::OpReturn, 123);

    vm.interpret(&chunk);

    chunk.free();
    vm.free();
}
