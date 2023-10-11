package it.test.regex;

import it.test.regex.statemachine.Constants;
import it.test.regex.statemachine.EventEnum;
import it.test.regex.statemachine.StateEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RegexService {

    private final StateMachine<StateEnum, EventEnum> stateMachine;
    private Pattern pattern = Pattern.compile("\\d+");

    public String calculateRegexFromString(List<String> input) {
        LinkedList<RegexGroup> regexGroups = new LinkedList<>();
        for (String code : input) {
            regexGroups = calculateRegexFromString(code, regexGroups);
        }

        return buildRegexString(regexGroups);
    }


    /**
     * Starting from a previous group list and an input code return a list of group with min and max occurrences
     */
    private LinkedList<RegexGroup> calculateRegexFromString(String inputCode, LinkedList<RegexGroup> previousGroupList) {

        if (!inputCode.matches("([A-Z])([A-Z || \\d]*)")) {
            throw new IllegalArgumentException("Invalid input");
        }

        stateMachine.getExtendedState().getVariables().put(Constants.PREVIOUS_GROUPS, previousGroupList);
        stateMachine.getExtendedState().getVariables().remove(Constants.CURRENT_GROUPS);

        Arrays.asList(inputCode.split(""))
                .forEach(character -> {
                    if (isNumeric(character)) {
                        stateMachine.sendEvent(EventEnum.NUMERIC_EVENT);
                    } else {
                        stateMachine.sendEvent(EventEnum.LITERAL_EVENT);
                    }
                });

        stateMachine.sendEvent(EventEnum.END_EVENT);
        LinkedList<RegexGroup> regexGroups = (LinkedList<RegexGroup>) stateMachine.getExtendedState().getVariables().get(Constants.CURRENT_GROUPS);

        if (previousGroupList.size() > regexGroups.size()) {
            for (int i = regexGroups.size(); i < previousGroupList.size(); i++) {
                RegexGroup regexGroup = previousGroupList.get(i);
                regexGroup.setMinOccurencies(0);
                regexGroups.add(regexGroup);
            }
        }

        return regexGroups;
    }

    private boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }

    private static String buildRegexString(LinkedList<RegexGroup> list) {
        StringBuilder builder = new StringBuilder();
        list.forEach(regexGroup -> {
            String value = switch (regexGroup.getType()) {
                case LITERAL -> "[A-Z]";
                case NUMERIC -> "\\d";
            };
            builder.append(value);

            if (regexGroup.getMinOccurencies() != regexGroup.getMaxOccurencies()) {
                builder.append("{" + regexGroup.getMinOccurencies() + "," + regexGroup.getMaxOccurencies() + "}");
            } else {
                if (regexGroup.getOccurencies() > 1) {
                    builder.append("{" + regexGroup.getOccurencies() + "}");
                }
            }

        });
        return builder.toString();
    }
}
