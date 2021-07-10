package com.tymofiivoitenko.telegram.bot.handler;

import com.tymofiivoitenko.telegram.bot.state.userState.UserState;
import com.tymofiivoitenko.telegram.model.User;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import static com.tymofiivoitenko.telegram.util.TelegramUtil.createInlineKeyboardButton;
import static com.tymofiivoitenko.telegram.util.TelegramUtil.createMessageTemplate;

@Component
public class HelpHandler implements Handler {
    public static final String HELP = "/start";

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {
        // Button for changing name
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        // To-Do finish helphandler
        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = List.of(
                createInlineKeyboardButton("Get help", HELP));

        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne));

        return List.of(createMessageTemplate(user).setText(String.format("" +
                "Ты пришел за помощью? Это сюда..", user.getFirstName()))
                .setReplyMarkup(inlineKeyboardMarkup));
    }

    @Override
    public List<UserState> operatedUserState() {
        return List.of(UserState.NONE);
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return Collections.emptyList();
    }
}

