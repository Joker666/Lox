mod chunk;
mod compiler;
mod scanner;
mod value;
mod vm;

use std::env::args;
use std::fs::read_to_string;
use std::io::{stdin, stdout, Write};
use std::process::exit;
use vm::*;

pub enum InterpretError {
    CompileError,
    RuntimeError,
}

fn main() {
    let mut vm = VM::new();

    let args = args().collect::<Vec<String>>();

    match args.len() {
        1 => run_prompt(&mut vm),
        2 => run_file(&mut vm, &args[1]),
        _ => {
            println!("Usage: rlox [path]");
            exit(64);
        }
    }

    vm.free();
}

fn run_prompt(vm: &mut VM) {
    loop {
        print!("> ");
        stdout().flush().unwrap();

        let mut line = String::new();
        stdin().read_line(&mut line).unwrap();

        let _ = vm.interpret(&line);
    }
}

fn run_file(vm: &mut VM, path: &str) {
    let buf = read_to_string(path).unwrap();
    match vm.interpret(&buf) {
        Err(InterpretError::CompileError) => exit(65),
        Err(InterpretError::RuntimeError) => exit(70),
        Ok(..) => exit(0),
    }
}
