package com.tymofiivoitenko.telegram.bot.handler;

import com.tymofiivoitenko.telegram.model.user.User;
import com.tymofiivoitenko.telegram.model.user.UserState;
import com.tymofiivoitenko.telegram.repository.MemeReactionRepository;
import com.tymofiivoitenko.telegram.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.tymofiivoitenko.telegram.bot.updateReceiver.UpdateReceiver.STATISTICS;
import static com.tymofiivoitenko.telegram.model.user.UserRole.ROLE_ADMIN;
import static com.tymofiivoitenko.telegram.util.TelegramUtil.createMessageTemplate;

@Slf4j
@Component
public class StatisticsHandler implements Handler {

    public static final String STATISTICS_NUMBER_OF_USERS = "/statistics_number_of_users";
    public static final String STATISTICS_NUMBER_OF_NEW_USERS = "/statistics_number_of_new_users";
    public static final String STATISTICS_NUMBER_OF_ALL_REACTIONS = "/statistics_number_of_all_reactions";
    public static final String STATISTICS_NUMBER_OF_NEW_REACTIONS = "/statistics_number_of_new_reactions";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemeReactionRepository memeReactionRepository;

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {

        // Check is user has access to receive statistics
        if (!user.getRole().equals(ROLE_ADMIN)) {
            return List.of(createMessageTemplate(user)
                    .setText("ACCESS DENIED"));
        }

        if (message.startsWith(STATISTICS_NUMBER_OF_USERS)) {
            return getNumberOfAllUsers(user);
        } else if (message.startsWith(STATISTICS_NUMBER_OF_NEW_USERS)) {
            return geNewUsers(user);
        } else if (message.startsWith(STATISTICS_NUMBER_OF_ALL_REACTIONS)) {
            return getNumberOfAllReactions(user);
        } else if (message.startsWith(STATISTICS_NUMBER_OF_NEW_REACTIONS)) {
            return getNumberOfNewReactions(user);
        } else if (message.equals(STATISTICS)) {
            return List.of(createMessageTemplate(user)
                    .setText("Options available for statistics")
                    .setReplyMarkup(getStatisticsKeyboardMakrup()));
        }

        throw new UnsupportedOperationException("Unexpected call back message:" + message);
    }

    private List<PartialBotApiMethod<? extends Serializable>> getNumberOfAllUsers(User user) {
        log.info("Admin requested statistics - number of all users");

        return List.of(createMessageTemplate(user)
                .setText(String.format("Number of all users: *%s*", this.userRepository.findAll().size())));
    }

    private List<PartialBotApiMethod<? extends Serializable>> geNewUsers(User user) {
        log.info("Admin requested statistics - new users");
        List<User> newUsers = this.userRepository.findNewUsers();

        return List.of(createMessageTemplate(user)
                .setText(String.format("Number of new users: *%s*", newUsers.size()) +
                        "\nNew users: \n" + newUsers.stream().map(String::valueOf).collect(Collectors.joining(", \n"))));
    }

    private List<PartialBotApiMethod<? extends Serializable>> getNumberOfAllReactions(User user) {
        log.info("Admin requested statistics - number of all reactions");

        return List.of(createMessageTemplate(user)
                .setText(String.format("Number of all reactions: *%s*", this.memeReactionRepository.findAll().size())));
    }

    private List<PartialBotApiMethod<? extends Serializable>> getNumberOfNewReactions(User user) {
        log.info("Admin requested statistics - number of new reactions");

        return List.of(createMessageTemplate(user)
                .setText(String.format("Number of new reactions: *%s*", this.memeReactionRepository.getNewReactions().size())));
    }

    private InlineKeyboardMarkup getStatisticsKeyboardMakrup() {
        List<InlineKeyboardButton> inlineKeyboardButtonsUsersRow = new ArrayList<>();
        List<InlineKeyboardButton> inlineKeyboardButtonsReactionsRow = new ArrayList<>();

        // STATISTICS_NUMBER_OF_USERS
        InlineKeyboardButton statisticsNumberOfUsers = new InlineKeyboardButton()
                .setText("number of users")
                .setCallbackData(STATISTICS_NUMBER_OF_USERS);

        // STATISTICS_NUMBER_OF_NEW_USERS
        InlineKeyboardButton statisticsNumberOfNewUsers = new InlineKeyboardButton()
                .setText("number of new users")
                .setCallbackData(STATISTICS_NUMBER_OF_NEW_USERS);

        // STATISTICS_NUMBER_OF_ALL_REACTIONS
        InlineKeyboardButton statisticsNumberOfAllReactions = new InlineKeyboardButton()
                .setText("number of reactions")
                .setCallbackData(STATISTICS_NUMBER_OF_ALL_REACTIONS);

        // STATISTICS_NUMBER_OF_ALL_REACTIONS
        InlineKeyboardButton statisticsNumberOfNewReactions = new InlineKeyboardButton()
                .setText("number of new reactions")
                .setCallbackData(STATISTICS_NUMBER_OF_NEW_REACTIONS);

        inlineKeyboardButtonsUsersRow.add(statisticsNumberOfUsers);
        inlineKeyboardButtonsUsersRow.add(statisticsNumberOfNewUsers);
        inlineKeyboardButtonsReactionsRow.add(statisticsNumberOfAllReactions);
        inlineKeyboardButtonsReactionsRow.add(statisticsNumberOfNewReactions);

        return new InlineKeyboardMarkup().setKeyboard(List.of(inlineKeyboardButtonsUsersRow, inlineKeyboardButtonsReactionsRow));
    }

    @Override
    public List<UserState> operatedUserState() {
        return List.of(UserState.GET_STATISTICS);
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return List.of(STATISTICS_NUMBER_OF_USERS, STATISTICS_NUMBER_OF_NEW_USERS, STATISTICS_NUMBER_OF_ALL_REACTIONS, STATISTICS_NUMBER_OF_NEW_REACTIONS);
    }
}
