package it.test.regex.statemachine;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {
    /**
     * Key used to retrieve the object representing the current input string
     */
    public static final String CURRENT_GROUPS = "currentGroups";

    /**
     * Key used to retrieve the object representing the previous input string
     */
    public static final String PREVIOUS_GROUPS = "previousResultList";
}
