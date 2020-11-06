package org.telekit.base.service.impl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.telekit.base.BaseSetup;
import org.telekit.base.service.Transliterator;

import java.util.List;
import java.util.stream.Stream;

import static org.telekit.base.TestUtils.loadResourceBundle;

@SuppressWarnings("SpellCheckingInspection")
@ExtendWith(BaseSetup.class)
public class RUTransliteratorTest {

    private final Transliterator tr = new RUTransliterator();

    public RUTransliteratorTest() {
        loadResourceBundle();
    }

    @ParameterizedTest
    @MethodSource("directTranslationProvider")
    public void testDirectTranslation(Entry entry) {
        Assertions.assertThat(tr.transliterate(entry.word)).isEqualTo(entry.translation);
        Assertions.assertThat(tr.transliterate(entry.word.toUpperCase())).isEqualTo(entry.translation.toUpperCase());
    }

    @ParameterizedTest
    @MethodSource("exceptionsTranslationProvider")
    public void testExceptionsTranslation(Entry entry) {
        Assertions.assertThat(tr.transliterate(entry.word)).isEqualTo(entry.translation);
        Assertions.assertThat(tr.transliterate(entry.word.toUpperCase())).isEqualTo(entry.translation.toUpperCase());
    }

    @Test
    public void testSentences() {
        List<Entry> entries = List.of(
                new Entry("Не знаю где, но не у нас,", "Ne znayu gde, no ne u nas,"),
                new Entry("Достопочтенный лорд Мидас,", "Dostopochtenny lord Midas,"),
                new Entry("С душой посредственной и низкой, —", "S dushoy posredstvennoy i nizkoy, —"),
                new Entry("Чтоб не упасть дорогой склизкой,", "Chtob ne upast dorogoy sklizkoy,"),
                new Entry("Ползком прополз в известный чин", "Polzkom propolz v izvestny chin"),
                new Entry("И стал известный господин.", "I stal izvestny gospodin."),
                new Entry("Еще два слова об Мидасе:", "Yeshche dva slova ob Midase:"),
                new Entry("Он не хранил в своем запасе", "On ne khranil v svoyem zapase"),
                new Entry("Глубоких замыслов и дум,", "Glubokikh zamyslov i dum,"),
                new Entry("Имел он не блестящий ум,", "Imel on ne blestyashchy um,"),
                new Entry("Душой не слишком был отважен,", "Dushoy ne slishkom byl otvazhen,"),
                new Entry("Зато был сух, учтив и важен.", "Zato byl sukh, uchtiv i vazhen.")
        );

        assertAll(entries);
    }

    @Test
    public void testLoremIpsum() {
        String loremIpsum = "\"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.\"";
        Assertions.assertThat(tr.transliterate(loremIpsum)).isEqualTo(loremIpsum);
    }

    @Test
    public void testUpperCase() {
        List<Entry> entries = List.of(
                new Entry("ЖЕЛЕЗНОДОРОЖНИК", "ZHELEZNODOROZHNIK"),
                new Entry("РУКИ ВВЕРХ", "RUKI VVERKH"),
                new Entry("ВПЕРЁД К ПОБЕДЕ", "VPERYOD K POBEDE"),
                new Entry("ЁПРСТ Х ЁКЛМН", "YOPRST KH YOKLMN"),
                new Entry("ЕЛЬЦИН - ПЕРВЫЙ ПРЕЗИДЕНТ РОССИИ", "YELTSIN - PERVY PREZIDENT ROSSII")
        );

        assertAll(entries);
    }

    @Test
    public void testMixedCase() {
        List<Entry> entries = List.of(
                new Entry("НеСтойПодСтрелой", "NeStoyPodStreloy"),
                new Entry("неВлезайУбъет", "neVlezayUbyet"),
                new Entry("ЕльцинПервыйПрезидентРоссии", "YeltsinPervyyPrezidentRossii"),
                new Entry("ЧеБуРашКа", "CheBuRashKa")
        );

        assertAll(entries);
    }

    public void assertAll(List<Entry> entries) {
        for (Entry entry : entries) {
            Assertions.assertThat(tr.transliterate(entry.word)).isEqualTo(entry.translation);
        }
    }

    public static Stream<Entry> directTranslationProvider() {
        return List.of(
                new Entry("Аликово", "Alikovo"),
                new Entry("Поганкино", "Pogankino"),
                new Entry("Болотин", "Bolotin"),
                new Entry("Колбасин", "Kolbasin"),
                new Entry("Воронин", "Voronin"),
                new Entry("Привалин", "Privalin"),
                new Entry("Галкин", "Galkin"),
                new Entry("Луговой", "Lugovoy"),
                new Entry("Дровяное", "Drovyanoye"),
                new Entry("Подгорск", "Podgorsk"),
                new Entry("Белкин", "Belkin"),
                new Entry("Ёлкино", "Yolkino"),
                new Entry("Озёрск", "Ozyorsk"),
                new Entry("Жиров", "Zhirov"),
                new Entry("Приволжское", "Privolzhskoye"),
                new Entry("Зорин", "Zorin"),
                new Entry("Обозов", "Obozov"),
                new Entry("Иркутск", "Irkutsk"),
                new Entry("Владивосток", "Vladivostok"),
                new Entry("Йошкар-Ола", "Yoshkar-Ola"),
                new Entry("Бийск", "Biysk"),
                new Entry("Киров", "Kirov"),
                new Entry("Галкин", "Galkin"),
                new Entry("Лапинск", "Lapinsk"),
                new Entry("Комсомольск", "Komsomolsk"),
                new Entry("Мичурин", "Michurin"),
                new Entry("Колыма", "Kolyma"),
                new Entry("Нальчик", "Nalchik"),
                new Entry("Савино", "Savino"),
                new Entry("Оха", "Okha"),
                new Entry("Грозный", "Grozny"),
                new Entry("Петроград", "Petrograd"),
                new Entry("Ставрополь", "Stavropol"),
                new Entry("Родниковое", "Rodnikovoye"),
                new Entry("Высокогорск", "Vysokogorsk"),
                new Entry("Ступино", "Stupino"),
                new Entry("Бирск", "Birsk"),
                new Entry("Тавричанка", "Tavrichanka"),
                new Entry("Ростов", "Rostov"),
                new Entry("Улетайск", "Uletaysk"),
                new Entry("Шушенское", "Shushenskoye"),
                new Entry("Фёдоровка", "Fyodorovka"),
                new Entry("Уфа", "Ufa"),
                new Entry("Хабаровск", "Khabarovsk"),
                new Entry("Оха", "Okha"),
                new Entry("Царское", "Tsarskoye"),
                new Entry("Черемшаны", "Cheremshany"),
                new Entry("Зареченск", "Zarechensk"),
                new Entry("Шадрин", "Shadrin"),
                new Entry("Моршанск", "Morshansk"),
                new Entry("Щукино", "Shchukino"),
                new Entry("Элиста", "Elista"),
                new Entry("Тетраэдральный", "Tetraedralny"),
                new Entry("Южный", "Yuzhny"),
                new Entry("Вилючинск", "Vilyuchinsk"),
                new Entry("Ярославль", "Yaroslavl"),
                new Entry("Бурянск", "Buryansk")
        ).stream();
    }

    public static Stream<Entry> exceptionsTranslationProvider() {
        return List.of(
                new Entry("Зарецкий", "Zaretsky"),
                new Entry("Рощинский", "Roshchinsky"),
                new Entry("Подъярский", "Podyarsky"),
                new Entry("Ельцин", "Yeltsin"),
                new Entry("Раздольное", "Razdolnoye"),
                new Entry("Юрьев", "Yuryev"),
                new Entry("Подъездной", "Podyezdnoy"),
                new Entry("Мусийкъонгийкоте", "Musiykyongiykote"),
                new Entry("Усолье", "Usolye"),
                new Entry("Выхухоль", "Vykhukhol"),
                new Entry("Дальнегорск", "Dalnegorsk"),
                new Entry("Ильинский", "Ilyinsky"),
                new Entry("Синий", "Siny"),
                new Entry("Набережные Челны", "Naberezhnye Chelny")
        ).stream();
    }

    public static class Entry {

        public final String word;
        public final String translation;

        public Entry(String word, String translation) {
            this.word = word;
            this.translation = translation;
        }

        @Override
        public String toString() {
            return word + "=" + translation;
        }
    }
}
