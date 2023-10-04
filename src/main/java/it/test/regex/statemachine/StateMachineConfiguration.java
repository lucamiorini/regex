package it.test.regex.statemachine;

import it.test.regex.GroupType;
import it.test.regex.RegexGroup;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.LinkedList;

@Configuration
@EnableStateMachine
public class StateMachineConfiguration extends StateMachineConfigurerAdapter<StateEnum, EventEnum> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<StateEnum, EventEnum> config) throws Exception {
        config
                .withConfiguration()
                .autoStartup(true);
    }

    @Override
    public void configure(StateMachineStateConfigurer<StateEnum, EventEnum> states) throws Exception {
        states
                .withStates()
                .initial(StateEnum.INITIAL_STATE)
                .state(StateEnum.LITERAL_STATE, createGroupAction(GroupType.LITERAL), finalizeRegexGroup())
                .state(StateEnum.NUMERIC_STATE, createGroupAction(GroupType.NUMERIC), finalizeRegexGroup());
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<StateEnum, EventEnum> transitions) throws Exception {
        transitions
                .withExternal()
                    .source(StateEnum.INITIAL_STATE)
                    .target(StateEnum.LITERAL_STATE)
                    .event(EventEnum.LITERAL_EVENT)
                .and()
                    .withInternal()
                    .source(StateEnum.LITERAL_STATE)
                    .event(EventEnum.LITERAL_EVENT)
                    .action(incrementCounter())
                .and()
                .withExternal()
                    .source(StateEnum.LITERAL_STATE)
                    .target(StateEnum.NUMERIC_STATE)
                    .event(EventEnum.NUMERIC_EVENT)
                .and()
                .withInternal()
                    .source(StateEnum.NUMERIC_STATE)
                    .event(EventEnum.NUMERIC_EVENT)
                    .action(incrementCounter())
                .and()
                .withExternal()
                    .source(StateEnum.NUMERIC_STATE)
                    .target(StateEnum.LITERAL_STATE)
                    .event(EventEnum.LITERAL_EVENT)
                .and()
                .withExternal()
                    .source(StateEnum.LITERAL_STATE)
                    .target(StateEnum.INITIAL_STATE)
                    .event(EventEnum.END_EVENT)
                .and()
                .withExternal()
                    .source(StateEnum.NUMERIC_STATE)
                    .target(StateEnum.INITIAL_STATE)
                    .event(EventEnum.END_EVENT);
    }

    /**
     * Entering a new state a new object representing the input code must be created
     */
    private Action<StateEnum, EventEnum> createGroupAction(GroupType type) {
        return context -> {
            int numberOfElement = 1;

            //create a group with the given type
            RegexGroup regexGroup = new RegexGroup();
            regexGroup.setType(type);
            regexGroup.setOccurencies(numberOfElement);

            //check if the state machine already have a list of group in the context
            LinkedList<RegexGroup> currentResult = (LinkedList<RegexGroup>) context.getExtendedState().getVariables().get(Constants.CURRENT_GROUPS);
            if (currentResult == null) {
                //no list group in the state machine
                LinkedList<RegexGroup> groupList = new LinkedList<>();
                groupList.add(regexGroup);
                context.getExtendedState().getVariables().put(Constants.CURRENT_GROUPS, groupList);
            } else {
                currentResult.add(regexGroup);
            }
        };
    }

    /**
     * Exiting a state means the group is closed so min and max occurrences could be calculated based on the previous
     * and current input code
     */
    private Action<StateEnum, EventEnum> finalizeRegexGroup() {
        return context -> {
            LinkedList<RegexGroup> currentResult = (LinkedList<RegexGroup>) context.getExtendedState().getVariables().get(Constants.CURRENT_GROUPS);
            //retrieve the last written group
            RegexGroup last = currentResult.getLast();
            last.setMaxOccurencies(last.getOccurencies());
            last.setMinOccurencies(last.getOccurencies());

            LinkedList<RegexGroup> previousResultList = (LinkedList<RegexGroup>) context.getExtendedState().getVariables().get(Constants.PREVIOUS_GROUPS);

            //check which is the min and max values between the current and the previous input code in the same position
            if (previousResultList.size() > 0) {
                RegexGroup previousResultAtCurrentPosition = previousResultList.get(currentResult.size() - 1);
                if (last.getMaxOccurencies() < previousResultAtCurrentPosition.getMaxOccurencies()) {
                    last.setMaxOccurencies(previousResultAtCurrentPosition.getMaxOccurencies());
                }
                if (last.getMinOccurencies() > previousResultAtCurrentPosition.getMinOccurencies()) {
                    last.setMinOccurencies(previousResultAtCurrentPosition.getMinOccurencies());
                }
            }
        };
    }

    /**
     * When the state doesn't change it means it's the same group and only the element counter must be updated
     */
    private Action<StateEnum, EventEnum> incrementCounter() {
        return context -> {
            LinkedList<RegexGroup> currentResult = (LinkedList<RegexGroup>) context.getExtendedState().getVariables().get(Constants.CURRENT_GROUPS);
            RegexGroup last = currentResult.getLast();
            last.setOccurencies(last.getOccurencies()+1);
        };
    }
}
