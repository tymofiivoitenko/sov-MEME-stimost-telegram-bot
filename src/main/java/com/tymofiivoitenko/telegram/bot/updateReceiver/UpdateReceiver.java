package com.tymofiivoitenko.telegram.bot.updateReceiver;

import com.tymofiivoitenko.telegram.bot.handler.Handler;
import com.tymofiivoitenko.telegram.model.user.UserState;
import com.tymofiivoitenko.telegram.model.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.tymofiivoitenko.telegram.repository.UserRepository;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class UpdateReceiver {
    public static final String MEME_FORCE_START_OVER = "/start";
    public static final String STATISTICS = "/statistics";

    private final List<Handler> handlers;

    private final UserRepository userRepository;

    public UpdateReceiver(List<Handler> handlers, UserRepository userRepository) {
        this.handlers = handlers;
        this.userRepository = userRepository;
    }

    public List<PartialBotApiMethod<? extends Serializable>> handle(Update update) {

        try {

            // Check if that is update with message
            if (isMessageWithText(update)) {
                final Message message = update.getMessage();
                log.info("New update message: <" + message + ">");

                final Integer chatId = message.getFrom().getId();
                final String firstName = message.getFrom().getFirstName();
                final String lastName = message.getFrom().getLastName();
                final String userName= message.getFrom().getUserName();
                // Check user repository - if there is no such user, we create a new one and return it.
                final User user = userRepository.getByChatId(chatId)
                        .orElseGet(() -> userRepository.save(new User(chatId, firstName, lastName, userName)));

                // Check if user wants bot to start from scratch by typing "/start"
                if (message.getText().equals(MEME_FORCE_START_OVER)) {
                    user.setState(UserState.START);
                    userRepository.save(user);
                }

                // Check if user wants bot to provide the statistics
                if (message.getText().equals(STATISTICS)) {
                    user.setState(UserState.GET_STATISTICS);
                    userRepository.save(user);
                }

                // Find suitable current based on user state
                return getHandlerByState(user.getState()).handle(user, message.getText());

            } else if (update.hasCallbackQuery()) {
                final CallbackQuery callbackQuery = update.getCallbackQuery();
                log.info("New update callbackQuery: <" + callbackQuery.getData() + ">");
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
                .filter(h -> h.operatedUserState() != null)
                .filter(h -> h.operatedUserState().contains(userState))
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