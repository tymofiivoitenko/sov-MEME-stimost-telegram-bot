package com.tymofiivoitenko.telegram.bot.handler;


import com.tymofiivoitenko.telegram.bot.state.userState.UserState;
import com.tymofiivoitenko.telegram.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.tymofiivoitenko.telegram.repository.JpaUserRepository;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.Serializable;
import java.util.*;

import static com.tymofiivoitenko.telegram.bot.handler.MemTestHandler.MEME_TEST_COMPETION_START;
import static com.tymofiivoitenko.telegram.util.TelegramUtil.createInlineKeyboardButton;
import static com.tymofiivoitenko.telegram.util.TelegramUtil.createMessageTemplate;

@Slf4j
@Component
public class StartHandler implements Handler {
    @Value("${bot.name}")
    private String botUsername;

    private final JpaUserRepository userRepository;

    public StartHandler(JpaUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {

        // Приветствуем пользователя
//        SendMessage welcomeMessage = createMessageTemplate(user)
//                .setText(String.format(
//                        "Привет! Пришел проверится у мемного психолога? Это лучше чем гороскопы. Начнем?"));
//
        String welcomeMessage = String.format(
                "С помощью кнопок \uD83D\uDC4D\uD83C\uDFFB и \uD83D\uDC4E\uD83C\uDFFB оцени 13 случайных мемов и узнай, насколько твои вкусы на мемы совпадают со вкусами твоих знакомых:\n" +
                        "\n" +
                        "(нажимай кнопки только по одному разу, если бот зависает — дожидайся ответа)");
//        // Просим назваться
//        SendMessage registrationMessage = createMessageTemplate(user)
//                .setText("Для начала, скажи как тебя зовут:");

        // Создаем кнопку для начала игры
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> inlineKeyboardButtonsRow;


        if (message.startsWith(MEME_TEST_COMPETION_START)) {
            // Меняем пользователю статус на - "ожидание ввода имени"
            log.info("он пришел сюда посоревноваться");
            user.setUserState(UserState.MEM_TEST_COMETETION);
            inlineKeyboardButtonsRow = List.of(
                    createInlineKeyboardButton("Начать тест на совМЕМстимость",  message));
        } else {
            log.info("Он пришел создавать тест");
            inlineKeyboardButtonsRow = List.of(
                    createInlineKeyboardButton("Начать тест на совМЕМстимость", MemTestHandler.MEME_TEST_START));
            user.setUserState(UserState.MEM_TEST);
        }

        userRepository.save(user);
        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRow));

        return List.of(createMessageTemplate(user)
                .setText(welcomeMessage)
                .setReplyMarkup(inlineKeyboardMarkup));
    }

    @Override
    public List<UserState> operatedBotState() {
        return List.of(UserState.START);
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return Collections.emptyList();
    }
}