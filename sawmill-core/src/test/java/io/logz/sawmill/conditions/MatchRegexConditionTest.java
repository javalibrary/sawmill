package io.logz.sawmill.conditions;

import io.logz.sawmill.Doc;
import org.junit.Test;

import java.util.regex.PatternSyntaxException;

import static io.logz.sawmill.utils.DocUtils.createDoc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MatchRegexConditionTest {

    @Test
    public void testMatchingValueCaseSensitive() {
        String field = "field1";
        String regex = "^Hello.+";
        MatchRegexCondition matchRegexCondition = new MatchRegexCondition(field, regex, false, false);

        Doc doc = createDoc("field1", "Hello World!");
        assertThat(matchRegexCondition.evaluate(doc)).isTrue();

        doc = createDoc("field1", "hello World!");
        assertThat(matchRegexCondition.evaluate(doc)).isFalse();
    }

    @Test
    public void testMatchingValueCaseInsensitive() {
        String field = "field1";
        String regex = "^Hello.+";
        MatchRegexCondition matchRegexCondition = new MatchRegexCondition(field, regex, true, false);

        Doc doc = createDoc("field1", "Hello World!");
        assertThat(matchRegexCondition.evaluate(doc)).isTrue();

        doc = createDoc("field1", "hello World!");
        assertThat(matchRegexCondition.evaluate(doc)).isTrue();
    }

    @Test
    public void testMatchingPartOfValue() {
        String field = "field1";
        String regex = "World";
        MatchRegexCondition matchRegexCondition = new MatchRegexCondition(field, regex, false, false);

        Doc doc = createDoc("field1", "Hello World!");
        assertThat(matchRegexCondition.evaluate(doc)).isFalse();

        MatchRegexCondition matchPartOfValueRegexCondition = new MatchRegexCondition(field, regex, false, true);
        doc = createDoc("field1", "hello World!");
        assertThat(matchPartOfValueRegexCondition.evaluate(doc)).isTrue();
    }

    @Test
    public void testMatchingPartOfValueCaseInsensitive() {
        String field = "field1";
        String regex = "world";
        MatchRegexCondition matchRegexCondition = new MatchRegexCondition(field, regex, false, true);

        Doc doc = createDoc("field1", "Hello World!");
        assertThat(matchRegexCondition.evaluate(doc)).isFalse();

        MatchRegexCondition matchPartOfValueRegexCondition = new MatchRegexCondition(field, regex, true, true);
        doc = createDoc("field1", "hello World!");
        assertThat(matchPartOfValueRegexCondition.evaluate(doc)).isTrue();
    }

    @Test
    public void testNotMatchingValue() {
        String field = "field1";
        String regex = "Wed.+";
        MatchRegexCondition matchRegexCondition = new MatchRegexCondition(field, regex, false, false);

        Doc doc = createDoc("field1", "Thursday");
        assertThat(matchRegexCondition.evaluate(doc)).isFalse();
    }

    @Test
    public void testInvalidRegex() {
        String field = "field1";
        String invalidRegexEscaping = "Wed\\"+'x';
        assertThatThrownBy(() -> new MatchRegexCondition(field, invalidRegexEscaping, false, false))
                .isInstanceOf(PatternSyntaxException.class)
                .hasMessageContaining("Illegal hexadecimal escape sequence near index");

        String invalidRegex = "[]";
        assertThatThrownBy(() -> new MatchRegexCondition(field, invalidRegex, false, false))
                .isInstanceOf(PatternSyntaxException.class)
                .hasMessageContaining("Unclosed character class near index");
    }

    @Test
    public void testWrongValueType() {
        String field = "field1";
        String regex = "Wed.+";
        MatchRegexCondition matchRegexCondition = new MatchRegexCondition(field, regex, false, false);

        Doc doc = createDoc("field1", 123);
        assertThat(matchRegexCondition.evaluate(doc)).isFalse();
    }

}