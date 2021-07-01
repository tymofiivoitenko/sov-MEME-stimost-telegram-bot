package com.tymofiivoitenko.telegram.bot.handler;


import com.tymofiivoitenko.telegram.bot.state.userState.UserState;
import com.tymofiivoitenko.telegram.model.User;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;

import java.util.List;

import java.io.Serializable;

public interface Handler {

// основной метод, который будет обрабатывать действия пользователя
    List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message);
    // метод, который позволяет узнать, можем ли мы обработать текущий State у пользователя
    List<UserState> operatedBotState();
    // метод, который позволяет узнать, какие команды CallBackQuery мы можем обработать в этом классе
    List<String> operatedCallBackQuery();
}
