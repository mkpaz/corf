package org.telekit.base.util.transliterate;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.telekit.base.service.RUTransliterator;
import org.telekit.base.service.Transliterator;

@SuppressWarnings("SpellCheckingInspection")
class RUTransliteratorTest {

    private final Transliterator tr = new RUTransliterator();

    @Test
    public void testDirect() {
        Assertions.assertThat(tr.transliterate("Аликово")).isEqualTo("Alikovo");
        Assertions.assertThat(tr.transliterate("Поганкино")).isEqualTo("Pogankino");
        Assertions.assertThat(tr.transliterate("Болотин")).isEqualTo("Bolotin");
        Assertions.assertThat(tr.transliterate("Колбасин")).isEqualTo("Kolbasin");
        Assertions.assertThat(tr.transliterate("Воронин")).isEqualTo("Voronin");
        Assertions.assertThat(tr.transliterate("Привалин")).isEqualTo("Privalin");
        Assertions.assertThat(tr.transliterate("Галкин")).isEqualTo("Galkin");
        Assertions.assertThat(tr.transliterate("Луговой")).isEqualTo("Lugovoy");
        Assertions.assertThat(tr.transliterate("Дровяное")).isEqualTo("Drovyanoye");
        Assertions.assertThat(tr.transliterate("Подгорск")).isEqualTo("Podgorsk");
        Assertions.assertThat(tr.transliterate("Белкин")).isEqualTo("Belkin");
        Assertions.assertThat(tr.transliterate("Ёлкино")).isEqualTo("Yolkino");
        Assertions.assertThat(tr.transliterate("Озёрск")).isEqualTo("Ozyorsk");
        Assertions.assertThat(tr.transliterate("Жиров")).isEqualTo("Zhirov");
        Assertions.assertThat(tr.transliterate("Приволжское")).isEqualTo("Privolzhskoye");
        Assertions.assertThat(tr.transliterate("Зорин")).isEqualTo("Zorin");
        Assertions.assertThat(tr.transliterate("Обозов")).isEqualTo("Obozov");
        Assertions.assertThat(tr.transliterate("Иркутск")).isEqualTo("Irkutsk");
        Assertions.assertThat(tr.transliterate("Владивосток")).isEqualTo("Vladivostok");
        Assertions.assertThat(tr.transliterate("Йошкар-Ола")).isEqualTo("Yoshkar-Ola");
        Assertions.assertThat(tr.transliterate("Бийск")).isEqualTo("Biysk");
        Assertions.assertThat(tr.transliterate("Киров")).isEqualTo("Kirov");
        Assertions.assertThat(tr.transliterate("Галкин")).isEqualTo("Galkin");
        Assertions.assertThat(tr.transliterate("Лапинск")).isEqualTo("Lapinsk");
        Assertions.assertThat(tr.transliterate("Комсомольск")).isEqualTo("Komsomolsk");
        Assertions.assertThat(tr.transliterate("Мичурин")).isEqualTo("Michurin");
        Assertions.assertThat(tr.transliterate("Колыма")).isEqualTo("Kolyma");
        Assertions.assertThat(tr.transliterate("Нальчик")).isEqualTo("Nalchik");
        Assertions.assertThat(tr.transliterate("Савино")).isEqualTo("Savino");
        Assertions.assertThat(tr.transliterate("Оха")).isEqualTo("Okha");
        Assertions.assertThat(tr.transliterate("Грозный")).isEqualTo("Grozny");
        Assertions.assertThat(tr.transliterate("Петроград")).isEqualTo("Petrograd");
        Assertions.assertThat(tr.transliterate("Ставрополь")).isEqualTo("Stavropol");
        Assertions.assertThat(tr.transliterate("Родниковое")).isEqualTo("Rodnikovoye");
        Assertions.assertThat(tr.transliterate("Высокогорск")).isEqualTo("Vysokogorsk");
        Assertions.assertThat(tr.transliterate("Ступино")).isEqualTo("Stupino");
        Assertions.assertThat(tr.transliterate("Бирск")).isEqualTo("Birsk");
        Assertions.assertThat(tr.transliterate("Тавричанка")).isEqualTo("Tavrichanka");
        Assertions.assertThat(tr.transliterate("Ростов")).isEqualTo("Rostov");
        Assertions.assertThat(tr.transliterate("Улетайск")).isEqualTo("Uletaysk");
        Assertions.assertThat(tr.transliterate("Шушенское")).isEqualTo("Shushenskoye");
        Assertions.assertThat(tr.transliterate("Фёдоровка")).isEqualTo("Fyodorovka");
        Assertions.assertThat(tr.transliterate("Уфа")).isEqualTo("Ufa");
        Assertions.assertThat(tr.transliterate("Хабаровск")).isEqualTo("Khabarovsk");
        Assertions.assertThat(tr.transliterate("Оха")).isEqualTo("Okha");
        Assertions.assertThat(tr.transliterate("Царское")).isEqualTo("Tsarskoye");
        Assertions.assertThat(tr.transliterate("Черемшаны")).isEqualTo("Cheremshany");
        Assertions.assertThat(tr.transliterate("Зареченск")).isEqualTo("Zarechensk");
        Assertions.assertThat(tr.transliterate("Шадрин")).isEqualTo("Shadrin");
        Assertions.assertThat(tr.transliterate("Моршанск")).isEqualTo("Morshansk");
        Assertions.assertThat(tr.transliterate("Щукино")).isEqualTo("Shchukino");
        Assertions.assertThat(tr.transliterate("Элиста")).isEqualTo("Elista");
        Assertions.assertThat(tr.transliterate("Тетраэдральный")).isEqualTo("Tetraedralny");
        Assertions.assertThat(tr.transliterate("Южный")).isEqualTo("Yuzhny");
        Assertions.assertThat(tr.transliterate("Вилючинск")).isEqualTo("Vilyuchinsk");
        Assertions.assertThat(tr.transliterate("Ярославль")).isEqualTo("Yaroslavl");
        Assertions.assertThat(tr.transliterate("Бурянск")).isEqualTo("Buryansk");
    }

    @Test
    public void testExceptions() {
        Assertions.assertThat(tr.transliterate("Зарецкий")).isEqualTo("Zaretsky");
        Assertions.assertThat(tr.transliterate("Рощинский")).isEqualTo("Roshchinsky");
        Assertions.assertThat(tr.transliterate("Подъярский")).isEqualTo("Podyarsky");
        Assertions.assertThat(tr.transliterate("Ельцин")).isEqualTo("Yeltsin");
        Assertions.assertThat(tr.transliterate("Раздольное")).isEqualTo("Razdolnoye");
        Assertions.assertThat(tr.transliterate("Юрьев")).isEqualTo("Yuryev");
        Assertions.assertThat(tr.transliterate("Подъездной")).isEqualTo("Podyezdnoy");
        Assertions.assertThat(tr.transliterate("Мусийкъонгийкоте")).isEqualTo("Musiykyongiykote");
        Assertions.assertThat(tr.transliterate("Усолье")).isEqualTo("Usolye");
        Assertions.assertThat(tr.transliterate("Выхухоль")).isEqualTo("Vykhukhol");
        Assertions.assertThat(tr.transliterate("Дальнегорск")).isEqualTo("Dalnegorsk");
        Assertions.assertThat(tr.transliterate("Ильинский")).isEqualTo("Ilyinsky");
        Assertions.assertThat(tr.transliterate("Синий")).isEqualTo("Siny");
        Assertions.assertThat(tr.transliterate("Набережные Челны")).isEqualTo("Naberezhnye Chelny");
    }

    @Test
    public void testSentences() {
        Assertions.assertThat(tr.transliterate("Не знаю где, но не у нас,")).isEqualTo("Ne znayu gde, no ne u nas,");
        Assertions.assertThat(tr.transliterate("Достопочтенный лорд Мидас,")).isEqualTo("Dostopochtenny lord Midas,");
        Assertions.assertThat(tr.transliterate("С душой посредственной и низкой, —")).isEqualTo("S dushoy posredstvennoy i nizkoy, —");
        Assertions.assertThat(tr.transliterate("Чтоб не упасть дорогой склизкой,")).isEqualTo("Chtob ne upast dorogoy sklizkoy,");
        Assertions.assertThat(tr.transliterate("Ползком прополз в известный чин")).isEqualTo("Polzkom propolz v izvestny chin");
        Assertions.assertThat(tr.transliterate("И стал известный господин.")).isEqualTo("I stal izvestny gospodin.");
        Assertions.assertThat(tr.transliterate("Еще два слова об Мидасе:")).isEqualTo("Yeshche dva slova ob Midase:");
        Assertions.assertThat(tr.transliterate("Он не хранил в своем запасе")).isEqualTo("On ne khranil v svoyem zapase");
        Assertions.assertThat(tr.transliterate("Глубоких замыслов и дум;")).isEqualTo("Glubokikh zamyslov i dum;");
        Assertions.assertThat(tr.transliterate("Имел он не блестящий ум,")).isEqualTo("Imel on ne blestyashchy um,");
        Assertions.assertThat(tr.transliterate("Душой не слишком был отважен;")).isEqualTo("Dushoy ne slishkom byl otvazhen;");
        Assertions.assertThat(tr.transliterate("Зато был сух, учтив и важен.")).isEqualTo("Zato byl sukh, uchtiv i vazhen.");
    }

    @Test
    public void testLoremIpsum() {
        String loremIpsum = "\"Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.\"";
        Assertions.assertThat(tr.transliterate(loremIpsum)).isEqualTo(loremIpsum);
    }
}
