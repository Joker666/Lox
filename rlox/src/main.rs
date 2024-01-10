mod chunk;
use chunk::*;

fn main() {
    println!("Hello, world!");

    let mut chunk = Chunk::new();
    chunk.write_opcode(OpCode::OpReturn);
    chunk.free();
}
