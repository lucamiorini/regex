package it.test.regex;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Object representing a regex group
 */
@Getter
@Setter
@ToString
public class RegexGroup {
    private GroupType type;
    private int occurencies;
    private int minOccurencies;
    private int maxOccurencies;
}
