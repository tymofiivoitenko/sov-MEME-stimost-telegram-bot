package com.tymofiivoitenko.telegram.bot.handler;


import com.tymofiivoitenko.telegram.bot.state.userState.UserState;
import com.tymofiivoitenko.telegram.model.User;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;

import java.util.List;

import java.io.Serializable;

public interface Handler {

    List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message);

    List<UserState> operatedUserState();

    List<String> operatedCallBackQuery();
}
