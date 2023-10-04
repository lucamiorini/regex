package it.test.regex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RegexApplicationIT {

    @Autowired
    private RegexService regexService;

    @DisplayName("When input start with a non literal character or contains invalid character throw exception")
    @ParameterizedTest
    @MethodSource("providerWrongParameter")
    void shouldValidateWrongFirstCharacter(List<String> input) {
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> regexService.calculateRegexFromString(input));
        assertEquals("Invalid input", illegalArgumentException.getMessage());
    }

    @DisplayName("When input is valid then return the correct regex")
    @ParameterizedTest
    @MethodSource("providerParameter")
    void isValid(List<String> input, String expectedRegex) {
        String resultedRegex = assertDoesNotThrow(() -> regexService.calculateRegexFromString(input));
        assertEquals(expectedRegex, resultedRegex);
    }

    private static Stream<Arguments> providerParameter() {
        return Stream.of(
                Arguments.of(List.of("AB123ZZ", "BB742TG", "CF678HG"), "[A-Z]{2}\\d{3}[A-Z]{2}"),
                Arguments.of(List.of("TNTTST80A01F205E", "MRNLCU83E20L872L"), "[A-Z]{6}\\d{2}[A-Z]\\d{2}[A-Z]\\d{3}[A-Z]"),
                Arguments.of(List.of("AA123", "ABC123", "BA1234", "AB12345"), "[A-Z]{2,3}\\d{3,5}"),
                Arguments.of(List.of("A123XY", "BA1234ZT", "AB12345B"), "[A-Z]{1,2}\\d{3,5}[A-Z]{1,2}")
        );
    }

    private static Stream<Arguments> providerWrongParameter() {
        return Stream.of(
                Arguments.of(List.of("1ABC")),
                Arguments.of(List.of("A1-BC")),
                Arguments.of(List.of("ABC-1"))
        );
    }

}
