package ru.sbtqa.tag.pagefactory.support.data;

import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Tag;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.reflect.FieldUtils;
import ru.sbtqa.tag.datajack.TestDataObject;
import ru.sbtqa.tag.datajack.exceptions.DataException;

public class DataParser {

    private static final String STEP_PARSE_REGEX = "(?:(@[^\\$]+)?(\\$\\{[^\\}]+\\}))+";
    private static final String TAG_PARSE_REGEX = "([^\\$]+)(?:\\$\\{([^\\}]+)\\})?";

    private String featureDataTag;
    private String currentScenarioTag;

    public List<Tag> getScenarioTags(ScenarioDefinition scenarioDefinition) {
        try {
            return (List<Tag>) FieldUtils.readField(scenarioDefinition, "tags", true);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            return new ArrayList<>();
        }
    }

    public String parseTags(List<Tag> tags) throws DataException {
        Optional<Tag> dataTag = tags.stream().filter(predicate -> predicate.getName().startsWith("@data")).findFirst();
        return dataTag.isPresent() ? dataTag.get().getName().split("=")[1].trim() : null;
    }

    public String parseString(String raw) throws DataException {
        Pattern stepDataPattern = Pattern.compile(STEP_PARSE_REGEX);
        Matcher stepDataMatcher = stepDataPattern.matcher(raw);
        StringBuffer parsedStep = new StringBuffer(raw);
        int skipRange = 0;

        while (stepDataMatcher.find()) {
            String collection = stepDataMatcher.group(1);
            String value = stepDataMatcher.group(2);

            if (value == null) {
                continue;
            }
            if (collection != null) {
                DataProvider.updateCollection(DataProvider.getInstance().fromCollection(collection.replace("@", "")));

                parsedStep = parsedStep.replace(stepDataMatcher.start(1) + skipRange, stepDataMatcher.end(1) + skipRange, "");
                skipRange += "".length() - collection.length();
            } else {
                String tag = currentScenarioTag != null ? currentScenarioTag : featureDataTag;

                if (tag != null) {
                    parseTestDataObject(tag);
                }
            }

            String dataPath = value.replace("${", "").replace("}", "");
            String parsedValue = DataProvider.getInstance().get(dataPath).getValue();
            parsedStep = parsedStep.replace(stepDataMatcher.start(2) + skipRange, stepDataMatcher.end(2) + skipRange, parsedValue);
            skipRange += parsedValue.length() - value.length();
        }
        return parsedStep.toString();
    }

    private void parseTestDataObject(String tag) throws DataException {
        Pattern dataP = Pattern.compile(TAG_PARSE_REGEX);
        Matcher m = dataP.matcher(tag.trim());

        if (m.matches()) {
            String collection = m.group(1);
            String value = m.group(2);
            TestDataObject tdo = DataProvider.getInstance().fromCollection(collection);

            if (value != null) {
                tdo = tdo.get(value);
            }

            DataProvider.updateCollection(tdo);
        }
    }

    public void setFeatureDataTag(String featureDataTag) {
        this.featureDataTag = featureDataTag;
    }

    public void setCurrentScenarioTag(String currentScenarioTag) {
        this.currentScenarioTag = currentScenarioTag;
    }
}
