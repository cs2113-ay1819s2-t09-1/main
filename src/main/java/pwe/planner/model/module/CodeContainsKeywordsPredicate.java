package pwe.planner.model.module;

import static java.util.Objects.requireNonNull;
import static pwe.planner.logic.parser.ParserUtil.parseKeyword;

import java.util.List;

/**
 * Tests that a {@code Module}'s {@code Code} matches any of the keywords given.
 */
public class CodeContainsKeywordsPredicate<T> implements KeywordsPredicate<T> {
    private final List<String> keywords;

    public CodeContainsKeywordsPredicate(List<String> keywords) {
        requireNonNull(keywords);

        this.keywords = keywords;
    }

    @Override
    public boolean test(T object) {
        requireNonNull(object);
        Module module = (Module) object;

        String moduleCode = module.getCode().toString();
        return keywords.stream().anyMatch(keyword -> parseKeyword(keyword, moduleCode));
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof CodeContainsKeywordsPredicate // instanceof handles nulls
                && keywords.equals(((CodeContainsKeywordsPredicate) other).keywords)); // state check
    }

}