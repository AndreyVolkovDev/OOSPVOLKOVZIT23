package com.evoting.model; // Класс CEC находится в этом пакете

// import com.evoting.model.User; // <-- Эта строка нужна!

public class CEC extends User { // <-- Теперь компилятор знает, что такое User
    public CEC(String login, String password) {
        super(login, password, "ЦИК " + login, Role.CEC); // Также убедитесь, что Role импортирован
    }
}