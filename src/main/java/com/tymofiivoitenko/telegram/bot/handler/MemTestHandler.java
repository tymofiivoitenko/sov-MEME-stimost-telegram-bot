package com.tymofiivoitenko.telegram.bot.handler;

import com.tymofiivoitenko.telegram.bot.state.memReactionState.MemReactionState;
import com.tymofiivoitenko.telegram.bot.state.userState.UserState;
import com.tymofiivoitenko.telegram.model.MemImage;
import com.tymofiivoitenko.telegram.model.MemReaction;
import com.tymofiivoitenko.telegram.model.MemTest;
import com.tymofiivoitenko.telegram.model.User;
import com.tymofiivoitenko.telegram.repository.JpaMemImageRepository;
import com.tymofiivoitenko.telegram.repository.JpaMemReactionRepository;
import com.tymofiivoitenko.telegram.repository.JpaMemTestRepository;
import com.tymofiivoitenko.telegram.repository.JpaUserRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.tymofiivoitenko.telegram.util.TelegramUtil.*;

@Slf4j
@Component
public class MemTestHandler implements Handler {
    //Храним поддерживаемые CallBackQuery в виде констант
    public static final String MEME_TEST_START = "/mem_test_start";
    public static final String MEME_IS_LIKED = "/meme_is_liked";
    public static final String MEME_IS_DISLIKED = "/meme_is_disliked";
    public static final String MEME_SUPER_LIKE_START = "/meme_super_like_start";
    public static final String MEME_TEST_COMPETION_START = "/start ";

    public static final String urlPrefix = "file:///Users/tymofiivoitenko/SovmemstimostBot/src/main/resources/memes/";


    @Value("${mem-test.numberOfMems}")
    public int numberOfMemesInTest;

    //Храним варианты ответа
    private static final List<String> OPTIONS = List.of(EmojiParser.parseToUnicode(":thumbsup:"), EmojiParser.parseToUnicode(":thumbsdown:"));

    private final JpaUserRepository userRepository;
    private final JpaMemTestRepository memTestRepository;
    private final JpaMemImageRepository memImageRepository;
    private final JpaMemReactionRepository memReactionRepository;

    public MemTestHandler(JpaUserRepository userRepository, JpaMemTestRepository memTestRepository, JpaMemImageRepository memImageRepository, JpaMemReactionRepository memReactionRepository) {
        this.userRepository = userRepository;
        this.memTestRepository = memTestRepository;
        this.memImageRepository = memImageRepository;
        this.memReactionRepository = memReactionRepository;
    }

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {

        System.out.println("MemTestHandler: user " + user.getId() + " FULL message: <" + message + ">");

        if (message.startsWith(MEME_IS_LIKED) || message.startsWith(MEME_IS_DISLIKED)) {

            // Save meme reaction
            saveMemeReaction(message);

            // Get testId
            int memeTestId = Integer.valueOf(message.substring(message.indexOf("testId: ") + 8, message.indexOf("reactionId:")).trim());

            // Get next meme Reaction
            int memeReactionId = getNextReactionInTest(memeTestId, user);

            if (memeReactionId == 0) {
                // no more memes to reactions are left
                return finishTest(user, memeTestId);
            }

            // Send next meme to react
            return nextMemeReaction(user, memeTestId, memeReactionId);
        } else if (message.startsWith(MEME_TEST_COMPETION_START)) {
            System.out.println("WE compete");
            return competeMemTest(user, message);
        } else if (message.startsWith(MEME_TEST_START)) {
            System.out.println("WE start new test");
            return startNewMemTest(user);
        }

        throw new UnsupportedOperationException();
    }

    private List<PartialBotApiMethod<? extends Serializable>> competeMemTest(User user, String message) {
        int testId = Integer.valueOf(message.substring(message.indexOf("/start ") + 6).trim());

        MemTest memtest = memTestRepository.findById(testId).get();
        int createdByUserId = memtest.getCreateByUser();

        System.out.println("User " + user.getId() + " Compete on test createdByUser id: " + createdByUserId);

        List<MemReaction> oldMemReactions = memReactionRepository.getByMemTestId(testId);

        // Check if current user passed this test before:
        Optional<MemReaction> passedMemeReactionOptional = oldMemReactions.stream()
                .filter(x -> x.getReactedByUser() == user.getId())
                .findAny();

        if (passedMemeReactionOptional.isPresent()) {
            user.setUserState(UserState.START);
            userRepository.save(user);
            String finishMessage = " Вы уже проходили тест: t.me/sovmemstimost\\_bot?start=" + testId + "\nНачните новый";
            List<InlineKeyboardButton> inlineKeyboardButtonsRow = List.of(
                    createInlineKeyboardButton("Начать тест на совМЕМстимость", MemTestHandler.MEME_TEST_START));
            user.setUserState(UserState.MEM_TEST);
            userRepository.save(user);

            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRow));

            return List.of(createMessageTemplate(user)
                    .setText(finishMessage)
                    .setReplyMarkup(inlineKeyboardMarkup));

        }

        // Collect only memes from the original test
        oldMemReactions = oldMemReactions.stream()
                .filter(x -> x.getReactedByUser() == createdByUserId)
                .collect(Collectors.toList());

        System.out.println("original meme reactions: ");
        oldMemReactions.forEach(System.out::println);

        int firstMemeReactionId = 0;
        for (MemReaction memReaction : oldMemReactions) {
            firstMemeReactionId = memReactionRepository.save(new MemReaction(testId, memReaction.getMemImageId(), user.getId())).getId();
        }

        return nextMemeReaction(user, testId, firstMemeReactionId);
    }

    private List<PartialBotApiMethod<? extends Serializable>> finishTest(User user, int memeTestId) {

        int numberOfMatchReactions = 0;
        System.out.println("=finishTest:");

        if (user.getUserState().equals(UserState.MEM_TEST_COMETETION)) {
            System.out.println("MEM_TEST_COMETETION");

            MemTest memtest = memTestRepository.findById(memeTestId).get();

            System.out.println("memtest:" + memtest);
            System.out.println("createdBy:" + memtest.getCreateByUser());
            int createdByUserId = memtest.getCreateByUser();

            User createdByUser = userRepository.findById(createdByUserId).get();
            System.out.println("createdByUser: " + createdByUser);

            List<MemReaction> allMemReactionsForTestId = memReactionRepository.getByMemTestId(memeTestId);
            List<MemReaction> reactionsByOriginalUser = allMemReactionsForTestId.stream()
                    .filter(x -> (x.getReactedByUser() == createdByUserId))
                    .collect(Collectors.toList());
            List<MemReaction> reactionsByCurrentUser = allMemReactionsForTestId.stream()
                    .filter(x -> (x.getReactedByUser() == user.getId()))
                    .collect(Collectors.toList());

            for (MemReaction originalMemReaction : reactionsByOriginalUser) {
                MemReaction reactionByCurrentUser = reactionsByCurrentUser.stream()
                        .filter(x -> (x.getMemImageId() == originalMemReaction.getMemImageId()))
                        .findAny()
                        .get();

                if (originalMemReaction.getMemReactionState().equals(reactionByCurrentUser.getMemReactionState())) {
                    System.out.println("MATCH");
                    numberOfMatchReactions++;
                    continue;
                }
                System.out.println("Doesn't match");
            }

            user.setUserState(UserState.START);
            BigDecimal matchPercentage = new BigDecimal("0");
            if (numberOfMatchReactions != 0) {
                matchPercentage = BigDecimal.valueOf(numberOfMatchReactions * 100 / numberOfMemesInTest).setScale(0, RoundingMode.UP);
            }

            String finishMessage = String.format("Поздравляю! твой результат совмемcтимости c *%s* - *%s*" + Character.toString(0xFF05), createdByUser.getFirstName(), matchPercentage);
            System.out.println("finishMessage: " + finishMessage);
            return List.of(createMessageTemplate(user)
                    .setText(String.format(finishMessage)));
        }

        user.setUserState(UserState.START);
        userRepository.save(user);
        String finishMessage = "Теперь осталось узнать вкусы твоих знакомых и сравнить с твоими. Для этого отправь им свою личную ссылку на тест: t.me/sovmemstimost\\_bot?start=" + memeTestId + "\n\nКогда они пройдут тест — тебе придут результаты, с кем именно и насколько у тебя совпадают вкусы на мемы.";
        return List.of(createMessageTemplate(user)
                .setText(String.format(finishMessage)));
    }

    private void saveMemeReaction(String message) {
        MemReactionState memReactionState = message.startsWith(MEME_IS_LIKED) ? MemReactionState.LIKE : MemReactionState.DISLIKE;
        int reactionID = Integer.valueOf(message.substring(message.indexOf("reactionId: ") + 12).trim());

        System.out.println("Реагируем на: " + reactionID);
        MemReaction memeReaction = memReactionRepository.findById(reactionID).get();

        memeReaction.setMemReactionState(memReactionState);
        memReactionRepository.save(memeReaction);
    }

    private List<PartialBotApiMethod<? extends Serializable>> startNewMemTest(User user) {
        System.out.println("начинаем test");

        // Create new test
        MemTest memTest = new MemTest();
        memTest.setCreateByUser(user.getId());
        int testId = memTestRepository.save(memTest).getId();

        // Choose memes to show to user and create reactions for them with status none
        List<Integer> memIds = memImageRepository.getRandomMems();

        int firstMemeReactionId = 0;

        // Save new reactions with status None
        for (int i = 0; i < numberOfMemesInTest; i++) {
            MemReaction memReaction = new MemReaction();
            memReaction.setMemTestId(testId);
            memReaction.setMemReactionState(MemReactionState.NONE);
            memReaction.setMemImageId(memIds.get(i));
            memReaction.setReactedByUser(user.getId());
            firstMemeReactionId = memReactionRepository.save(memReaction).getId();
        }

        return nextMemeReaction(user, testId, firstMemeReactionId);
    }

    private List<PartialBotApiMethod<? extends Serializable>> nextMemeReaction(User user, int memeTestId, int memeReactionId) {
        System.out.println("начинаем следующий реакцию пишу в кнопку: memeTestId=" + memeTestId + " memeReactionId = " + memeReactionId);

        // Create Markup of Like and Dislike buttons
        InlineKeyboardMarkup inlineKeyboardMarkup = createLikeDislikeMarkup(memeTestId, memeReactionId);

        // Load meme image
        MemReaction memReaction = memReactionRepository.findById(memeReactionId).get();
        MemImage memImage = memImageRepository.findById(memReaction.getMemImageId()).get();
        InputStream memImageIS = getMemImage(memImage.getUrl());

        return List.of(createPhotoTemplate(user)
                .setPhoto("mem", memImageIS)
                .setReplyMarkup(inlineKeyboardMarkup));
    }

    private int getNextReactionInTest(int memeTestId, User user) {
        int memeReactionId = 0;
        try {
            List<MemReaction> allMemeReactions = memReactionRepository.getByMemTestId(memeTestId);

            Optional<MemReaction> nextMemReaction = allMemeReactions.stream()
                    .filter(x -> x.getMemReactionState() == MemReactionState.NONE && x.getReactedByUser() == user.getId())
                    .findAny();

            memeReactionId = nextMemReaction.isPresent() ? nextMemReaction.get().getId() : 0;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return memeReactionId;
    }

    private InputStream getMemImage(@NotNull String url) {
        InputStream image = null;

        try {
            image = new URL(urlPrefix + url).openStream();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return image;
    }

    private InlineKeyboardMarkup createLikeDislikeMarkup(int memeTestId, int memeReactionId) {
        List<InlineKeyboardButton> inlineKeyboardButtonsRow = new ArrayList<>();
        String likeCallbackData = MEME_IS_LIKED;
        String dislikeCallbackData = MEME_IS_DISLIKED;

        InlineKeyboardButton likeButton = new InlineKeyboardButton()
                .setText(OPTIONS.get(0))
                .setCallbackData(String.format("%s testId: %d reactionId: %d", likeCallbackData, memeTestId, memeReactionId));


        InlineKeyboardButton dislikeButton = new InlineKeyboardButton()
                .setText(OPTIONS.get(1))
                .setCallbackData(String.format("%s testId: %d reactionId: %d", dislikeCallbackData, memeTestId, memeReactionId));

        inlineKeyboardButtonsRow.add(likeButton);
        inlineKeyboardButtonsRow.add(dislikeButton);

        return new InlineKeyboardMarkup().setKeyboard(List.of(inlineKeyboardButtonsRow));
    }

    @Override
    public List<UserState> operatedBotState() {
        return List.of(UserState.MEM_TEST, UserState.MEM_TEST_COMETETION);
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return List.of(MEME_TEST_COMPETION_START, MEME_TEST_START, MEME_IS_LIKED, MEME_IS_DISLIKED, MEME_SUPER_LIKE_START);
    }
}
