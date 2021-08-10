package com.tymofiivoitenko.telegram.bot.handler;

import com.tymofiivoitenko.telegram.model.user.UserState;
import com.tymofiivoitenko.telegram.model.meme.MemeReaction;
import com.tymofiivoitenko.telegram.model.user.User;
import com.tymofiivoitenko.telegram.repository.MemeReactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.tymofiivoitenko.telegram.repository.UserRepository;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import static com.tymofiivoitenko.telegram.util.TelegramUtil.createMessageTemplate;

@Slf4j
@Component
public class FinishTestHandler implements Handler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemeReactionRepository memeReactionRepository;

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {
        // If user is here, he wants to start from scratch. Change his status to None
        user.setState(UserState.NONE);
        userRepository.save(user);

        List<MemeReaction> allMemeReactions = memeReactionRepository.findAll();

        Optional<MemeReaction> lastMemeReaction = allMemeReactions.stream()
                .sorted((f1, f2) -> Long.compare(f2.getId(), f1.getId()))
                .findFirst();

        log.info("last meme reaction: " + lastMemeReaction.get());

        return List.of(createMessageTemplate(user)
                .setText(String.format("Теперь осталось узнать вкусы твоих знакомых и сравнить с твоими. Для этого отправь им свою личную ссылку на тест:\n" +
                        "\n" +
                        "t.me/sovmemstimost_bot?start= %d \n" +
                        "\n" +
                        "Когда они пройдут тест — тебе придут результаты, с кем именно и насколько у тебя совпадают вкусы на мемы.")));

    }

    @Override
    public List<UserState> operatedUserState() {
        return List.of(UserState.ENTER_NAME);
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return List.of();
    }
}
