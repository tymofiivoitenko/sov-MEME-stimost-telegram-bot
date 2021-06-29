package com.tymofiivoitenko.telegram.util;

import com.tymofiivoitenko.telegram.model.User;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.File;
import java.io.FileInputStream;

public class TelegramUtil {
    public static SendMessage createMessageTemplate(User user) {
        return createMessageTemplate(String.valueOf(user.getChatId()));
    }

    // Создаем шаблон SendMessage с включенным Markdown
    public static SendMessage createMessageTemplate(String chatId) {
        return new SendMessage()
                .setChatId(chatId)
                .enableMarkdown(true);
    }

    public static SendPhoto createPhotoTemplate(User user) {
        return createPhotoTemplate(String.valueOf(user.getChatId()));
    }

    // Создаем шаблон SendMessage с включенным Markdown
    public static SendPhoto createPhotoTemplate(String chatId) {

        return new SendPhoto()
                .setChatId(chatId);

    }

    // Создаем кнопку
    public static InlineKeyboardButton createInlineKeyboardButton(String text, String command) {
        return new InlineKeyboardButton()
                .setText(text)
                .setCallbackData(command);
    }
}