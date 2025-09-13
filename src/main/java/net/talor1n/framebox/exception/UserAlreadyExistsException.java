package net.talor1n.framebox.exception;

public class UserAlreadyExistsException extends Exception {
    public UserAlreadyExistsException() {
        super("Пользователь с таким именем и фамилией уже существует");
    }
}
