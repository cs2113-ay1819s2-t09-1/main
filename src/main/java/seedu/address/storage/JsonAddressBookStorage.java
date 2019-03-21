package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataConversionException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.FileUtil;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.module.Module;
import seedu.address.model.requirement.RequirementCategory;

/**
 * A class to call JsonSerializableAddressBook methods
 */
public class JsonAddressBookStorage implements AddressBookStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonSerializableAddressBook.class);

    private Path moduleListFilePath;
    private Path requirementCategoryListFilePath;

    public JsonAddressBookStorage(Path moduleListFilePath, Path requirementCategoryListFilePath) {
        this.moduleListFilePath = moduleListFilePath;
        this.requirementCategoryListFilePath = requirementCategoryListFilePath;
    }

    public Path getModuleListFilePath() {
        return moduleListFilePath;
    }

    public Path getRequirementCategoryListFilePath() {
        return requirementCategoryListFilePath;
    }

    @Override
    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        saveModuleList(addressBook, getModuleListFilePath());
        saveRequirementCategoryList(addressBook, getRequirementCategoryListFilePath());
    }

    @Override
    public Optional<ReadOnlyAddressBook> readAddressBook() throws DataConversionException {
        return readAddressBook(moduleListFilePath, requirementCategoryListFilePath);
    }

    /**
     * @param moduleListFilePath and @param requirementCategoryListFilePath location of the data. Cannot be null.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook(Path moduleListFilePath, Path requirementCategoryListFilePath)
            throws DataConversionException {
        requireNonNull(moduleListFilePath);
        requireNonNull(requirementCategoryListFilePath);
        Optional<ObservableList<Module>> optionalModuleObservableList = readModuleList(moduleListFilePath);
        Optional<ObservableList<RequirementCategory>> optionalRequirementCategoryObservableList =
                readRequirementCategoryList(requirementCategoryListFilePath);

        if (!optionalModuleObservableList.isPresent()) {
            return Optional.empty();
        } else if (!optionalRequirementCategoryObservableList.isPresent()) {
            return Optional.empty();
        }

        try {
            JsonSerializableAddressBook addressBook =
                    new JsonSerializableAddressBook(optionalModuleObservableList.get(),
                            optionalRequirementCategoryObservableList.get());
            ReadOnlyAddressBook jsonAddressBook = addressBook.toModelType();
            return Optional.of(jsonAddressBook);
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + moduleListFilePath + ": " + ive.getMessage());
            logger.info("Illegal values found in " + requirementCategoryListFilePath + ": " + ive.getMessage());
            throw new DataConversionException(ive);
        }
    }

    @Override
    public Optional<ObservableList<Module>> readModuleList() throws DataConversionException {
        return readModuleList(moduleListFilePath);
    }

    /**
     * @param filePath location of the data. Cannot be null.
     */
    public Optional<ObservableList<Module>> readModuleList(Path filePath) throws DataConversionException {
        requireNonNull(filePath);
        Optional<JsonSerializableModuleList> jsonAddressBook = JsonUtil.readJsonFile(
                filePath, JsonSerializableAddressBook.getJsonSerializableModuleListClass());
        if (!jsonAddressBook.isPresent()) {
            return Optional.empty();
        }

        try {
            return Optional.of(jsonAddressBook.get().toModelType());
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
            throw new DataConversionException(ive);
        }
    }

    @Override
    public void saveModuleList(ReadOnlyAddressBook addressBook) throws IOException {
        saveModuleList(addressBook, moduleListFilePath);
    }

    /**
     * @param filePath location of the data. Cannot be null.
     */
    public void saveModuleList(ReadOnlyAddressBook addressBook, Path filePath) throws IOException {
        requireNonNull(addressBook);
        requireNonNull(filePath);
        FileUtil.createIfMissing(filePath);
        JsonUtil.saveJsonFile(new JsonSerializableModuleList(addressBook), filePath);
    }

    @Override
    public Optional<ObservableList<RequirementCategory>> readRequirementCategoryList() throws DataConversionException {
        return readRequirementCategoryList(requirementCategoryListFilePath);
    }

    /**
     * @param filePath location of the data. Cannot be null.
     */
    public Optional<ObservableList<RequirementCategory>> readRequirementCategoryList(Path filePath)
            throws DataConversionException {
        requireNonNull(filePath);
        Optional<JsonSerializableRequirementCategoryList> jsonAddressBook = JsonUtil.readJsonFile(
                filePath, JsonSerializableAddressBook.getJsonSerializableRequirementCategoryListClass());
        if (!jsonAddressBook.isPresent()) {
            return Optional.empty();
        }

        try {
            return Optional.of(jsonAddressBook.get().toModelType());
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
            throw new DataConversionException(ive);
        }
    }

    @Override
    public void saveRequirementCategoryList(ReadOnlyAddressBook requirementCategoryList)
            throws IOException {
        saveRequirementCategoryList(requirementCategoryList, requirementCategoryListFilePath);
    }

    /**
     * @param filePath location of the data. Cannot be null.
     */
    public void saveRequirementCategoryList(ReadOnlyAddressBook requirementCategoryList, Path filePath)
            throws IOException {
        requireNonNull(requirementCategoryList);
        requireNonNull(filePath);
        FileUtil.createIfMissing(filePath);
        JsonUtil.saveJsonFile(new JsonSerializableRequirementCategoryList(requirementCategoryList), filePath);
    }

}
