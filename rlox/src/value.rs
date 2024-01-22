pub type Value = f64;

pub struct ValueArray {
    values: Vec<Value>,
}

impl ValueArray {
    pub fn new() -> Self {
        Self { values: Vec::new() }
    }

    pub fn write(&mut self, value: Value) -> usize {
        let count = self.values.len();
        self.values.push(value);
        count
    }

    pub fn print_value(&self, index: usize) {
        print!("{}", self.values[index]);
    }

    pub fn free(&mut self) {
        self.values = Vec::new();
    }
}
