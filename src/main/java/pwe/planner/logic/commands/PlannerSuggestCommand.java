package pwe.planner.logic.commands;

import static java.lang.Math.abs;
import static java.util.Objects.requireNonNull;
import static pwe.planner.commons.util.CollectionUtil.requireAllNonNull;
import static pwe.planner.logic.parser.CliSyntax.PREFIX_CREDITS;
import static pwe.planner.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;
import pwe.planner.commons.util.StringUtil;
import pwe.planner.logic.CommandHistory;
import pwe.planner.model.Model;
import pwe.planner.model.module.Code;
import pwe.planner.model.module.Credits;
import pwe.planner.model.module.Module;
import pwe.planner.model.planner.DegreePlanner;
import pwe.planner.model.tag.Tag;

/**
 * Suggests module(s) to take.
 */
public class PlannerSuggestCommand extends Command {

    public static final String COMMAND_WORD = "planner_suggest";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Suggests module(s) to take.\n"
            + "Format: " + COMMAND_WORD + ' '
            + PREFIX_CREDITS + "CREDITS "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_CREDITS + "2 "
            + PREFIX_TAG + "algorithms "
            + PREFIX_TAG + "c";

    public static final String MESSAGE_SUCCESS = "The lists are sorted with the more recommended module(s)"
            + " in front.\nModule(s) recommended: %1$s\nModule(s) with relevant"
            + " tags: %2$s\nModule(s) with matching credits: %3$s";
    private static final int MAX_NUMBER_OF_ELEMENETS = 10;
    private static final int VALUE_OF_NO_DIFFERENCE = 0;

    private Credits creditsToFind;
    private Set<Tag> tagsToFind;

    /**
     * Creates a PlannerSuggestCommand to suggest {@code codes} to take.
     */
    public PlannerSuggestCommand(Credits bestCredits, Set<Tag> tags) {
        requireAllNonNull(bestCredits, tags);

        creditsToFind = bestCredits;
        tagsToFind = tags;
    }

    @Override
    public CommandResult execute(Model model, CommandHistory history) {
        requireNonNull(model);

        Set<Code> plannerCodes = new HashSet<>();
        // Adds all the codes in the degree plan to a set.
        model.getApplication().getDegreePlannerList()
                .stream().map(DegreePlanner::getCodes).forEach(plannerCodes::addAll);

        ObservableList<Module> moduleList = model.getApplication().getModuleList();
        List<ModuleToSuggest> modulesToSuggest = new ArrayList<>();
        List<ModuleToSuggest> modulesWithMatchingTags = new ArrayList<>();
        List<ModuleToSuggest> modulesWithMatchingCredits = new ArrayList<>();
        for (Module module : moduleList) {
            // Finds the matching tags for each module.
            Set<Tag> matchingTags = new HashSet<>(tagsToFind);
            matchingTags.retainAll(module.getTags());

            // Finds the credits difference.
            int credit = Integer.valueOf((module.getCredits().toString()));
            int bestCredits = Integer.valueOf(creditsToFind.toString());
            int creditDifference = abs(credit - bestCredits);

            ModuleToSuggest moduleToSuggest =
                    new ModuleToSuggest(creditDifference, matchingTags.size(), module.getCode());
            modulesToSuggest.add(moduleToSuggest);

            if (!matchingTags.isEmpty()) {
                modulesWithMatchingTags.add(moduleToSuggest);
            }

            if (creditDifference == VALUE_OF_NO_DIFFERENCE) {
                modulesWithMatchingCredits.add(moduleToSuggest);
            }
        }

        // Returns a sorted list of codes to suggest based on both credits and tags.
        List<Code> codesToSuggest = modulesToSuggest.stream().sorted().map(ModuleToSuggest::getModuleCode)
                .filter(code -> !plannerCodes.contains(code)).limit(MAX_NUMBER_OF_ELEMENETS)
                    .collect(Collectors.toList());
        // Converts the list to a string to remove the brackets of list.
        String suggestionString = StringUtil.joinStreamAsString(codesToSuggest.stream());

        // Returns a sorted list of codes with matching tags in the recommendation list.
        List<Code> codesWithMatchingTags = modulesWithMatchingTags.stream().sorted().map(ModuleToSuggest::getModuleCode)
                .filter(code -> !plannerCodes.contains(code) && codesToSuggest.contains(code))
                    .collect(Collectors.toList());
        String matchingTagCodeString = StringUtil.joinStreamAsString(codesWithMatchingTags.stream());

        // Returns a sorted list of codes with matching credits in the recommendation list.
        List<Code> codesWithMatchingCredits = modulesWithMatchingCredits.stream().sorted()
                .map(ModuleToSuggest::getModuleCode).filter(code -> !plannerCodes.contains(code) && codesToSuggest
                        .contains(code)).collect(Collectors.toList());
        String matchingCreditCodeString = StringUtil.joinStreamAsString(codesWithMatchingCredits.stream());

        return new CommandResult(String.format(MESSAGE_SUCCESS, suggestionString, matchingTagCodeString,
                matchingCreditCodeString));
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof PlannerSuggestCommand // instanceof handles nulls
                && tagsToFind.equals(((PlannerSuggestCommand) other).tagsToFind)
                && creditsToFind.equals(((PlannerSuggestCommand) other).creditsToFind));
    }

    /**
     * Creates a ModuleToSuggest object for sorting.
     */
    public class ModuleToSuggest implements Comparable<ModuleToSuggest> {
        private int creditDifference;
        private int numberOfMatchingTags;
        private Code moduleCode;

        public ModuleToSuggest(int creditDifference, int numberOfMatchingTags, Code moduleCode) {
            this.creditDifference = creditDifference;
            this.numberOfMatchingTags = numberOfMatchingTags;
            this.moduleCode = moduleCode;
        }

        public int getCreditDifference() {
            return creditDifference;
        }

        public int getNumberOfMatchingTags() {
            return numberOfMatchingTags;
        }

        public Code getModuleCode() {
            return moduleCode;
        }

        /**
         * @param moduleToCompare A valid module to suggest
         * @return number of matching tags difference between two modules to suggest, or
         * credit difference between two modules if tie. If both tie, sort according to
         * alphabetical order.
         */
        @Override
        public int compareTo(ModuleToSuggest moduleToCompare) {
            if (this.getNumberOfMatchingTags() == moduleToCompare.getNumberOfMatchingTags()
                && this.getCreditDifference() == moduleToCompare.getCreditDifference()) {
                return this.getModuleCode().compareTo(moduleToCompare.getModuleCode());
            } else if (this.getNumberOfMatchingTags() == moduleToCompare.getNumberOfMatchingTags()) {
                return this.getCreditDifference() - moduleToCompare.getCreditDifference();
            }
            return moduleToCompare.getNumberOfMatchingTags() - this.getNumberOfMatchingTags();
        }
    }
}
