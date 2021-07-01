package com.tymofiivoitenko.telegram.bot.handler;

import com.tymofiivoitenko.telegram.bot.MemReactionState;
import com.tymofiivoitenko.telegram.bot.userState.UserState;
import com.tymofiivoitenko.telegram.model.MemReaction;
import com.tymofiivoitenko.telegram.model.User;
import com.tymofiivoitenko.telegram.repository.JpaMemReactionRepository;
import org.springframework.stereotype.Component;
import com.tymofiivoitenko.telegram.repository.JpaUserRepository;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import static com.tymofiivoitenko.telegram.util.TelegramUtil.createInlineKeyboardButton;
import static com.tymofiivoitenko.telegram.util.TelegramUtil.createMessageTemplate;

@Component
public class RegistrationHandler implements Handler {
    //Храним поддерживаемые CallBackQuery в виде констант
    public static final String NAME_ACCEPT = "/enter_name_accept";
    public static final String NAME_CHANGE = "/enter_name";
    public static final String NAME_CHANGE_CANCEL = "/enter_name_cancel";

    private final JpaUserRepository userRepository;
    private final JpaMemReactionRepository memReactionRepository;

    public RegistrationHandler(JpaUserRepository userRepository, JpaMemReactionRepository memReactionRepository) {
        this.userRepository = userRepository;
        this.memReactionRepository = memReactionRepository;
    }

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {
        return accept(user);
    }

    private List<PartialBotApiMethod<? extends Serializable>> accept(User user) {
        // Если пользователь принял имя - меняем статус и сохраняем
        user.setUserState(UserState.NONE);
        userRepository.save(user);

        List<MemReaction> allMemeReactions = memReactionRepository.findAll();

        Optional<MemReaction> lastMemReaction = allMemeReactions.stream()
                .sorted((f1, f2) -> Long.compare(f2.getId(), f1.getId()))
                .findFirst();

        System.out.println("LAST meme reaction: " + lastMemReaction.get());


        return List.of(createMessageTemplate(user)
                .setText(String.format("Теперь осталось узнать вкусы твоих знакомых и сравнить с твоими. Для этого отправь им свою личную ссылку на тест:\n" +
                        "\n" +
                        "t.me/sovmemstimost_bot?start= %d \n" +
                        "\n" +
                        "Когда они пройдут тест — тебе придут результаты, с кем именно и насколько у тебя совпадают вкусы на мемы.")));
    }

    private List<PartialBotApiMethod<? extends Serializable>> checkName(User user, String message) {
        // При проверке имени мы превентивно сохраняем пользователю новое имя в базе
        // идея для рефакторинга - добавить временное хранение имени
        user.setFirstName(message);
        userRepository.save(user);

        // Делаем кнопку для применения изменений
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = List.of(
                createInlineKeyboardButton("Accept", NAME_ACCEPT));

        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne));

        return List.of(createMessageTemplate(user)
                .setText(String.format("You have entered: %s%nIf this is correct - press the button", user.getFirstName()))
                .setReplyMarkup(inlineKeyboardMarkup));
    }

    private List<PartialBotApiMethod<? extends Serializable>> changeName(User user) {
        // При запросе изменения имени мы меняем State
        user.setUserState(UserState.ENTER_NAME);
        userRepository.save(user);

        // Создаем кнопку для отмены операции
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<InlineKeyboardButton> inlineKeyboardButtonsRowOne = List.of(
                createInlineKeyboardButton("Cancel", NAME_CHANGE_CANCEL));

        inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRowOne));

        return List.of(createMessageTemplate(user).setText(String.format(
                "Your current name is: %s%nEnter new name or press the button to continue", user.getFirstName()))
                .setReplyMarkup(inlineKeyboardMarkup));
    }

    @Override
    public List<UserState> operatedBotState() {
        return List.of(UserState.ENTER_NAME);
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return List.of(NAME_ACCEPT, NAME_CHANGE, NAME_CHANGE_CANCEL);
    }
}
