pub enum OpCode {
    OpReturn = 0,
}

pub struct Chunk {
    code: Vec<u8>,
}

impl Chunk {
    pub fn new() -> Self {
        Chunk { code: Vec::new() }
    }

    fn write(&mut self, byte: u8) {
        self.code.push(byte);
    }

    pub fn write_opcode(&mut self, opcode: OpCode) {
        self.write(opcode.into());
    }

    pub fn free(&mut self) {
        self.code = Vec::new();
    }
}

impl From<u8> for OpCode {
    fn from(value: u8) -> Self {
        match value {
            0 => OpCode::OpReturn,
            _ => panic!("Invalid OpCode"),
        }
    }
}

impl From<OpCode> for u8 {
    fn from(value: OpCode) -> Self {
        value as u8
    }
}
