package ru.pb.netchatclient.utils;

import java.util.ArrayList;

public class StringUtils {
    public static String wrapText(String text, int lettersCount) {

        String arrWords[] = text.split(" ");  // Массив слов
        ArrayList<String> arrPhrases = new ArrayList<>(); // Коллекция подстрок(фраз)
        StringBuilder stringBuffer = new StringBuilder(); // Буфер для накопления фразы
        int cnt = 0;   // Счётчик, чтобы не выйти за пределы 30 символов
        int index = 0; // Индекс элемента в массиве arrWords. Сразу указывает на первый элемент
        int length = arrWords.length; // Общее количество слов (длина массива)
        while (index != length) {  // Пока не дойдём до последнего элемента
            if (arrWords[index].length() > lettersCount) {
                arrPhrases.add(arrWords[index].substring(0, lettersCount));
                arrWords[index] = arrWords[index].substring(lettersCount);
                continue;
            }
            if (cnt + arrWords[index].length() <= lettersCount) { // Если текущая фраза + текущее слово в массиве arrWords не превышает 30
                cnt += arrWords[index].length() + 1;  // То увеличиваем счётчик
                stringBuffer.append(arrWords[index]).append(" ");  // и накапливаем фразу
                index++;   // Переходим на следующее слово
            } else {   // Фраза превысит лимит в 30 символов
                arrPhrases.add(stringBuffer.toString());   // Добавляем фразу в коллекцию
                stringBuffer = new StringBuilder();
                cnt = 0;                                   // Обнуляем счётчик
            }

        }

        if (stringBuffer.length() > 0) {
            arrPhrases.add(stringBuffer.toString());       // Забираем "остатки"
        }

        StringBuilder result = new StringBuilder();
        for (String elem : arrPhrases) {
            result.append(elem);
            result.append('\n');
        }

        return result.toString();
    }
}
