package com.tymofiivoitenko.telegram.bot.updateReceiver;

import com.tymofiivoitenko.telegram.bot.handler.Handler;
import com.tymofiivoitenko.telegram.bot.userState.UserState;
import com.tymofiivoitenko.telegram.model.User;
import org.springframework.stereotype.Component;
import com.tymofiivoitenko.telegram.repository.JpaUserRepository;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Component
public class UpdateReceiver {
    // Храним доступные хендлеры в списке (подсмотрел у Miroha)
    private final List<Handler> handlers;
    // Имеем доступ в базу пользователей
    private final JpaUserRepository userRepository;

    public UpdateReceiver(List<Handler> handlers, JpaUserRepository userRepository) {
        this.handlers = handlers;
        this.userRepository = userRepository;
    }

    // Обрабатываем полученный Update
    public List<PartialBotApiMethod<? extends Serializable>> handle(Update update) {
        System.out.println("New update message: " + update.getMessage());

        // try-catch, чтобы при несуществующей команде просто возвращать пустой список
        try {

            // Проверяем, если Update - сообщение с текстом
            if (isMessageWithText(update)) {
                // Получаем Message из Update
                final Message message = update.getMessage();
                // Получаем айди чата с пользователем

                final int chatId = message.getFrom().getId();
                final String firstName = message.getFrom().getFirstName();
                final String lastName = message.getFrom().getLastName();
                final String userName= message.getFrom().getUserName();
                // Просим у репозитория пользователя. Если такого пользователя нет - создаем нового и возвращаем его.
                // Как раз на случай нового пользователя мы и сделали конструктор с одним параметром в классе User
                final User user = userRepository.getByChatId(chatId)
                        .orElseGet(() -> userRepository.save(new User(chatId, firstName, lastName, userName)));
                // Ищем нужный обработчик и возвращаем результат его работы
                return getHandlerByState(user.getUserState()).handle(user, message.getText());

            } else if (update.hasCallbackQuery()) {
                final CallbackQuery callbackQuery = update.getCallbackQuery();
                final String firstName = callbackQuery.getFrom().getFirstName();
                final String lastName = callbackQuery.getFrom().getLastName();
                final String userName= callbackQuery.getFrom().getUserName();

                final int chatId = callbackQuery.getFrom().getId();
                final User user = userRepository.getByChatId(chatId)
                        .orElseGet(() -> userRepository.save(new User(chatId, firstName, lastName, userName)));

                return getHandlerByCallBackQuery(callbackQuery.getData()).handle(user, callbackQuery.getData());
            }

            throw new UnsupportedOperationException();
        } catch (UnsupportedOperationException e) {
            return Collections.emptyList();
        }
    }

    private Handler getHandlerByState(UserState userState) {
        return handlers.stream()
                .filter(h -> h.operatedBotState() != null)
                .filter(h -> h.operatedBotState().contains(userState))
                .findAny()
                .orElseThrow(UnsupportedOperationException::new);
    }

    private Handler getHandlerByCallBackQuery(String query) {
        return handlers.stream()
                .filter(h -> h.operatedCallBackQuery().stream()
                        .anyMatch(query::startsWith))
                .findAny()
                .orElseThrow(UnsupportedOperationException::new);
    }

    private boolean isMessageWithText(Update update) {
        return !update.hasCallbackQuery() && update.hasMessage() && update.getMessage().hasText();
    }
}