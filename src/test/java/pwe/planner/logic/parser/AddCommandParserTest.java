package pwe.planner.logic.parser;

import static pwe.planner.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static pwe.planner.logic.commands.CommandTestUtil.CODE_DESC_AMY;
import static pwe.planner.logic.commands.CommandTestUtil.CODE_DESC_BOB;
import static pwe.planner.logic.commands.CommandTestUtil.CREDITS_DESC_AMY;
import static pwe.planner.logic.commands.CommandTestUtil.CREDITS_DESC_BOB;
import static pwe.planner.logic.commands.CommandTestUtil.INVALID_CODE_DESC;
import static pwe.planner.logic.commands.CommandTestUtil.INVALID_CREDITS_DESC;
import static pwe.planner.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static pwe.planner.logic.commands.CommandTestUtil.INVALID_TAG_DESC;
import static pwe.planner.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static pwe.planner.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static pwe.planner.logic.commands.CommandTestUtil.PREAMBLE_NON_EMPTY;
import static pwe.planner.logic.commands.CommandTestUtil.PREAMBLE_WHITESPACE;
import static pwe.planner.logic.commands.CommandTestUtil.TAG_DESC_FRIEND;
import static pwe.planner.logic.commands.CommandTestUtil.TAG_DESC_HUSBAND;
import static pwe.planner.logic.commands.CommandTestUtil.VALID_CODE_BOB;
import static pwe.planner.logic.commands.CommandTestUtil.VALID_CREDITS_BOB;
import static pwe.planner.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static pwe.planner.logic.commands.CommandTestUtil.VALID_TAG_FRIEND;
import static pwe.planner.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static pwe.planner.logic.parser.CommandParserTestUtil.assertParseFailure;
import static pwe.planner.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static pwe.planner.testutil.TypicalModules.AMY;
import static pwe.planner.testutil.TypicalModules.BOB;

import org.junit.Test;

import pwe.planner.logic.commands.AddCommand;
import pwe.planner.model.module.Code;
import pwe.planner.model.module.Credits;
import pwe.planner.model.module.Module;
import pwe.planner.model.module.Name;
import pwe.planner.model.tag.Tag;
import pwe.planner.testutil.ModuleBuilder;

public class AddCommandParserTest {
    private AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        Module expectedModule =
                new ModuleBuilder(BOB).withTags(VALID_TAG_FRIEND).build();

        // whitespace only preamble
        assertParseSuccess(parser, PREAMBLE_WHITESPACE + NAME_DESC_BOB + CREDITS_DESC_BOB
                + CODE_DESC_BOB + TAG_DESC_FRIEND, new AddCommand(expectedModule));

        // multiple names - last name accepted
        assertParseSuccess(parser, NAME_DESC_AMY + NAME_DESC_BOB + CREDITS_DESC_BOB
                + CODE_DESC_BOB + TAG_DESC_FRIEND, new AddCommand(expectedModule));

        // multiple credits - last credits accepted
        assertParseSuccess(parser, NAME_DESC_BOB + CREDITS_DESC_AMY
                + CREDITS_DESC_BOB + CODE_DESC_BOB + TAG_DESC_FRIEND, new AddCommand(expectedModule));

        // multiple codes - last code accepted
        assertParseSuccess(parser, NAME_DESC_BOB + CREDITS_DESC_BOB
                + CODE_DESC_AMY + CODE_DESC_BOB + TAG_DESC_FRIEND, new AddCommand(expectedModule));

        // multiple tags - all accepted
        Module expectedModuleMultipleTags =
                new ModuleBuilder(BOB).withTags(VALID_TAG_FRIEND, VALID_TAG_HUSBAND).build();
        assertParseSuccess(parser, NAME_DESC_BOB + CREDITS_DESC_BOB + CODE_DESC_BOB
                + TAG_DESC_HUSBAND + TAG_DESC_FRIEND, new AddCommand(expectedModuleMultipleTags));
    }

    @Test
    public void parse_optionalFieldsMissing_success() {
        // zero tags
        Module expectedModule = new ModuleBuilder(AMY).withTags().build();
        assertParseSuccess(parser, NAME_DESC_AMY + CREDITS_DESC_AMY
                + CODE_DESC_AMY, new AddCommand(expectedModule));
    }

    @Test
    public void parse_compulsoryFieldMissing_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        // missing name prefix
        assertParseFailure(parser, VALID_NAME_BOB + CREDITS_DESC_BOB
                + CODE_DESC_BOB, expectedMessage);

        // missing credits prefix
        assertParseFailure(parser, NAME_DESC_BOB + VALID_CREDITS_BOB
                + CODE_DESC_BOB, expectedMessage);

        // missing code prefix
        assertParseFailure(parser, NAME_DESC_BOB + CREDITS_DESC_BOB
                + VALID_CODE_BOB, expectedMessage);

        // all prefixes missing
        assertParseFailure(parser, VALID_NAME_BOB
                + VALID_CREDITS_BOB + VALID_CODE_BOB, expectedMessage);
    }

    @Test
    public void parse_invalidValue_failure() {
        // invalid name
        assertParseFailure(parser, INVALID_NAME_DESC + CREDITS_DESC_BOB + CODE_DESC_BOB + TAG_DESC_HUSBAND
                + TAG_DESC_FRIEND, Name.MESSAGE_CONSTRAINTS);

        // invalid credits
        assertParseFailure(parser, NAME_DESC_BOB + INVALID_CREDITS_DESC + CODE_DESC_BOB + TAG_DESC_HUSBAND
                + TAG_DESC_FRIEND, Credits.MESSAGE_CONSTRAINTS);

        // invalid code
        assertParseFailure(parser, NAME_DESC_BOB + CREDITS_DESC_BOB + INVALID_CODE_DESC + TAG_DESC_HUSBAND
                + TAG_DESC_FRIEND, Code.MESSAGE_CONSTRAINTS);

        // invalid tag
        assertParseFailure(parser, NAME_DESC_BOB + CREDITS_DESC_BOB + CODE_DESC_BOB + INVALID_TAG_DESC
                + VALID_TAG_FRIEND, Tag.MESSAGE_CONSTRAINTS);

        // two invalid values, only first invalid value reported
        assertParseFailure(parser, INVALID_NAME_DESC + CREDITS_DESC_BOB + INVALID_CODE_DESC,
                Code.MESSAGE_CONSTRAINTS);

        // non-empty preamble
        assertParseFailure(parser, PREAMBLE_NON_EMPTY + NAME_DESC_BOB + CREDITS_DESC_BOB + CODE_DESC_BOB
                        + TAG_DESC_HUSBAND + TAG_DESC_FRIEND,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
    }
}