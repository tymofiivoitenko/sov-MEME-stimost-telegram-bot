package com.tymofiivoitenko.telegram.bot.handler;

import com.tymofiivoitenko.telegram.model.meme.MemeImage;
import com.tymofiivoitenko.telegram.model.meme.MemeReaction;
import com.tymofiivoitenko.telegram.model.meme.MemeReactionState;
import com.tymofiivoitenko.telegram.model.meme.MemeTest;
import com.tymofiivoitenko.telegram.model.user.User;
import com.tymofiivoitenko.telegram.model.user.UserState;
import com.tymofiivoitenko.telegram.repository.MemeImageRepository;
import com.tymofiivoitenko.telegram.repository.MemeReactionRepository;
import com.tymofiivoitenko.telegram.repository.MemeTestRepository;
import com.tymofiivoitenko.telegram.repository.UserRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.tymofiivoitenko.telegram.util.TelegramUtil.*;

@Slf4j
@Component
public class MemeTestHandler implements Handler {

    // Store supported CallBackQuery as constants
    public static final String MEME_TEST_START = "/mem_test_start";
    public static final String MEME_IS_LIKED = "/meme_is_liked";
    public static final String MEME_IS_DISLIKED = "/meme_is_disliked";
    public static final String MEME_SUPER_LIKE_START = "/meme_super_like_start";
    public static final String MEME_TEST_COMPETITION_START = "/start ";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MemeTestRepository memTestRepository;

    @Autowired
    private MemeImageRepository memImageRepository;

    @Autowired
    private MemeReactionRepository memeReactionRepository;

    @Value("${meme-image.meme-folder}")
    public String urlPrefix;

    @Value("${meme-test.number-of-memes}")
    public int numberOfMemesInTest;

    //Meme reactions (thumbsup, thumbsdown)
    private static final List<String> OPTIONS = List.of(EmojiParser.parseToUnicode(":thumbsup:"), EmojiParser.parseToUnicode(":thumbsdown:"));

    @Override
    public List<PartialBotApiMethod<? extends Serializable>> handle(User user, String message) {

        log.info("MemeTestHandler: user " + user.getId() + " FULL message: <" + message + ">");

        if (message.startsWith(MEME_IS_LIKED) || message.startsWith(MEME_IS_DISLIKED)) {

            // Save meme reaction
            boolean memReactedSuccessfully = saveMemeReaction(message);

            if (!memReactedSuccessfully) {
                return memeWasAlreadyReacted(user);
            }

            // Get testId
            int memeTestId = Integer.valueOf(message.substring(message.indexOf("testId: ") + 8, message.indexOf("reactionId:")).trim());

            // Get next meme Reaction
            int memeReactionId = getNextReactionInTest(memeTestId, user);

            if (memeReactionId == -1) {
                // No more memes to reactions are left
                return finishTest(user, memeTestId);
            }

            // Send next meme to react
            return nextMemeReaction(user, memeTestId, memeReactionId);
        } else if (message.startsWith(MEME_TEST_COMPETITION_START)) {
            user.setState(UserState.MEME_TEST_COMPETITION);
            userRepository.save(user);
            log.info("User starts competition");

            return completeMemTest(user, message);
        } else if (message.startsWith(MEME_TEST_START)) {
            log.info("User starts new test");
            return startNewMemeTest(user);
        }

        throw new UnsupportedOperationException("Unexpected call back message:" + message);
    }

    private List<PartialBotApiMethod<? extends Serializable>> memeWasAlreadyReacted(User user) {
        String message = "";
        return List.of(createMessageTemplate(user)
                .setText(String.format(message)));
    }

    private List<PartialBotApiMethod<? extends Serializable>> completeMemTest(User user, String message) {
        int testId = Integer.valueOf(message.substring(message.indexOf("/start ") + 6).trim());

        MemeTest memeTest = memTestRepository.findById(testId).get();
        int createdByUserId = memeTest.getCreateByUser();

        log.info("User " + user.getId() + " Compete on test createdByUser id: " + createdByUserId);

        List<MemeReaction> oldMemeReactions = memeReactionRepository.getByMemeTestId(testId);

        // Check if current user passed this test before:
        Optional<MemeReaction> passedMemeReactionOptional = oldMemeReactions.stream()
                .filter(x -> x.getReactedByUser() == user.getId())
                .findAny();

        if (passedMemeReactionOptional.isPresent()) {
            user.setState(UserState.START);
            userRepository.save(user);
            String finishMessage = " Вы уже проходили тест: t.me/sovmemstimost\\_bot?start=" + testId + "\nНачните новый";
            List<InlineKeyboardButton> inlineKeyboardButtonsRow = List.of(
                    createInlineKeyboardButton("Начать тест на совМЕМстимость", MemeTestHandler.MEME_TEST_START));
            user.setState(UserState.MEME_TEST);
            userRepository.save(user);

            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            inlineKeyboardMarkup.setKeyboard(List.of(inlineKeyboardButtonsRow));

            return List.of(createMessageTemplate(user)
                    .setText(finishMessage)
                    .setReplyMarkup(inlineKeyboardMarkup));

        }

        // Collect only memes from the original test
        oldMemeReactions = oldMemeReactions.stream()
                .filter(x -> x.getReactedByUser() == createdByUserId)
                .collect(Collectors.toList());

        log.info("original meme reactions: ");
        oldMemeReactions.forEach(x -> log.info(x.toString()));

        int firstMemeReactionId = -1;
        for (MemeReaction memeReaction : oldMemeReactions) {
            firstMemeReactionId = memeReactionRepository.save(new MemeReaction(testId, memeReaction.getMemeImageId(), user.getId())).getId();
        }

        return nextMemeReaction(user, testId, firstMemeReactionId);
    }

    private List<PartialBotApiMethod<? extends Serializable>> finishTest(User userPassTest, int memeTestId) {

        int numberOfMatchReactions = 0;
        log.info("finishTest");

        if (userPassTest.getState().equals(UserState.MEME_TEST_COMPETITION)) {
            log.info("MEME_TEST_COMPETITION");

            MemeTest memeTest = memTestRepository.findById(memeTestId).get();

            log.info("memeTest:" + memeTest);
            log.info("createdBy:" + memeTest.getCreateByUser());
            int createdByUserId = memeTest.getCreateByUser();

            User createdByUser = userRepository.findById(createdByUserId).get();
            log.info("createdByUser: " + createdByUser);

            List<MemeReaction> allMemeReactionsForTestId = memeReactionRepository.getByMemeTestId(memeTestId);
            List<MemeReaction> reactionsByOriginalUser = allMemeReactionsForTestId.stream()
                    .filter(x -> (x.getReactedByUser() == createdByUserId))
                    .collect(Collectors.toList());
            List<MemeReaction> reactionsByCurrentUser = allMemeReactionsForTestId.stream()
                    .filter(x -> (x.getReactedByUser() == userPassTest.getId()))
                    .collect(Collectors.toList());

            for (MemeReaction originalMemeReaction : reactionsByOriginalUser) {
                MemeReaction reactionByCurrentUser = reactionsByCurrentUser.stream()
                        .filter(x -> (x.getMemeImageId() == originalMemeReaction.getMemeImageId()))
                        .findAny()
                        .get();

                if (originalMemeReaction.getMemeReactionState().equals(reactionByCurrentUser.getMemeReactionState())) {
                    log.info("MATCH, mem id:" + originalMemeReaction.getMemeImageId());
                    numberOfMatchReactions++;
                    continue;
                }
                log.info("no match, mem id: " + originalMemeReaction.getMemeImageId());
            }

            userPassTest.setState(UserState.START);
            BigDecimal matchPercentage = new BigDecimal("0");
            if (numberOfMatchReactions != 0) {
                matchPercentage = BigDecimal.valueOf(numberOfMatchReactions * 100 / numberOfMemesInTest).setScale(0, RoundingMode.UP);
            }

            // Create original user name = first name + last name (if not empty)
            String originalUserName = createdByUser.getFirstName();
            if (!Objects.isNull(createdByUser.getLastName())) {
                originalUserName = originalUserName + " " + createdByUser.getLastName();
            }

            String competingUserFinishMessage = String.format("Поздравляю! твой результат совMEMcтимости c *%s* - *%s*" + Character.toString(0xFF05), originalUserName, matchPercentage);
            log.info("competingUserfinishMessage: " + competingUserFinishMessage);

            String originalUserFinishMessage = String.format("Поздравляю! Пользователь *%s* только что прошел твой тест. Ваш результат совMEMcтимости - *%s*" + Character.toString(0xFF05), userPassTest.getFirstName(), matchPercentage);
            log.info("originalUserFinishMessage: " + originalUserFinishMessage);

            return List.of(createMessageTemplate(userPassTest).setText(String.format(competingUserFinishMessage)),
                    createMessageTemplate(createdByUser).setText(String.format(originalUserFinishMessage)));
        }

        userPassTest.setState(UserState.START);
        userRepository.save(userPassTest);
        String finishMessage = "Теперь осталось узнать вкусы твоих знакомых и сравнить с твоими. Для этого отправь им свою личную ссылку на тест: t.me/sovmemstimost\\_bot?start=" + memeTestId + "\n\nКогда они пройдут тест — тебе придут результаты, с кем именно и насколько у тебя совпадают вкусы на мемы.";
        log.info("FinishMessage: " + finishMessage);
        return List.of(createMessageTemplate(userPassTest)
                .setText(String.format(finishMessage)));
    }

    private boolean saveMemeReaction(String message) {
        MemeReactionState memReactionState = message.startsWith(MEME_IS_LIKED) ? MemeReactionState.LIKE : MemeReactionState.DISLIKE;
        int reactionID = Integer.valueOf(message.substring(message.indexOf("reactionId: ") + 12).trim());

        log.info("Реагируем на: " + reactionID);
        MemeReaction memeReaction = memeReactionRepository.findById(reactionID).get();

        // Check if this meme was already reacted
        if (!memeReaction.getMemeReactionState().equals(MemeReactionState.NONE)) {
            log.info("This message has been already reacted");
            return false;
        }

        memeReaction.setMemeReactionState(memReactionState);
        memeReactionRepository.save(memeReaction);

        return true;
    }

    private List<PartialBotApiMethod<? extends Serializable>> startNewMemeTest(User user) {
        log.info("Sstart new test");

        // Create new test
        MemeTest memeTest = new MemeTest();
        memeTest.setCreateByUser(user.getId());
        int testId = memTestRepository.save(memeTest).getId();

        // Choose memes to show to user and create reactions for them with status none
        List<Integer> memIds = memImageRepository.getRandomMemes();
        List<MemeReaction> emptyMemeReactions = new ArrayList<>();

        int firstMemeReactionId = -1;

        // Save new reactions with status None
        for (int i = 0; i < numberOfMemesInTest; i++) {
            MemeReaction memeReaction = new MemeReaction();
            memeReaction.setMemeTestId(testId);
            memeReaction.setMemeReactionState(MemeReactionState.NONE);
            memeReaction.setMemeImageId(memIds.get(i));
            memeReaction.setReactedByUser(user.getId());
            emptyMemeReactions.add(memeReaction);
        }

        // Get id of first saved meme reaction
        firstMemeReactionId = memeReactionRepository.saveAll(emptyMemeReactions).get(0).getId();
        return nextMemeReaction(user, testId, firstMemeReactionId);
    }

    private List<PartialBotApiMethod<? extends Serializable>> nextMemeReaction(User user, int memeTestId, int memeReactionId) {
        log.info("Start next reaction. Data in tg button: memeTestId=" + memeTestId + " memeReactionId = " + memeReactionId);

        // Create Markup of Like and Dislike buttons
        InlineKeyboardMarkup inlineKeyboardMarkup = createMemeEvaluationMarkup(memeTestId, memeReactionId);

        // Load meme image
        MemeReaction memeReaction = memeReactionRepository.findById(memeReactionId).get();
        MemeImage memeImage = memImageRepository.findById(memeReaction.getMemeImageId()).get();
        InputStream memImageIS = getMemeImage(memeImage.getUrl());

        return List.of(createPhotoTemplate(user)
                .setPhoto("mem", memImageIS)
                .setReplyMarkup(inlineKeyboardMarkup));
    }

    private int getNextReactionInTest(int memeTestId, User user) {
        int memeReactionId = -1;
        try {
            List<MemeReaction> allMemeReactions = memeReactionRepository.getByMemeTestId(memeTestId);

            Optional<MemeReaction> nextMemReaction = allMemeReactions.stream()
                    .filter(x -> x.getMemeReactionState() == MemeReactionState.NONE && x.getReactedByUser() == user.getId())
                    .findAny();

            memeReactionId = nextMemReaction.isPresent() ? nextMemReaction.get().getId() : -1;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return memeReactionId;
    }

    private InputStream getMemeImage(@NotNull String url) {
        InputStream image = null;

        try {
            image = new URL(urlPrefix + url).openStream();
       } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return image;
    }

    private static void getAllFiles(File curDir) {

        File[] filesList = curDir.listFiles();
        for(File f : filesList){
            log.info(f.getAbsolutePath());
            if(f.isDirectory())
                log.info("IS DIRECTORY");
            if(f.isFile()){
                log.info("IS FILE");
            }
        }
    }

    public void getAllNestedFiles(String directoryName, List<File> files) {
        File directory = new File(directoryName);

        // Get all files from a directory.
        File[] fList = directory.listFiles();
        if(fList != null)
            for (File file : fList) {
                if (file.isFile()) {
                    files.add(file);
                } else if (file.isDirectory()) {
                    getAllNestedFiles(file.getAbsolutePath(), files);
                }
            }
    }

    private InlineKeyboardMarkup createMemeEvaluationMarkup(int memeTestId, int memeReactionId) {
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
    public List<UserState> operatedUserState() {
        return List.of(UserState.MEME_TEST, UserState.MEME_TEST_COMPETITION);
    }

    @Override
    public List<String> operatedCallBackQuery() {
        return List.of(MEME_TEST_COMPETITION_START, MEME_TEST_START, MEME_IS_LIKED, MEME_IS_DISLIKED, MEME_SUPER_LIKE_START);
    }
}
