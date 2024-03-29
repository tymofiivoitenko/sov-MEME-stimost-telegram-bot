package com.tymofiivoitenko.telegram.bot.handler;


import com.tymofiivoitenko.telegram.model.user.UserState;
import com.tymofiivoitenko.telegram.model.user.User;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.NaturalId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.tymofiivoitenko.telegram.repository.UserRepository;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.Serializable;
import java.util.*;

import static com.tymofiivoitenko.telegram.bot.handler.MemeTestHandler.MEME_TEST_COMPETITION_START;
import static com.tymofiivoitenko.telegram.util.TelegramUtil.createInlineKeyboardButton;
import static com.tymofiivoitenko.telegram.util.TelegramUtil.createMessageTemplate;

@Slf4j
@Component
public class StartHandler implements Handler {

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {

        // Say hi to the user
        String welcomeMessage = String.format(
                "С помощью кнопок \uD83D\uDC4D\uD83C\uDFFB и \uD83D\uDC4E\uD83C\uDFFB оцени 13 случайных мемов и узнай, насколько твои вкусы на мемы совпадают со вкусами твоих знакомых:\n" +
                        "\n" +
                        "(нажимай кнопки только по одному разу, если бот зависает — дожидайся ответа)");

        // Create initial button
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> inlineKeyboardButtonsRow;


        if (message.startsWith(MEME_TEST_COMPETITION_START)) {
            log.info("He is here for competition");
            user.setState(UserState.MEME_TEST_COMPETITION);
            inlineKeyboardButtonsRow = List.of(
                    createInlineKeyboardButton("Начать тест на совМЕМстимость", message));
        } else {
            log.info("He is here to start new test");
            inlineKeyboardButtonsRow = List.of(
                    createInlineKeyboardButton("Начать тест на совМЕМстимость", MemeTestHandler.MEME_TEST_START));
            user.setState(UserState.MEME_TEST);
        }

        userRepository.save(user);
        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRow));

        return List.of(createMessageTemplate(user)
                .setText(welcomeMessage)
                .setReplyMarkup(inlineKeyboardMarkup));
    }

    @Override
    public List<UserState> operatedUserState() {
        return List.of(UserState.START);
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return Collections.emptyList();
    }
}